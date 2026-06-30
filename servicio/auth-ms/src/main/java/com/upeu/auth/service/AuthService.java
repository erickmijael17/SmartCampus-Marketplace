package com.upeu.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upeu.auth.dto.AuthLoginRequest;
import com.upeu.auth.dto.AuthLoginResponse;
import com.upeu.auth.dto.AuthRegisterRequest;
import com.upeu.auth.dto.PersonaDto;
import com.upeu.auth.entity.Persona;
import com.upeu.auth.exception.PersonaNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${keycloak.token-url}")
    private String tokenUrl;

    @Value("${keycloak.admin-url}")
    private String adminUrl;

    @Value("${keycloak.admin-username}")
    private String adminUsername;

    @Value("${keycloak.admin-password}")
    private String adminPassword;

    private final PersonaService personaService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    public AuthLoginResponse login(AuthLoginRequest request) {
        return doLogin(request.getUsername(), request.getPassword());
    }

    @SuppressWarnings("unchecked")
    public AuthLoginResponse register(AuthRegisterRequest request) {
        String username = request.getUsername();
        if (username == null || username.isBlank()) {
            username = request.getEmail() != null ? request.getEmail() : "user_" + System.currentTimeMillis();
        }
        String password = request.getPassword();

        String adminToken = getAdminToken();

        createKeycloakUser(adminToken, username, password, request);

        AuthLoginResponse loginResponse = doLogin(username, password);

        String userId = extractUserIdFromToken(loginResponse.getAccessToken());
        if (userId != null) {
            try {
                personaService.findByUserId(userId);
            } catch (PersonaNotFoundException e) {
                PersonaDto.Request personaRequest = PersonaDto.Request.builder()
                        .nombres(request.getFullName() != null ? request.getFullName() : username)
                        .apellidos("")
                        .email(request.getEmail() != null ? request.getEmail() : username)
                        .tipoUsuario(Persona.TipoUsuario.ESTUDIANTE)
                        .build();
                personaService.create(userId, personaRequest);
            }
        }

        return loginResponse;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> me(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token no proporcionado");
        }

        String accessToken = authHeader.substring(7);

        try {
            Map<String, Object> claims = decodeToken(accessToken);
            String userId = (String) claims.get("sub");

            Map<String, Object> result = new HashMap<>();
            result.put("username", claims.get("preferred_username"));
            result.put("email", claims.get("email"));
            result.put("userId", userId);
            result.put("roles", extractRoles(claims));
            result.put("accessToken", accessToken);

            try {
                PersonaDto.Response persona = personaService.findByUserId(userId);
                result.put("persona", persona);
            } catch (PersonaNotFoundException e) {
                org.springframework.security.oauth2.jwt.Jwt jwt = createJwtFromClaims(accessToken, claims);
                PersonaDto.Response persona = personaService.createFromJwt(jwt);
                result.put("persona", persona);
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error al decodificar el token: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public PersonaDto.Response getProfile(String accessToken) {
        Map<String, Object> claims = decodeToken(accessToken);
        String userId = (String) claims.get("sub");

        try {
            return personaService.findByUserId(userId);
        } catch (PersonaNotFoundException e) {
            return personaService.createFromJwt(createJwtFromClaims(accessToken, claims));
        }
    }

    @SuppressWarnings("unchecked")
    public PersonaDto.Response updateProfile(String accessToken, PersonaDto.Request request) {
        Map<String, Object> claims = decodeToken(accessToken);
        String userId = (String) claims.get("sub");

        try {
            personaService.findByUserId(userId);
        } catch (PersonaNotFoundException e) {
            personaService.createFromJwt(createJwtFromClaims(accessToken, claims));
            return personaService.update(userId, request);
        }

        return personaService.update(userId, request);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> decodeToken(String accessToken) {
        try {
            String[] chunks = accessToken.split("\\.");
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payload = new String(decoder.decode(chunks[1]));
            return objectMapper.readValue(payload, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Error al decodificar el token: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractUserIdFromToken(String accessToken) {
        try {
            Map<String, Object> claims = decodeToken(accessToken);
            return (String) claims.get("sub");
        } catch (Exception e) {
            return null;
        }
    }

    private org.springframework.security.oauth2.jwt.Jwt createJwtFromClaims(String accessToken, Map<String, Object> claims) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("typ", "JWT");
        Map<String, Object> jwtClaims = new HashMap<>(claims);
        return new org.springframework.security.oauth2.jwt.Jwt(
                accessToken != null ? accessToken : "",
                null, null, headers, jwtClaims);
    }

    @SuppressWarnings("unchecked")
    private AuthLoginResponse doLogin(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("client_id", "marketplace-client");
        map.add("username", username);
        map.add("password", password);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, entity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                String accessToken = (String) body.get("access_token");
                String tokenType = (String) body.get("token_type");
                int expiresIn = (Integer) body.get("expires_in");

                String[] chunks = accessToken.split("\\.");
                Base64.Decoder decoder = Base64.getUrlDecoder();
                String payload = new String(decoder.decode(chunks[1]));
                Map<String, Object> claims = objectMapper.readValue(payload, Map.class);

                String preferredUsername = (String) claims.get("preferred_username");

                return AuthLoginResponse.builder()
                        .accessToken(accessToken)
                        .tokenType(tokenType)
                        .expiresIn(expiresIn)
                        .username(preferredUsername != null ? preferredUsername : username)
                        .roles(extractRoles(claims))
                        .build();
            }
        } catch (Exception e) {
            throw new RuntimeException("Autenticación fallida con Keycloak: " + e.getMessage(), e);
        }
        throw new RuntimeException("Autenticación fallida con Keycloak");
    }

    @SuppressWarnings("unchecked")
    private String getAdminToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("client_id", "admin-cli");
        map.add("username", adminUsername);
        map.add("password", adminPassword);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        String adminTokenUrl = adminUrl + "/realms/master/protocol/openid-connect/token";
        ResponseEntity<Map> response = restTemplate.postForEntity(adminTokenUrl, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return (String) response.getBody().get("access_token");
        }
        throw new RuntimeException("No se pudo obtener token de administrador de Keycloak");
    }

    @SuppressWarnings("unchecked")
    private void createKeycloakUser(String adminToken, String username, String password, AuthRegisterRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        Map<String, Object> userRepresentation = new HashMap<>();
        userRepresentation.put("username", username);
        userRepresentation.put("email", request.getEmail());
        userRepresentation.put("firstName", request.getFullName());
        userRepresentation.put("enabled", true);
        // emailVerified=true evita que Keycloak 25+ bloquee el login con VERIFY_PROFILE
        userRepresentation.put("emailVerified", true);

        Map<String, Object> credentials = new HashMap<>();
        credentials.put("type", "password");
        credentials.put("value", password);
        credentials.put("temporary", false);
        userRepresentation.put("credentials", Collections.singletonList(credentials));

        if (request.getUserType() != null) {
            Map<String, List<String>> attributes = new HashMap<>();
            attributes.put("userType", Collections.singletonList(request.getUserType()));
            attributes.put("career", request.getCareer() != null
                    ? Collections.singletonList(request.getCareer())
                    : Collections.singletonList(""));
            attributes.put("cycle", request.getCycle() != null
                    ? Collections.singletonList(request.getCycle())
                    : Collections.singletonList(""));
            userRepresentation.put("attributes", attributes);
        }

        String usersUrl = adminUrl + "/admin/realms/smartcampus/users";
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(userRepresentation, headers);

        try {
            restTemplate.postForEntity(usersUrl, entity, String.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Ya existe una cuenta registrada con ese correo o usuario."
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Keycloak rechazo la creacion del usuario: " + e.getStatusText(),
                    e
            );
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRoles(Map<String, Object> claims) {
        List<String> roles = new ArrayList<>();
        Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            roles = (List<String>) realmAccess.get("roles");
        }
        return roles;
    }
}
