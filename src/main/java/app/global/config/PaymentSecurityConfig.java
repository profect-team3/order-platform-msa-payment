package app.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class PaymentSecurityConfig {

	@Bean
	@Order(0)
	public SecurityFilterChain paymentFilterChain(HttpSecurity http) throws Exception {
		http
			.securityMatcher("/checkout", "/success", "/fail", "/style.css")
			.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
			.csrf(AbstractHttpConfigurer::disable);

		return http.build();
	}
}
