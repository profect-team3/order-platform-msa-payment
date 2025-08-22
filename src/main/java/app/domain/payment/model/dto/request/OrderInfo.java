package app.domain.payment.model.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;


import app.domain.payment.model.entity.enums.PaymentMethod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OrderInfo {

	private UUID orderId;
	private UUID storeId;
	private Long customerId;
	private Long totalPrice;
	private String orderStatus;
	private LocalDateTime orderedAt;
	private String paymentMethod;
	private Boolean isRefundable;

	public OrderInfo(UUID orderId, UUID storeId,Long customerId,Long totalPrice,String orderStatus,LocalDateTime orderedAt,String paymentMethod, Boolean isRefundable){
		this.orderId=orderId;
		this.storeId=storeId;
		this.customerId=customerId;
		this.totalPrice=totalPrice;
		this.orderStatus=orderStatus;
		this.orderedAt=orderedAt;
		this.paymentMethod=paymentMethod;
		this.isRefundable=isRefundable;

	}
	public PaymentMethod getPaymentMethodEnum() {
		return PaymentMethod.valueOf(paymentMethod);
	}
}
