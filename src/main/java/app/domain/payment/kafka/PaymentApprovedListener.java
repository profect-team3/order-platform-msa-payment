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
	public void onAfterCommit(Map<String,Object> event) {

		Map<String, Object> headers =(Map<String, Object>) event.get("headers");
		Map<String, Object> payload = new HashMap<>();
		payload.put("userId",headers.get("userId"));

		try {
			paymentApprovedProducer.sendPaymentApproved(headers, payload);
		} catch (Exception ex) {
			log.error("Kafka publish failed for orderId={} err={}", event.get("orderId"), ex.toString());
		}
	}
}
