package com.upeu.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.cloud.config.enabled=false",
		"eureka.client.enabled=false",
		"spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/.well-known/jwks.json",
		"jwt.secret=test",
		"jwt.issuer=gateway-test"
})
class GatewayApplicationTests {

	@Test
	void contextLoads() {
	}

}
