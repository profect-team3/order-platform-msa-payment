package app.domain.payment.client;

import java.util.UUID;

import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import app.domain.payment.model.dto.request.OrderInfo;

@Service
public class InternalOrderClient {
	private final RestTemplate restTemplate;

	public InternalOrderClient(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Value("${order.service.url:http://localhost:8084}")
	private String orderServiceUrl;

	public boolean isOrderExists(UUID orderId) {
		String url = orderServiceUrl+"/internal/order/"+orderId+"/exists";
		Boolean exists = restTemplate.getForObject(url, Boolean.class);
		return Boolean.TRUE.equals(exists);
	}

	public OrderInfo getOrderInfo(UUID orderId) {
		String url = orderServiceUrl+"/internal/order/"+orderId;
		return restTemplate.getForObject(url, OrderInfo.class);
	}

	public void updateOrderStatus(UUID orderId, String orderStatus) {
		String url = orderServiceUrl+"/internal/order/"+orderId+"/status";
		HttpEntity<String> request = new HttpEntity<>(orderStatus);
		restTemplate.postForObject(url, request, Void.class);
	}

	public void addOrderHistory(UUID orderId, String state) {
		String url = orderServiceUrl+"/internal/order/"+orderId+"/history";
		HttpEntity<String> request = new HttpEntity<>(state);
		restTemplate.postForObject(url, request, Void.class);
	}

	public void clearOrderCartItems(Long userId) {
		String url = orderServiceUrl+"/internal/order/cart/"+userId;
		restTemplate.delete(url);
	}
}
