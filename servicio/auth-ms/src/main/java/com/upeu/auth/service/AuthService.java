package com.upeu.auth.service;

import com.upeu.auth.dto.AuthLoginRequest;
import com.upeu.auth.dto.AuthLoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${keycloak.token-url}")
    private String tokenUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    public AuthLoginResponse login(AuthLoginRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("client_id", "marketplace-client");
        map.add("username", request.getUsername());
        map.add("password", request.getPassword());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, entity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                String accessToken = (String) body.get("access_token");
                String tokenType = (String) body.get("token_type");
                int expiresIn = (Integer) body.get("expires_in");

                // Parse JWT to extract roles and username
                String[] chunks = accessToken.split("\\.");
                Base64.Decoder decoder = Base64.getUrlDecoder();
                String payload = new String(decoder.decode(chunks[1]));
                Map<String, Object> claims = objectMapper.readValue(payload, Map.class);

                String username = (String) claims.get("preferred_username");
                
                List<String> roles = new ArrayList<>();
                Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");
                if (realmAccess != null && realmAccess.containsKey("roles")) {
                    roles = (List<String>) realmAccess.get("roles");
                }

                return AuthLoginResponse.builder()
                        .accessToken(accessToken)
                        .tokenType(tokenType)
                        .expiresIn(expiresIn)
                        .username(username != null ? username : request.getUsername())
                        .roles(roles)
                        .build();
            }
        } catch (Exception e) {
            throw new RuntimeException("Autenticación fallida con Keycloak: " + e.getMessage(), e);
        }
        throw new RuntimeException("Autenticación fallida con Keycloak");
    }
}
