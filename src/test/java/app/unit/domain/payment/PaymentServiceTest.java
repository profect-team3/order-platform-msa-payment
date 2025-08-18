// package app.unit.domain.payment;
//
// import static org.assertj.core.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;
//
// import java.time.LocalDateTime;
// import java.util.Optional;
// import java.util.UUID;
//
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.Spy;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.test.util.ReflectionTestUtils;
//
// import app.domain.payment.service.PaymentService;
// import app.domain.payment.client.InternalOrderClient;
// import app.domain.payment.model.dto.request.OrderInfo;
// import app.domain.payment.model.entity.enums.PaymentMethod;
//
// import app.domain.payment.model.dto.request.CancelPaymentRequest;
// import app.domain.payment.model.dto.request.PaymentConfirmRequest;
// import app.domain.payment.model.dto.request.PaymentFailRequest;
// import app.domain.payment.model.entity.Payment;
// import app.domain.payment.model.entity.PaymentEtc;
// import app.domain.payment.model.entity.enums.PaymentStatus;
// import app.domain.payment.model.repository.PaymentEtcRepository;
// import app.domain.payment.model.repository.PaymentRepository;
// import app.domain.payment.status.PaymentErrorStatus;
// import app.global.apiPayload.ApiResponse;
// import app.global.apiPayload.code.status.ErrorStatus;
// import app.global.apiPayload.exception.GeneralException;
//
// @ExtendWith(MockitoExtension.class)
// @DisplayName("PaymentService 단위 테스트 (MSA 전환 반영)")
// class PaymentServiceTest {
//
// 	@Mock
// 	private PaymentRepository paymentRepository;
// 	@Mock
// 	private PaymentEtcRepository paymentEtcRepository;
// 	@Mock
// 	private InternalOrderClient internalOrderClient;
//
// 	@Spy
// 	@InjectMocks
// 	private PaymentService paymentService;
//
// 	private UUID orderId;
// 	private UUID storeId;
// 	private Long userId;
// 	private PaymentConfirmRequest confirmRequest;
// 	private PaymentFailRequest failRequest;
// 	private CancelPaymentRequest cancelRequest;
// 	private Payment payment;
// 	private OrderInfo orderInfo;
//
// 	@BeforeEach
// 	void setUp() {
// 		orderId = UUID.randomUUID();
// 		storeId= UUID.randomUUID();
//
// 		userId = 1L;
//
// 		ReflectionTestUtils.setField(paymentService, "tossSecretKey", "test_secret_key");
// 		ReflectionTestUtils.setField(paymentService, "tossUrl", "https://api.tosspayments.com/v1/payments");
//
// 		confirmRequest = new PaymentConfirmRequest(
// 			"test_payment_key",
// 			orderId.toString(),
// 			"10000"
// 		);
//
// 		failRequest = new PaymentFailRequest(String.valueOf(orderId), "INVALID_CARD", "유효하지 않은 카드입니다.");
//
// 		cancelRequest = new CancelPaymentRequest(orderId, "구매자가 취소를 원함");
//
// 		orderInfo = new OrderInfo(orderId,storeId,userId,10000L,"PENDING", LocalDateTime.now(),"CREDIT_CARD",true);
//
// 		payment = Payment.builder()
// 			.paymentId(UUID.randomUUID())
// 			.ordersId(orderId)
// 			.paymentKey("test_payment_key")
// 			.paymentMethod(PaymentMethod.CREDIT_CARD)
// 			.paymentStatus(PaymentStatus.COMPLETED)
// 			.amount(10000L)
// 			.build();
// 	}
//
// 	@Test
// 	@DisplayName("결제 승인 성공")
// 	void confirmPayment_Success() {
// 		// Given
// 		when(internalOrderClient.getOrderInfo(orderId)).thenReturn(new ApiResponse<>(true, "200", "성공", orderInfo));
// 		when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
// 		when(paymentEtcRepository.save(any(PaymentEtc.class))).thenReturn(mock(PaymentEtc.class));
// 		when(internalOrderClient.clearCartItems(userId)).thenReturn(new ApiResponse<>(true, "200", "성공", "장바구니 비우기 성공"));
// 		doReturn("success:{\"status\":\"DONE\"}").when(paymentService).callTossConfirmApi(any(PaymentConfirmRequest.class), any(Long.class));
//
// 		// When
// 		String result = paymentService.confirmPayment(confirmRequest, userId);
//
// 		// Then
// 		assertThat(result).contains("결제 승인이 완료되었습니다");
// 		verify(internalOrderClient).getOrderInfo(orderId);
// 		verify(paymentRepository).save(any(Payment.class));
// 		verify(paymentEtcRepository).save(any(PaymentEtc.class));
// 		verify(internalOrderClient).clearCartItems(userId);
// 	}
//
// 	@Test
// 	@DisplayName("결제 승인 실패 - API 호출 실패")
// 	void confirmPayment_ApiCallFailed() {
// 		// Given
// 		when(internalOrderClient.getOrderInfo(orderId)).thenReturn(new ApiResponse<>(true, "200", "성공", orderInfo));
// 		when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
// 		when(paymentEtcRepository.save(any(PaymentEtc.class))).thenReturn(mock(PaymentEtc.class));
// 		doReturn("fail:{\"code\":\"INVALID_REQUEST\",\"message\":\"Invalid request\"}").when(paymentService).callTossConfirmApi(any(PaymentConfirmRequest.class), any(Long.class));
//
// 		// When & Then
// 		assertThatThrownBy(() -> paymentService.confirmPayment(confirmRequest, userId))
// 			.isInstanceOf(GeneralException.class)
// 			.satisfies(ex -> {
// 				GeneralException generalEx = (GeneralException)ex;
// 				assertThat(generalEx.getErrorReason().getCode()).isEqualTo(
// 					PaymentErrorStatus.PAYMENT_CONFIRM_FAILED.getCode());
// 			});
//
// 		verify(paymentRepository).save(any(Payment.class));
// 		verify(paymentEtcRepository).save(any(PaymentEtc.class));
// 		verify(internalOrderClient, never()).clearCartItems(userId);
// 	}
//
// 	@Test
// 	@DisplayName("결제 승인 실패 - 주문을 찾을 수 없음")
// 	void confirmPayment_OrderNotFound() {
// 		// Given
// 		when(internalOrderClient.getOrderInfo(orderId)).thenThrow(new GeneralException(ErrorStatus.ORDER_NOT_FOUND));
//
// 		// When & Then
// 		assertThatThrownBy(() -> paymentService.confirmPayment(confirmRequest, userId))
// 			.isInstanceOf(GeneralException.class)
// 			.satisfies(ex -> {
// 				GeneralException generalEx = (GeneralException)ex;
// 				assertThat(generalEx.getErrorReason().getCode()).isEqualTo(ErrorStatus.ORDER_NOT_FOUND.getCode());
// 			});
//
// 		verify(internalOrderClient).getOrderInfo(orderId);
// 		verify(paymentRepository, never()).save(any());
// 	}
//
// 	@Test
// 	@DisplayName("결제 승인 실패 - 금액 불일치")
// 	void confirmPayment_AmountMismatch() {
// 		// Given
// 		PaymentConfirmRequest wrongAmountRequest = new PaymentConfirmRequest(
// 			"test_payment_key",
// 			orderId.toString(),
// 			"20000"
// 		);
// 		when(internalOrderClient.getOrderInfo(orderId)).thenReturn(new ApiResponse<>(true, "200", "성공", orderInfo));
//
// 		// When & Then
// 		assertThatThrownBy(() -> paymentService.confirmPayment(wrongAmountRequest, userId))
// 			.isInstanceOf(GeneralException.class)
// 			.satisfies(ex -> {
// 				GeneralException generalEx = (GeneralException)ex;
// 				assertThat(generalEx.getErrorReason().getCode()).isEqualTo(
// 					PaymentErrorStatus.PAYMENT_AMOUNT_MISMATCH.getCode());
// 			});
//
// 		verify(internalOrderClient).getOrderInfo(orderId);
// 		verify(paymentRepository, never()).save(any());
// 	}
//
// 	@Test
// 	@DisplayName("결제 실패 처리 성공")
// 	void failSave_Success() {
// 		// Given
// 		when(internalOrderClient.updateOrderStatus(orderId,"FAILED")).thenReturn(new ApiResponse<>(true, "200", "성공", "주문 상태 변경에 성공"));
//
// 		// When
// 		String result = paymentService.failSave(failRequest);
//
// 		// Then
// 		assertThat(result).isEqualTo("결제 실패 처리가 완료되었습니다.");
// 		verify(internalOrderClient).updateOrderStatus(orderId, "FAILED");
// 	}
//
// 	@Test
// 	@DisplayName("결제 실패 처리 실패 - 주문을 찾을 수 없음")
// 	void failSave_OrderNotFound() {
// 		// Given
// 		doThrow(new GeneralException(ErrorStatus.ORDER_NOT_FOUND))
// 			.when(internalOrderClient).updateOrderStatus(orderId, "FAILED");
//
// 		// When & Then
// 		assertThatThrownBy(() -> paymentService.failSave(failRequest))
// 			.isInstanceOf(GeneralException.class)
// 			.satisfies(ex -> {
// 				GeneralException generalEx = (GeneralException)ex;
// 				assertThat(generalEx.getErrorReason().getCode()).isEqualTo(ErrorStatus.ORDER_NOT_FOUND.getCode());
// 			});
//
// 		verify(internalOrderClient).updateOrderStatus(orderId, "FAILED");
// 	}
//
// 	@Test
// 	@DisplayName("결제 취소 성공")
// 	void cancelPayment_Success() {
// 		// Given
// 		when(internalOrderClient.getOrderInfo(orderId)).thenReturn(new ApiResponse<>(true, "200", "성공", orderInfo));
// 		when(paymentRepository.findByOrdersId(orderId)).thenReturn(Optional.of(payment));
// 		when(paymentEtcRepository.save(any(PaymentEtc.class))).thenReturn(mock(PaymentEtc.class));
// 		doReturn("success:{\"status\":\"CANCELED\"}").when(paymentService)
// 			.callTossCancelApi(anyString(), anyString(), any(Long.class), any(UUID.class));
// 		when(internalOrderClient.updateOrderStatus(orderId,"REFUNDED")).thenReturn(new ApiResponse<>(true, "200", "성공", "주문 상태 변경에 성공"));
// 		when(internalOrderClient.addOrderHistory(orderId,"cancel")).thenReturn(new ApiResponse<>(true, "200", "성공", "주문 historty 추가에 성공"));
//
//
// 		// When
// 		String result = paymentService.cancelPayment(cancelRequest, userId);
//
// 		// Then
// 		assertThat(result).isEqualTo("결제 취소가 완료되었습니다.");
// 		verify(internalOrderClient).getOrderInfo(orderId);
// 		verify(paymentRepository).findByOrdersId(orderId);
// 		verify(paymentEtcRepository).save(any(PaymentEtc.class));
// 		verify(internalOrderClient).updateOrderStatus(orderId, "REFUNDED");
// 		verify(internalOrderClient).addOrderHistory(orderId, "cancel");
// 	}
//
// 	@Test
// 	@DisplayName("결제 취소 실패 - API 호출 실패")
// 	void cancelPayment_ApiCallFailed() {
// 		// Given
// 		when(internalOrderClient.getOrderInfo(orderId)).thenReturn(new ApiResponse<>(true, "200", "성공", orderInfo));
// 		when(paymentRepository.findByOrdersId(orderId)).thenReturn(Optional.of(payment));
// 		when(paymentEtcRepository.save(any(PaymentEtc.class))).thenReturn(mock(PaymentEtc.class));
// 		doReturn("fail:{\"code\":\"CANCEL_FAILED\",\"message\":\"Cancel failed\"}").when(paymentService)
// 			.callTossCancelApi(anyString(), anyString(), any(Long.class), any(UUID.class));
//
// 		// When & Then
// 		assertThatThrownBy(() -> paymentService.cancelPayment(cancelRequest, userId))
// 			.isInstanceOf(GeneralException.class)
// 			.satisfies(ex -> {
// 				GeneralException generalEx = (GeneralException)ex;
// 				assertThat(generalEx.getErrorReason().getCode()).isEqualTo(
// 					ErrorStatus._INTERNAL_SERVER_ERROR.getCode());
// 			});
//
// 		verify(paymentEtcRepository).save(any(PaymentEtc.class));
// 	}
//
// 	@Test
// 	@DisplayName("결제 취소 실패 - 주문을 찾을 수 없음")
// 	void cancelPayment_OrderNotFound() {
// 		// Given
// 		when(internalOrderClient.getOrderInfo(orderId)).thenThrow(new GeneralException(ErrorStatus.ORDER_NOT_FOUND));
//
// 		// When & Then
// 		assertThatThrownBy(() -> paymentService.cancelPayment(cancelRequest, userId))
// 			.isInstanceOf(GeneralException.class)
// 			.satisfies(ex -> {
// 				GeneralException generalEx = (GeneralException)ex;
// 				assertThat(generalEx.getErrorReason().getCode()).isEqualTo(ErrorStatus.ORDER_NOT_FOUND.getCode());
// 			});
//
// 		verify(internalOrderClient).getOrderInfo(orderId);
// 		verify(paymentRepository, never()).save(any());
// 	}
//
// 	@Test
// 	@DisplayName("결제 취소 실패 - 결제 정보를 찾을 수 없음")
// 	void cancelPayment_PaymentNotFound() {
// 		// Given
// 		when(internalOrderClient.getOrderInfo(orderId)).thenReturn(new ApiResponse<>(true, "200", "성공", orderInfo));
// 		when(paymentRepository.findByOrdersId(orderId)).thenReturn(Optional.empty());
//
// 		// When & Then
// 		assertThatThrownBy(() -> paymentService.cancelPayment(cancelRequest, userId))
// 			.isInstanceOf(GeneralException.class)
// 			.satisfies(ex -> {
// 				GeneralException generalEx = (GeneralException)ex;
// 				assertThat(generalEx.getErrorReason().getCode()).isEqualTo(ErrorStatus.PAYMENT_NOT_FOUND.getCode());
// 			});
//
// 		verify(internalOrderClient).getOrderInfo(orderId);
// 		verify(paymentRepository).findByOrdersId(orderId);
// 		verify(paymentRepository, never()).save(any());
// 	}
//
// 	@Test
// 	@DisplayName("결제 취소 실패 - 환불 불가능")
// 	void cancelPayment_NotRefundable() {
// 		// Given
// 		OrderInfo nonRefundableOrderInfo = new OrderInfo(orderId,storeId,userId,10000L,"PENDING", LocalDateTime.now(),"CREDIT_CARD",false);
// 		when(internalOrderClient.getOrderInfo(orderId)).thenReturn(new ApiResponse<>(true, "200", "성공", nonRefundableOrderInfo));
//
// 		// When & Then
// 		assertThatThrownBy(() -> paymentService.cancelPayment(cancelRequest, userId))
// 			.isInstanceOf(GeneralException.class)
// 			.satisfies(ex -> {
// 				GeneralException generalEx = (GeneralException)ex;
// 				assertThat(generalEx.getErrorReason().getCode()).isEqualTo(
// 					PaymentErrorStatus.PAYMENT_NOT_REFUNDABLE.getCode());
// 			});
//
// 		verify(internalOrderClient).getOrderInfo(orderId);
// 		verify(paymentRepository, never()).findByOrdersId(any());
// 	}
// }