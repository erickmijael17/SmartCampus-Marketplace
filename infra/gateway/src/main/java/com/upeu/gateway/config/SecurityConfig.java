package com.upeu.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;
import org.springframework.web.cors.reactive.CorsConfigurationSource;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http,
                                                     CorsConfigurationSource corsConfigurationSource) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll()
                        .pathMatchers("/auth/**").permitAll()
                        .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .pathMatchers("/*/swagger-ui/**", "/*/v3/api-docs/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/categorias/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/productos/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/publicaciones", "/api/v1/publicaciones/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/media", "/api/v1/media/**").permitAll()
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        return jwt -> {
            Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
            return Mono.just(new JwtAuthenticationToken(jwt, authorities));
        };
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null || realmAccess.isEmpty()) {
            return Collections.emptyList();
        }

        Collection<String> roles = (Collection<String>) realmAccess.get("roles");
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }

        return roles.stream()
                .map(roleName -> new SimpleGrantedAuthority("ROLE_" + roleName))
                .collect(Collectors.toList());
    }
}

