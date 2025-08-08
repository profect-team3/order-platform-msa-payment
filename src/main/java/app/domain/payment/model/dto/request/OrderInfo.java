package app.domain.payment.model.dto.request;

import app.domain.payment.model.entity.enums.PaymentMethod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OrderInfo {
	private Long totalPrice;
	private String paymentMethod;
	private Boolean isRefundable;

	public PaymentMethod getPaymentMethodEnum() {
		return PaymentMethod.valueOf(paymentMethod);
	}
}
