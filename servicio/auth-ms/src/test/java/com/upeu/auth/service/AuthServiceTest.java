package com.upeu.auth.service;

import com.upeu.auth.dto.AuthLoginRequest;
import com.upeu.auth.exception.PersonaNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

class AuthServiceTest {

    @Test
    void loginReturnsUnauthorizedWhenKeycloakRejectsCredentials() {
        PersonaService personaService = mock(PersonaService.class);
        AuthService authService = new AuthService(personaService);
        ReflectionTestUtils.setField(authService, "tokenUrl", "http://keycloak/realms/smartcampus/protocol/openid-connect/token");
        when(personaService.findByEmailStartingWith("user@test.com"))
                .thenThrow(new PersonaNotFoundException("Persona no encontrada"));

        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(authService, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://keycloak/realms/smartcampus/protocol/openid-connect/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withUnauthorizedRequest());

        AuthLoginRequest request = new AuthLoginRequest();
        request.setUsername("user@test.com");
        request.setPassword("wrong-password");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException response = (ResponseStatusException) exception;
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(response.getReason()).isEqualTo("Usuario o contrasena incorrectos en Keycloak.");
                });

        server.verify();
    }
}
