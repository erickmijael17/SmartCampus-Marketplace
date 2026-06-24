# Reporte de correccion auth-ms, Keycloak y Gateway

Fecha: 2026-06-22

## Errores encontrados

- `auth-ms` no compilaba por `flyway-database-postgresql` sin version explicita.
- `auth-ms` referenciaba `JwtProperties` desde `AuthApplication`, pero la clase no existia.
- `DataInitializer` inyectaba `PasswordEncoder`, pero no habia bean configurado.
- Los `pom.xml` usaban Spring Cloud `2025.0.x`, incompatible con Spring Boot `3.2.0`.
- `carrito-ms` e `inventario-ms` usaban Flyway PostgreSQL sin `flyway-core`.
- Gateway usaba `spring-cloud-starter-gateway-server-webflux`, artefacto no gestionado por Spring Cloud `2023.0.x`.
- Gateway tambien referenciaba `JwtProperties` sin clase local.
- `springdoc-openapi` `2.8.16` no era compatible con Spring Framework `6.1.x` de Spring Boot `3.2.0`.
- Los compose de microservicios publicaban puertos HTTP al host.
- No existia compose de Keycloak ni import reproducible del realm `smartcampus`.

## Archivos modificados

- `infra/compose.yml`
- `infra/config/pom.xml`
- `infra/eureka/pom.xml`
- `infra/gateway/pom.xml`
- `infra/gateway/src/main/java/com/upeu/gateway/config/JwtProperties.java`
- `infra/gateway/src/main/java/com/upeu/gateway/config/SecurityConfig.java`
- `infra/gateway/src/test/java/com/upeu/gateway/GatewayApplicationTests.java`
- `infra/config/config-repo/gateway-dev.yml`
- `infra/config/config-repo/gateway-prod.yml`
- `keycloak/compose.yml`
- `keycloak/realm-smartcampus.json`
- `servicio/auth-ms/pom.xml`
- `servicio/auth-ms/compose.yml`
- `servicio/auth-ms/src/main/java/com/upeu/auth/config/JwtProperties.java`
- `servicio/auth-ms/src/main/java/com/upeu/auth/config/SecurityConfig.java`
- `servicio/auth-ms/src/test/java/com/upeu/auth/AuthApplicationTests.java`
- `servicio/carrito-ms/pom.xml`
- `servicio/inventario-ms/pom.xml`
- `servicio/*/pom.xml` con Spring Cloud/Springdoc/Flyway donde aplicaba
- `servicio/*/compose.yml` para quitar publicacion HTTP directa de microservicios

## Cambios aplicados

- Spring Cloud homogeneizado a `2023.0.5`.
- Flyway homogeneizado con `flyway.version=10.17.0` y version explicita en `flyway-database-postgresql`.
- Agregado `flyway-core` en `carrito-ms` e `inventario-ms`.
- Agregado `JwtProperties` en `auth-ms` y Gateway.
- Agregado `PasswordEncoder` con `BCryptPasswordEncoder` en `auth-ms`.
- Gateway cambia `/auth/**` de ruta directa a Keycloak a `lb://auth-ms`.
- Gateway usa `spring-cloud-starter-gateway`, compatible con Spring Cloud `2023.0.x`.
- `oauth2Login` del Gateway queda condicionado a que exista configuracion de cliente OAuth2.
- El converter JWT del Gateway ya no se expone como bean global de conversion.
- Springdoc bajado a `2.3.0`, compatible con Spring Boot `3.2.0`.
- Tests de `auth-ms` usan H2 y propiedades locales minimas, sin depender de Config Server.
- Tests de Gateway usan propiedades locales minimas de JWKS y no dependen de Config Server.
- Compose de Keycloak agregado con imagen `quay.io/keycloak/keycloak:25.0.6`.
- Realm `smartcampus` importable agregado con cliente publico `marketplace-client` y password grant habilitado.
- Los microservicios en `servicio/*/compose.yml` usan `expose: 8080` en lugar de `ports` para su HTTP interno.

## Comandos ejecutados

- `mvn -f servicio/auth-ms/pom.xml clean compile`
- `mvn -f servicio/carrito-ms/pom.xml clean compile`
- `mvn -f servicio/inventario-ms/pom.xml clean compile`
- `mvn -f infra/gateway/pom.xml clean compile`
- `mvn -f servicio/auth-ms/pom.xml test`
- `mvn -f infra/gateway/pom.xml clean test`
- `docker compose -f keycloak/compose.yml config`
- `docker network create ecom-prod-net`
- `docker compose -f keycloak/compose.yml up -d`
- `docker ps --filter name=keycloak --format "{{.Names}} {{.Status}} {{.Ports}}"`
- `docker logs --tail 80 keycloak`
- `curl.exe -i http://127.0.0.1:8080/realms/smartcampus/.well-known/openid-configuration`
- `curl.exe -i http://127.0.0.1:8080/realms/smartcampus/protocol/openid-connect/certs`
- `rg -n --fixed-strings "<spring-cloud.version>2025" -g "pom.xml" .`
- `rg -n --fixed-strings "<version>2.8.16</version>" -g "pom.xml" .`
- `rg -n ":8080" servicio -g "compose.yml"`

## Resultado de compilacion

- `auth-ms`: `BUILD SUCCESS`.
- `carrito-ms`: `BUILD SUCCESS`.
- `inventario-ms`: `BUILD SUCCESS`.
- Gateway: `BUILD SUCCESS`.

## Resultado de pruebas

- `auth-ms`: `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`.
- Gateway: `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`.
- Keycloak: contenedor `keycloak` iniciado, realm `smartcampus` importado correctamente segun logs.
- OIDC discovery: `HTTP/1.1 200 OK`.
- JWKS: `HTTP/1.1 200 OK`, retorna claves del realm.

## Pendientes

- No se verifico password grant end-to-end porque el realm importado no incluye usuarios con contrasenas versionadas. Crear usuarios de prueba desde la consola/admin API y probar `/auth/login` via Gateway cuando `config`, `eureka`, `gateway`, `auth-ms`, PostgreSQL de `auth-ms` y Keycloak esten levantados.
- Los PostgreSQL de los compose de microservicios siguen publicando puertos hacia el host para desarrollo local. La exposicion HTTP directa de microservicios si fue removida.
- El arbol de trabajo ya tenia cambios en `target/`, logs, `.agents/`, `frontend/` y otros archivos no relacionados antes de esta correccion; no se revirtieron.
