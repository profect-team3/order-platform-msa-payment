package app.global.config;

import java.time.Duration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import app.commonUtil.security.TokenPrincipalParser;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

	private final TokenPrincipalParser tokenPrincipalParser;

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder, ObjectMapper objectMapper) {
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setObjectMapper(objectMapper);

		return builder
			.additionalInterceptors((req, body, ex) -> {
				tokenPrincipalParser.tryGetAccessToken()
					.ifPresent(token -> req.getHeaders().setBearerAuth(token));
				return ex.execute(req, body);
			})
			.connectTimeout(Duration.ofSeconds(5))
			.additionalMessageConverters(converter)
			.build();
	}
}
