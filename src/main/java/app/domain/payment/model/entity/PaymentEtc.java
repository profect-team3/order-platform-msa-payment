package app.domain.payment.model.entity;

import java.util.UUID;

import app.commonUtil.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_payment_etc")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class PaymentEtc extends BaseEntity {

	@Id
	@GeneratedValue
	private UUID paymentEtcId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id", nullable = false)
	private Payment payment;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String paymentResponse;

	public PaymentEtc(UUID paymentEtcId, Payment payment, String paymentResponse) {
		this.paymentEtcId = paymentEtcId;
		this.payment = payment;
		this.paymentResponse = paymentResponse;
	}
}