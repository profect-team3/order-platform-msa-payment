package app.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class RestTemplateConfig {
	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		mapper.configure(com.fasterxml.jackson.databind.MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);

		mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);

		return mapper;
	}


	@Bean
	public RestTemplate restTemplate(ObjectMapper objectMapper) {
		MappingJackson2HttpMessageConverter converter =
			new MappingJackson2HttpMessageConverter();
		converter.setObjectMapper(objectMapper);

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(0, converter);
		return restTemplate;
	}
}
