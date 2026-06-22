package com.upeu.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.cloud.config.enabled=false",
		"eureka.client.enabled=false",
		"spring.datasource.url=jdbc:h2:mem:auth_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=none",
		"spring.flyway.enabled=true",
		"keycloak.token-url=http://localhost/token",
		"jwt.secret=test",
		"jwt.issuer=auth-test"
})
class AuthApplicationTests {

	@Test
	void contextLoads() {
	}

}
