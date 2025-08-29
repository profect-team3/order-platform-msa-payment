package app.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.commonUtil.apiPayload.code.status.ErrorStatus;
import app.commonUtil.apiPayload.exception.GeneralException;
import app.domain.payment.controller.PaymentController;
import app.domain.payment.model.dto.request.CancelPaymentRequest;
import app.domain.payment.model.dto.request.PaymentConfirmRequest;
import app.domain.payment.model.dto.request.PaymentFailRequest;
import app.domain.payment.service.PaymentService;
import app.domain.payment.status.PaymentErrorStatus;
import app.domain.payment.status.PaymentSuccessStatus;

@WebMvcTest(PaymentController.class)
@DisplayName("PaymentController Test")
class PaymentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private WebApplicationContext context;

	@MockitoBean
	private PaymentService paymentService;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders
			.webAppContextSetup(context)
			.build();
	}

	@Test
	@DisplayName("결제 승인 - 성공")
	void confirmPayment_Success() throws Exception {
		PaymentConfirmRequest request = new PaymentConfirmRequest(
			"test_payment_key",
			UUID.randomUUID().toString(),
			"10000"
		);
		String resultMessage = "결제 승인이 완료되었습니다.";

		when(paymentService.confirmPayment(any(PaymentConfirmRequest.class),any()))
			.thenReturn(resultMessage);

		mockMvc.perform(post("/confirm")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(PaymentSuccessStatus.PAYMENT_CONFIRMED.getCode()))
			.andExpect(jsonPath("$.message").value(PaymentSuccessStatus.PAYMENT_CONFIRMED.getMessage()))
			.andExpect(jsonPath("$.result").value(resultMessage));

		verify(paymentService).confirmPayment(any(PaymentConfirmRequest.class),any());
	}

	@Test
	@DisplayName("결제 승인 - 서비스 에러")
	void confirmPayment_ServiceError() throws Exception {
		PaymentConfirmRequest request = new PaymentConfirmRequest(
			"test_payment_key",
			UUID.randomUUID().toString(),
			"10000"
		);

		when(paymentService.confirmPayment(any(PaymentConfirmRequest.class),any()))
			.thenThrow(new GeneralException(PaymentErrorStatus.PAYMENT_CONFIRM_FAILED));

		mockMvc.perform(post("/confirm")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.code").value(PaymentErrorStatus.PAYMENT_CONFIRM_FAILED.getCode()))
			.andExpect(jsonPath("$.message").value(PaymentErrorStatus.PAYMENT_CONFIRM_FAILED.getMessage()));
	}

	@Test
	@DisplayName("결제 실패 처리 - 성공")
	void processFail_Success() throws Exception {
		PaymentFailRequest request = new PaymentFailRequest(
			UUID.randomUUID().toString(),
			"INVALID_CARD",
			"유효하지 않은 카드입니다."
		);
		String resultMessage = "결제 실패 처리가 완료되었습니다.";

		when(paymentService.failSave(any(PaymentFailRequest.class)))
			.thenReturn(resultMessage);

		mockMvc.perform(post("/failsave")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(PaymentSuccessStatus.PAYMENT_FAIL_SAVED.getCode()))
			.andExpect(jsonPath("$.message").value(PaymentSuccessStatus.PAYMENT_FAIL_SAVED.getMessage()))
			.andExpect(jsonPath("$.result").value(resultMessage));

		verify(paymentService).failSave(any(PaymentFailRequest.class));
	}

	@Test
	@DisplayName("결제 취소 - 성공")
	void cancelPayment_Success() throws Exception {
		CancelPaymentRequest request = new CancelPaymentRequest(
			UUID.randomUUID(),
			"구매자가 취소를 원함"
		);
		String resultMessage = "결제 취소가 완료되었습니다.";

		when(paymentService.cancelPayment(any(CancelPaymentRequest.class),any()))
			.thenReturn(resultMessage);

		mockMvc.perform(post("/cancel")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(PaymentSuccessStatus.PAYMENT_CANCELLED.getCode()))
			.andExpect(jsonPath("$.message").value(PaymentSuccessStatus.PAYMENT_CANCELLED.getMessage()))
			.andExpect(jsonPath("$.result").value(resultMessage));

		verify(paymentService).cancelPayment(any(CancelPaymentRequest.class),any());
	}

	@Test
	@DisplayName("결제 실패 처리 - 서비스 에러")
	void processFail_ServiceError() throws Exception {
		PaymentFailRequest request = new PaymentFailRequest(
			UUID.randomUUID().toString(),
			"INVALID_CARD",
			"유효하지 않은 카드입니다."
		);

		when(paymentService.failSave(any(PaymentFailRequest.class)))
			.thenThrow(new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR));

		mockMvc.perform(post("/failsave")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.code").value(ErrorStatus._INTERNAL_SERVER_ERROR.getCode()))
			.andExpect(jsonPath("$.message").value(ErrorStatus._INTERNAL_SERVER_ERROR.getMessage()));
	}

	@Test
	@DisplayName("결제 취소 - 서비스 에러")
	void cancelPayment_ServiceError() throws Exception {
		CancelPaymentRequest request = new CancelPaymentRequest(
			UUID.randomUUID(),
			"구매자가 취소를 원함"
		);

		when(paymentService.cancelPayment(any(CancelPaymentRequest.class),any()))
			.thenThrow(new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR));

		mockMvc.perform(post("/cancel")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.code").value(ErrorStatus._INTERNAL_SERVER_ERROR.getCode()))
			.andExpect(jsonPath("$.message").value(ErrorStatus._INTERNAL_SERVER_ERROR.getMessage()));
	}

	@Test
	@DisplayName("결제 승인 - 주문 조회 실패")
	void confirmPayment_OrderNotFound() throws Exception {
		PaymentConfirmRequest request = new PaymentConfirmRequest(
			"test_payment_key",
			UUID.randomUUID().toString(),
			"10000"
		);

		when(paymentService.confirmPayment(any(PaymentConfirmRequest.class),any()))
			.thenThrow(new GeneralException(ErrorStatus.ORDER_NOT_FOUND));

		mockMvc.perform(post("/confirm")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value(ErrorStatus.ORDER_NOT_FOUND.getCode()))
			.andExpect(jsonPath("$.message").value(ErrorStatus.ORDER_NOT_FOUND.getMessage()));
	}

	@Test
	@DisplayName("결제 승인 - 결제 금액 불일치")
	void confirmPayment_AmountMismatch() throws Exception {
		PaymentConfirmRequest request = new PaymentConfirmRequest(
			"test_payment_key",
			UUID.randomUUID().toString(),
			"10000"
		);

		when(paymentService.confirmPayment(any(PaymentConfirmRequest.class),any()))
			.thenThrow(new GeneralException(PaymentErrorStatus.PAYMENT_AMOUNT_MISMATCH));

		mockMvc.perform(post("/confirm")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(PaymentErrorStatus.PAYMENT_AMOUNT_MISMATCH.getCode()))
			.andExpect(jsonPath("$.message").value(PaymentErrorStatus.PAYMENT_AMOUNT_MISMATCH.getMessage()));
	}

	@Test
	@DisplayName("결제 실패 처리 - 주문 조회 실패")
	void processFail_OrderNotFound() throws Exception {
		PaymentFailRequest request = new PaymentFailRequest(
			UUID.randomUUID().toString(),
			"INVALID_CARD",
			"유효하지 않은 카드입니다."
		);

		when(paymentService.failSave(any(PaymentFailRequest.class)))
			.thenThrow(new GeneralException(ErrorStatus.ORDER_NOT_FOUND));

		mockMvc.perform(post("/failsave")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value(ErrorStatus.ORDER_NOT_FOUND.getCode()))
			.andExpect(jsonPath("$.message").value(ErrorStatus.ORDER_NOT_FOUND.getMessage()));
	}

	@Test
	@DisplayName("결제 취소 - 주문 조회 실패")
	void cancelPayment_OrderNotFound() throws Exception {
		CancelPaymentRequest request = new CancelPaymentRequest(
			UUID.randomUUID(),
			"구매자가 취소를 원함"
		);

		when(paymentService.cancelPayment(any(CancelPaymentRequest.class),any()))
			.thenThrow(new GeneralException(ErrorStatus.ORDER_NOT_FOUND));

		mockMvc.perform(post("/cancel")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value(ErrorStatus.ORDER_NOT_FOUND.getCode()))
			.andExpect(jsonPath("$.message").value(ErrorStatus.ORDER_NOT_FOUND.getMessage()));
	}

	@Test
	@DisplayName("결제 취소 - 환불 불가능")
	void cancelPayment_NotRefundable() throws Exception {
		CancelPaymentRequest request = new CancelPaymentRequest(
			UUID.randomUUID(),
			"구매자가 취소를 원함"
		);

		when(paymentService.cancelPayment(any(CancelPaymentRequest.class),any()))
			.thenThrow(new GeneralException(PaymentErrorStatus.PAYMENT_NOT_REFUNDABLE));

		mockMvc.perform(post("/cancel")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(PaymentErrorStatus.PAYMENT_NOT_REFUNDABLE.getCode()))
			.andExpect(jsonPath("$.message").value(PaymentErrorStatus.PAYMENT_NOT_REFUNDABLE.getMessage()));
	}

	@Test
	@DisplayName("결제 취소 - 결제 정보 조회 실패")
	void cancelPayment_PaymentNotFound() throws Exception {
		CancelPaymentRequest request = new CancelPaymentRequest(
			UUID.randomUUID(),
			"구매자가 취소를 원함"
		);

		when(paymentService.cancelPayment(any(CancelPaymentRequest.class),any()))
			.thenThrow(new GeneralException(ErrorStatus.PAYMENT_NOT_FOUND));

		mockMvc.perform(post("/cancel")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value(ErrorStatus.PAYMENT_NOT_FOUND.getCode()))
			.andExpect(jsonPath("$.message").value(ErrorStatus.PAYMENT_NOT_FOUND.getMessage()));
	}

	@Test
	@DisplayName("결제 승인 - 토스 API 에러")
	void confirmPayment_TossApiError() throws Exception {
		PaymentConfirmRequest request = new PaymentConfirmRequest(
			"test_payment_key",
			UUID.randomUUID().toString(),
			"10000"
		);

		when(paymentService.confirmPayment(any(PaymentConfirmRequest.class),any()))
			.thenThrow(new GeneralException(PaymentErrorStatus.TOSS_API_ERROR));

		mockMvc.perform(post("/confirm")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.code").value(PaymentErrorStatus.TOSS_API_ERROR.getCode()))
			.andExpect(jsonPath("$.message").value(PaymentErrorStatus.TOSS_API_ERROR.getMessage()));
	}

	@Test
	@DisplayName("결제 취소 - 토스 API 에러")
	void cancelPayment_TossApiError() throws Exception {
		CancelPaymentRequest request = new CancelPaymentRequest(
			UUID.randomUUID(),
			"구매자가 취소를 원함"
		);

		when(paymentService.cancelPayment(any(CancelPaymentRequest.class),any()))
			.thenThrow(new GeneralException(PaymentErrorStatus.TOSS_API_ERROR));

		mockMvc.perform(post("/cancel")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.code").value(PaymentErrorStatus.TOSS_API_ERROR.getCode()))
			.andExpect(jsonPath("$.message").value(PaymentErrorStatus.TOSS_API_ERROR.getMessage()));
	}

	@Test
	@DisplayName("필수 파라미터 누락 - paymentKey 없음")
	void confirmPayment_MissingPaymentKey() throws Exception {
		String jsonWithoutPaymentKey = "{\"orderId\": \"" + UUID.randomUUID() + "\", \"amount\": \"10000\"}";

		mockMvc.perform(post("/confirm")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonWithoutPaymentKey))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(ErrorStatus._BAD_REQUEST.getCode()))
			.andExpect(jsonPath("$.message").value(ErrorStatus._BAD_REQUEST.getMessage()))
			.andExpect(jsonPath("$.result.paymentKey").value("결제 키는 필수입니다."));

		verify(paymentService, never()).confirmPayment(any(),any());
	}

	@Test
	@DisplayName("필수 파라미터 누락 - orderId 없음")
	void confirmPayment_MissingOrderId() throws Exception {
		String jsonWithoutOrderId = "{\"paymentKey\": \"test_key\", \"amount\": \"10000\"}";

		mockMvc.perform(post("/confirm")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonWithoutOrderId))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(ErrorStatus._BAD_REQUEST.getCode()))
			.andExpect(jsonPath("$.message").value(ErrorStatus._BAD_REQUEST.getMessage()))
			.andExpect(jsonPath("$.result.orderId").value("주문 ID는 필수입니다."));

		verify(paymentService, never()).confirmPayment(any(),any());
	}

	@Test
	@DisplayName("필수 파라미터 누락 - amount 없음")
	void confirmPayment_MissingAmount() throws Exception {
		String jsonWithoutAmount = "{\"paymentKey\": \"test_key\", \"orderId\": \"" + UUID.randomUUID() + "\"}";

		mockMvc.perform(post("/confirm")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonWithoutAmount))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(ErrorStatus._BAD_REQUEST.getCode()))
			.andExpect(jsonPath("$.message").value(ErrorStatus._BAD_REQUEST.getMessage()))
			.andExpect(jsonPath("$.result.amount").value("결제 금액은 필수입니다."));

		verify(paymentService, never()).confirmPayment(any(),any());
	}

	@Test
	@DisplayName("필수 파라미터 누락 - errorCode 없음")
	void processFail_MissingErrorCode() throws Exception {
		String jsonWithoutErrorCode = "{\"orderId\": \"" + UUID.randomUUID() + "\", \"message\": \"에러 메시지\"}";

		mockMvc.perform(post("/failsave")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonWithoutErrorCode))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(ErrorStatus._BAD_REQUEST.getCode()))
			.andExpect(jsonPath("$.message").value(ErrorStatus._BAD_REQUEST.getMessage()))
			.andExpect(jsonPath("$.result.errorCode").value("에러 코드는 필수입니다."));

		verify(paymentService, never()).failSave(any());
	}

	@Test
	@DisplayName("필수 파라미터 누락 - message 없음")
	void processFail_MissingMessage() throws Exception {
		String jsonWithoutMessage = "{\"orderId\": \"" + UUID.randomUUID() + "\", \"errorCode\": \"INVALID_CARD\"}";

		mockMvc.perform(post("/failsave")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonWithoutMessage))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(ErrorStatus._BAD_REQUEST.getCode()))
			.andExpect(jsonPath("$.message").value(ErrorStatus._BAD_REQUEST.getMessage()))
			.andExpect(jsonPath("$.result.message").value("실패 사유는 필수입니다."));

		verify(paymentService, never()).failSave(any());
	}

	@Test
	@DisplayName("필수 파라미터 누락 - orderId 없음 (cancel)")
	void cancelPayment_MissingOrderId() throws Exception {
		String jsonWithoutOrderId = "{\"cancelReason\": \"구매자 취소\"}";

		mockMvc.perform(post("/cancel")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonWithoutOrderId))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(ErrorStatus._BAD_REQUEST.getCode()))
			.andExpect(jsonPath("$.message").value(ErrorStatus._BAD_REQUEST.getMessage()))
			.andExpect(jsonPath("$.result.orderId").value("주문 ID는 필수입니다."));

		verify(paymentService, never()).cancelPayment(any(),any());
	}
}