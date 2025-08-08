package app.domain.payment.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class PaymentFailRequest {

	@NotNull(message = "주문 ID는 필수입니다.")
	private String orderId;

	@NotBlank(message = "에러 코드는 필수입니다.")
	private String errorCode;

	@NotBlank(message = "실패 사유는 필수입니다.")
	private String message;

	public PaymentFailRequest(String orderId, String errorCode, String message) {
		this.orderId = orderId;
		this.errorCode = errorCode;
		this.message = message;
	}
}