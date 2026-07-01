package com.upeu.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
		"spring.cloud.config.enabled=false",
		"eureka.client.enabled=false",
		"spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/.well-known/jwks.json",
		"jwt.secret=test",
		"jwt.issuer=gateway-test"
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayApplicationTests {

	@LocalServerPort
	int port;

	@Autowired
	WebClient.Builder webClientBuilder;

	@Test
	void contextLoads() {
	}

	@Test
	void mediaFilesCanBeReadWithoutJwt() {
		assertAnonymousGetIsNotUnauthorized("/api/v1/media/files/foto.jpg");
	}

	@Test
	void publicCatalogMetadataCanBeReadWithoutJwt() {
		assertAnonymousGetIsNotUnauthorized("/api/v1/publicaciones");
		assertAnonymousGetIsNotUnauthorized("/api/v1/media");
	}

	private void assertAnonymousGetIsNotUnauthorized(String path) {
		WebClient.ResponseSpec response = webClientBuilder
				.baseUrl("http://localhost:" + port)
				.build()
				.get()
				.uri(path)
				.retrieve();

		response.toBodilessEntity()
				.onErrorResume(org.springframework.web.reactive.function.client.WebClientResponseException.class, error -> {
					assertThat(error.getStatusCode().value()).isNotEqualTo(401);
					return reactor.core.publisher.Mono.empty();
				})
				.block();
	}

}
