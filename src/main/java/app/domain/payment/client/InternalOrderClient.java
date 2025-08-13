package app.domain.payment.client;

import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import app.domain.payment.model.dto.request.OrderInfo;
import app.global.apiPayload.ApiResponse;

@Service
public class InternalOrderClient {
	private final RestTemplate restTemplate;

	public InternalOrderClient(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Value("${order.service.url:http://localhost:8084}")
	private String orderServiceUrl;


	public ApiResponse<OrderInfo> getOrderInfo(UUID orderId) {
		String url = orderServiceUrl+"/internal/order/"+orderId;

		ResponseEntity<ApiResponse<OrderInfo>> response = restTemplate.exchange(
			url,
			HttpMethod.GET,
			null,
			new ParameterizedTypeReference<ApiResponse<OrderInfo>>() {}
		);
		return response.getBody();
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
