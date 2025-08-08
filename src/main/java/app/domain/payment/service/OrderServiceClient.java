package app.domain.payment.service;

import java.util.UUID;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import app.domain.payment.model.dto.request.OrderInfo;
import lombok.RequiredArgsConstructor;

@Service
public class OrderServiceClient {
	private final RestTemplate restTemplate;

	public OrderServiceClient(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Value("${order.service.url:http://localhost:8084}")
	private String orderServiceUrl;

	// 1. 주문 데이터 존재 여부 확인
	public boolean isOrderExists(UUID orderId) {
		String url = orderServiceUrl+"/internal/order"+orderId+"/exists";
		Boolean exists = restTemplate.getForObject(url, Boolean.class);
		return Boolean.TRUE.equals(exists);
	}

	// 2. 주문 정보 조회
	public OrderInfo getOrderInfo(UUID orderId) {
		String url = orderServiceUrl+"/internal/order/"+orderId;
		return restTemplate.getForObject(url, OrderInfo.class);
	}

	// 3. 주문 상태 변경
	public void updateOrderStatus(UUID orderId, String orderStatus) {
		String url = orderServiceUrl+"/internal/order/"+orderId+"/status";
		// requestBody가 단순 String 이므로 HttpEntity로 감싸서 보내기
		HttpEntity<String> request = new HttpEntity<>(orderStatus);
		restTemplate.postForObject(url, request, Void.class);
	}

	// 4. 주문 히스토리 추가
	public void addOrderHistory(UUID orderId, String state) {
		String url = orderServiceUrl+"/internal/order/"+orderId+"/history";
		HttpEntity<String> request = new HttpEntity<>(state);
		restTemplate.postForObject(url, request, Void.class);
	}

	// 5. 장바구니 비우기
	public void clearOrderCartItems(Long userId) {
		String url = orderServiceUrl+"/internal/order/cart/"+userId;
		restTemplate.delete(url);
	}
}
