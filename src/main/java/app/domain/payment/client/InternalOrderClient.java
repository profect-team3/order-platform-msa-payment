package app.domain.payment.client;

import java.util.List;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import app.domain.payment.model.dto.request.OrderInfo;
import app.global.apiPayload.ApiResponse;

@Component
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

	public ApiResponse<String> updateOrderStatus(UUID orderId, String orderStatus) {
		String url = orderServiceUrl+"/internal/order/"+orderId+"/status";

		HttpEntity<String> requestEntity = new HttpEntity<>(orderStatus);
		ResponseEntity<ApiResponse<String>> response = restTemplate.exchange(
			url,
			HttpMethod.POST,
			requestEntity,
			new ParameterizedTypeReference<ApiResponse<String>>() {}
		);
		return response.getBody();
	}

	public ApiResponse<String> addOrderHistory(UUID orderId, String state) {
		String url = orderServiceUrl+"/internal/order/"+orderId+"/history";
		ResponseEntity<ApiResponse<String>> response = restTemplate.exchange(
			url,
			HttpMethod.POST,
			null,
			new ParameterizedTypeReference<ApiResponse<String>>() {}
		);
		return response.getBody();
	}

	public ApiResponse<String> clearCartItems(Long userId) {
		String url = orderServiceUrl+"/internal/order/cart/"+userId;
		ResponseEntity<ApiResponse<String>> response = restTemplate.exchange(
			url,
			HttpMethod.DELETE,
			null,
			new ParameterizedTypeReference<ApiResponse<String>>() {}
		);
		return response.getBody();
	}
}
