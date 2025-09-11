package app.domain.payment.service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import app.commonUtil.apiPayload.ApiResponse;
import app.commonUtil.apiPayload.code.status.ErrorStatus;
import app.commonUtil.apiPayload.exception.GeneralException;
import app.commonUtil.security.TokenPrincipalParser;
import app.domain.payment.client.InternalOrderClient;
import app.domain.payment.model.dto.request.CancelPaymentRequest;
import app.domain.payment.model.dto.request.OrderInfo;
import app.domain.payment.model.dto.request.PaymentConfirmRequest;
import app.domain.payment.model.dto.request.PaymentFailRequest;
import app.domain.payment.model.entity.Payment;
import app.domain.payment.model.entity.PaymentEtc;
import app.domain.payment.model.entity.enums.PaymentStatus;
import app.domain.payment.model.repository.PaymentEtcRepository;
import app.domain.payment.model.repository.PaymentRepository;
import app.domain.payment.status.PaymentErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

	@Value("${TOSS_SECRET_KEY}")
	private String tossSecretKey;
	@Value("${TOSS_URL}")
	private String tossUrl;

	private final PaymentRepository paymentRepository;
	private final PaymentEtcRepository paymentEtcRepository;
	private final InternalOrderClient internalOrderClient;
	private final TokenPrincipalParser tokenPrincipalParser;
	private final PaymentApprovedProducer paymentApprovedProducer;

	private String generateIdempotencyKey(Long userId, String orderId) {
		try {
			String input = userId + orderId;
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(hash);
		} catch (Exception e) {
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}

	public String callTossConfirmApi(PaymentConfirmRequest request, Long userId) {
		try {
			String widgetSecretKey = tossSecretKey;
			Base64.Encoder encoder = Base64.getEncoder();
			byte[] encodedBytes = encoder.encode((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));
			String authorizations = "Basic " + new String(encodedBytes);
			String fullUrl = tossUrl + "/confirm";
			String idempotencyKey = generateIdempotencyKey(userId, request.getOrderId());

			JSONObject obj = new JSONObject();
			obj.put("orderId", request.getOrderId());
			obj.put("amount", request.getAmount());
			obj.put("paymentKey", request.getPaymentKey());

			URL url = new URL(fullUrl);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setRequestProperty("Authorization", authorizations);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Idempotency-Key", idempotencyKey);
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);

			OutputStream outputStream = connection.getOutputStream();
			outputStream.write(obj.toString().getBytes("UTF-8"));

			int code = connection.getResponseCode();
			boolean isSuccess = code == 200;

			java.io.InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();
			java.io.BufferedReader reader = new java.io.BufferedReader(
				new java.io.InputStreamReader(responseStream, StandardCharsets.UTF_8));
			StringBuilder responseBuilder = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				responseBuilder.append(line);
			}
			String responseBody = responseBuilder.toString();
			return (isSuccess ? "success:" : "fail:") + responseBody;
		} catch (Exception e) {
			throw new GeneralException(PaymentErrorStatus.TOSS_API_ERROR);
		}
	}

	public String callTossCancelApi(String paymentKey, String cancelReason, Long userId, UUID orderId) {
		try {
			String widgetSecretKey = tossSecretKey;
			Base64.Encoder encoder = Base64.getEncoder();
			byte[] encodedBytes = encoder.encode((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));
			String authorizations = "Basic " + new String(encodedBytes);
			String idempotencyKey = generateIdempotencyKey(userId, orderId.toString());

			JSONObject obj = new JSONObject();
			obj.put("cancelReason", cancelReason);

			String cancelUrl = tossUrl + "/" + paymentKey + "/cancel";

			URL url = new URL(cancelUrl);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setRequestProperty("Authorization", authorizations);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Idempotency-Key", idempotencyKey);
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);

			OutputStream outputStream = connection.getOutputStream();
			outputStream.write(obj.toString().getBytes("UTF-8"));

			int code = connection.getResponseCode();
			boolean isSuccess = code == 200;

			java.io.InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();
			java.io.BufferedReader reader = new java.io.BufferedReader(
				new java.io.InputStreamReader(responseStream, StandardCharsets.UTF_8));
			StringBuilder responseBuilder = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				responseBuilder.append(line);
			}
			String responseBody = responseBuilder.toString();
			return (isSuccess ? "success:" : "fail:") + responseBody;
		} catch (Exception e) {
			throw new GeneralException(PaymentErrorStatus.TOSS_API_ERROR);
		}
	}

	@Transactional
	public String confirmPayment(PaymentConfirmRequest request, Authentication authentication) {
		String userIdStr = tokenPrincipalParser.getUserId(authentication);
		Long userId = Long.parseLong(userIdStr);
		ApiResponse<OrderInfo> orderInfoResponse;
		try {
			orderInfoResponse = internalOrderClient.getOrderInfo(UUID.fromString(request.getOrderId()));
		} catch (HttpServerErrorException | HttpClientErrorException e){
			log.error("Order Service Error: {}", e.getResponseBodyAsString());
			throw new GeneralException(ErrorStatus.ORDER_NOT_FOUND);
		}

		OrderInfo orderInfo = orderInfoResponse.result();

		long requestAmount = Long.parseLong(request.getAmount());
		if (orderInfo.getTotalPrice() != requestAmount) {
			throw new GeneralException(PaymentErrorStatus.PAYMENT_AMOUNT_MISMATCH);
		}

		String responseWithPrefix = callTossConfirmApi(request, userId);
		boolean isSuccess = responseWithPrefix.startsWith("success:");
		String responseBody = responseWithPrefix.substring(responseWithPrefix.indexOf(":") + 1);
		PaymentStatus paymentStatus = isSuccess ? PaymentStatus.COMPLETED : PaymentStatus.FAILED;

		Payment payment = Payment.builder()
			.ordersId(UUID.fromString(request.getOrderId()))
			.paymentKey(request.getPaymentKey())
			.paymentMethod(orderInfo.getPaymentMethodEnum())
			.paymentStatus(paymentStatus)
			.amount(orderInfo.getTotalPrice())
			.build();

		Payment savedPayment = paymentRepository.save(payment);

		PaymentEtc paymentEtc = PaymentEtc.builder()
			.payment(savedPayment)
			.paymentResponse(responseBody)
			.build();

		paymentEtcRepository.save(paymentEtc);


		Map<String, Object> body = new HashMap<>();
		body.put("userID",userId);

		Map<String, Object> headers = new HashMap<>();
		headers.put("eventType", "success");
		headers.put("orderId",request.getOrderId());

		paymentApprovedProducer.sendPaymentApproved(headers,body);

		return "결제 승인이 완료되었습니다.";

	}

	@Transactional
	public String failSave(PaymentFailRequest request) {
		ApiResponse<String> updateOrderStatusResponse;
		try{
			updateOrderStatusResponse=internalOrderClient.updateOrderStatus(UUID.fromString(request.getOrderId()), "FAILED");
		}catch (HttpServerErrorException | HttpClientErrorException e){
			log.error("Order Service Error: {}",e.getResponseBodyAsString());
			throw new GeneralException(PaymentErrorStatus.ORDER_UPDATE_STATUS_FAILED);
		}
		return "결제 실패 처리가 완료되었습니다.";
	}

	@Transactional
	public String cancelPayment(CancelPaymentRequest request, Authentication authentication) {
		String userIdStr = tokenPrincipalParser.getUserId(authentication);
		Long userId = Long.parseLong(userIdStr);
		ApiResponse<OrderInfo> orderInfoResponse;
		try {
			orderInfoResponse = internalOrderClient.getOrderInfo(request.getOrderId());
		} catch (HttpServerErrorException | HttpClientErrorException e){
			log.error("Order Service Error: {}", e.getResponseBodyAsString());
			throw new GeneralException(ErrorStatus.ORDER_NOT_FOUND);
		}

		OrderInfo orderInfo = orderInfoResponse.result();
		if (!orderInfo.getIsRefundable()) {
			throw new GeneralException(PaymentErrorStatus.PAYMENT_NOT_REFUNDABLE);
		}

		Payment payment = paymentRepository.findByOrdersId(request.getOrderId())
			.orElseThrow(() -> new GeneralException(ErrorStatus.PAYMENT_NOT_FOUND));

		String responseWithPrefix = callTossCancelApi(payment.getPaymentKey(), request.getCancelReason(), userId,
			request.getOrderId());
		boolean isSuccess = responseWithPrefix.startsWith("success:");
		String responseBody = responseWithPrefix.substring(responseWithPrefix.indexOf(":") + 1);

		if (isSuccess) {
			ApiResponse<String> updateOrderStatusResponse;
			try{
				updateOrderStatusResponse=internalOrderClient.updateOrderStatus(request.getOrderId(), "REFUNDED");
			} catch (HttpServerErrorException | HttpClientErrorException e){
				log.error("Order Service Error: {}", e.getResponseBodyAsString());
				throw new GeneralException(PaymentErrorStatus.ORDER_UPDATE_STATUS_FAILED);
			}

			ApiResponse<String> addOrderHistoryResponse;
			try {
				addOrderHistoryResponse =internalOrderClient.addOrderHistory(request.getOrderId(), "cancel");
			} catch (HttpServerErrorException | HttpClientErrorException e){
				log.error("Order Service Error: {}", e.getResponseBodyAsString());
				throw new GeneralException(PaymentErrorStatus.ORDER_ADD_STATUS_FAILED);
			}
			payment.updatePaymentStatus(PaymentStatus.CANCELLED);
		}

		PaymentEtc paymentEtc = PaymentEtc.builder()
			.payment(payment)
			.paymentResponse(responseBody)
			.build();

		paymentEtcRepository.save(paymentEtc);

		if (isSuccess) {
			return "결제 취소가 완료되었습니다.";
		} else {
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}
}