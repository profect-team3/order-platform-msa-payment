package app.domain.payment.kafka;

import app.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentRefundConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = "refund.request", groupId = "refund.consumer")
    public void handleRefundRequest(@Header("orderId") String orderId, @Header("userId") String userId) {
        log.info("Received refund request - orderId: {}, userId: {}", orderId, userId);
        try {
            String result = paymentService.cancelPaymentByUserId(UUID.fromString(orderId), Long.parseLong(userId));
            log.info("Refund processing completed: {}", result);
        } catch (Exception e) {
            log.error("Failed to process refund request for orderId: {}, userId: {}", orderId, userId, e);
        }
    }
}
