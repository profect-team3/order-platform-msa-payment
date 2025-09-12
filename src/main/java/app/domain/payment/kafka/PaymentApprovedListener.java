package app.domain.payment.kafka;

import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentApprovedListener {

	private final PaymentApprovedProducer paymentApprovedProducer;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onAfterCommit(PaymentResultEvent e) {

		Map<String,Object> payload = Map.of("userId", e.getUserId());

		try {
			paymentApprovedProducer.sendPaymentApproved(e.getHeaders(), payload);
		} catch (Exception ex) {
			log.error("Kafka publish failed for orderId={} err={}", e.getHeaders().get("orderId"), ex.toString());
		}
	}
}
