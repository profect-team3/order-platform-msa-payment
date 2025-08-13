package app.domain.payment.model.dto.request;

import org.aspectj.weaver.ast.Or;

import app.domain.payment.model.entity.enums.PaymentMethod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OrderInfo {
	private Long totalPrice;
	private String paymentMethod;
	private Boolean isRefundable;

	public OrderInfo(Long totalPrice,String paymentMethod, Boolean isRefundable){
		this.totalPrice=totalPrice;
		this.paymentMethod=paymentMethod;
		this.isRefundable=isRefundable;

	}
	public PaymentMethod getPaymentMethodEnum() {
		return PaymentMethod.valueOf(paymentMethod);
	}
}
