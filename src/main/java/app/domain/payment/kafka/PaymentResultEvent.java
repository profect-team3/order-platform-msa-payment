package app.domain.payment.kafka;

import java.util.Map;
import org.springframework.context.ApplicationEvent;
import lombok.Getter;

@Getter
public class PaymentResultEvent extends ApplicationEvent {
	private final Long userId;
	private final Map<String, Object> headers;

	public PaymentResultEvent(Object source, String orderId, Long userId, Map<String, Object> headers) {
		super(source);
		this.userId = userId;
		this.headers = headers;
	}

}