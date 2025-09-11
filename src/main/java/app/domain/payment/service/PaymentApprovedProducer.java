package app.domain.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentApprovedProducer {
	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	public void sendPaymentApproved(Map<String, Object> headers, Map<String,Object> payload) {
		try {
			String payloadJson = objectMapper.writeValueAsString(payload);

			ProducerRecord<String, String> record = new ProducerRecord<>("payment.result", payloadJson);
			record.headers().add(new RecordHeader("eventType", headers.get("eventType").toString().getBytes()));
			if (headers.get("orderId") != null) {
				record.headers().add(new RecordHeader("orderId", headers.get("orderId").toString().getBytes()));
			}

			kafkaTemplate.send(record);
		} catch (JsonProcessingException e) {
			log.error("Could not serialize payload for orderId: {}", headers.get("orderId"), e);
		}
	}
}