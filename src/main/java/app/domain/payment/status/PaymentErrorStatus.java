package app.domain.payment.status;

import org.springframework.http.HttpStatus;

import app.global.apiPayload.code.BaseCode;
import app.global.apiPayload.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentErrorStatus implements BaseCode {

	PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "PAYMENT001", "결제 승인 정보가 결제 요청할 때의 정보와 다릅니다."),
	PAYMENT_CONFIRM_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT002", "결제 승인에 실패했습니다."),
	TOSS_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT003", "토스페이먼츠 API 오류가 발생했습니다."),
	PAYMENT_NOT_REFUNDABLE(HttpStatus.BAD_REQUEST, "PAYMENT004", "환불이 불가능한 주문입니다."),
	PAYMENT_CANCEL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT005", "결제 취소에 실패했습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

	@Override
	public ReasonDTO getReason() {
		return ReasonDTO.builder()
			.message(message)
			.code(code)
			.build();
	}

	@Override
	public ReasonDTO getReasonHttpStatus() {
		return ReasonDTO.builder()
			.message(message)
			.code(code)
			.httpStatus(httpStatus)
			.build();
	}
}