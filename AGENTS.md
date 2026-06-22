# AGENTS.md

Guia principal para agentes de IA que trabajen en el repositorio `SmartCampus-Marketplace`.

## 1. Descripcion del proyecto

SmartCampus Marketplace es una plataforma de mercado digital universitario basada en microservicios. Permite publicar, buscar, comprar y gestionar productos o servicios dentro de un entorno academico.

El backend usa Java 17, Spring Boot, Spring Cloud Config Server, Eureka, Spring Cloud Gateway, Keycloak, PostgreSQL, Flyway, Kafka y observabilidad con Prometheus, Loki, Promtail y Grafana. El frontend Angular vive en `frontend/`.

El despliegue documentado del repositorio se realiza con Docker Compose.

## 2. Stack tecnologico

- Java 17
- Spring Boot 3.2.x
- Spring Cloud Config Server
- Spring Cloud Netflix Eureka
- Spring Cloud Gateway WebFlux
- Spring Security
- OAuth2 Resource Server
- OAuth2 Client
- JWT/JWKS
- Keycloak
- Maven
- Spring Data JPA
- PostgreSQL
- Flyway
- OpenFeign
- Resilience4j
- Springdoc OpenAPI / Swagger
- Apache Kafka
- Docker
- Docker Compose
- Angular 20
- TypeScript
- RxJS
- Karma/Jasmine
- Prometheus
- Loki
- Promtail
- Grafana
- GitHub Actions para build de imagenes Docker

## 3. Estructura del repositorio

- `.agents/`: reportes y contexto auxiliar de agentes.
- `.github/`: workflows y archivos auxiliares de GitHub.
- `frontend/`: aplicacion Angular.
- `infra/config/`: Config Server y `config-repo`.
- `infra/eureka/`: servidor Eureka.
- `infra/gateway/`: API Gateway.
- `keycloak/`: Docker Compose e import del realm `smartcampus`.
- `kafka/`: Docker Compose para Kafka, Kafka UI y exporter.
- `obs/`: Prometheus, Loki, Promtail y Grafana.
- `servicio/`: microservicios de dominio y soporte.
- `Makefile`: comandos para construir imagenes y levantar servicios.
- `README.md` y `GUIA_ESTUDIANTE.md`: documentacion principal.

## 4. Arquitectura general

La infraestructura base esta en `infra/`:

- `config`: entrega configuracion centralizada desde `infra/config/config-repo`.
- `eureka`: registra servicios y permite descubrimiento dinamico.
- `gateway`: punto unico de entrada HTTP. Usa rutas `lb://...` hacia servicios registrados en Eureka.

Los microservicios estan en `servicio/`. Cada servicio mantiene su propio `pom.xml`, `Dockerfile`, `compose.yml`, `application.yml`, migraciones Flyway cuando aplica y capas como `controller`, `service`, `repository`, `entity`, `dto`, `mapper`, `config` y `exception`.

La seguridad se basa en Keycloak:

- `issuer-uri` apunta al realm `smartcampus`.
- `jwk-set-uri` valida firmas JWT mediante JWKS.
- Los roles se leen desde `realm_access.roles`.
- `auth-ms` conserva `/auth/login` y delega la autenticacion a Keycloak.

La comunicacion combina:

- HTTP sincrono por Gateway y Eureka.
- OpenFeign entre microservicios.
- Kafka para eventos asincronos.

## 5. Reglas para agentes de IA

- Analizar contexto antes de modificar archivos.
- Crear o modificar archivos solo cuando la tarea lo requiera.
- No editar archivos generados, caches, logs ni artefactos de build.
- No hardcodear credenciales, tokens, passwords, secretos ni URLs sensibles.
- Mantener la arquitectura existente: Config Server, Eureka, Gateway, microservicios, Kafka, Keycloak y observabilidad.
- Preferir cambios pequenos, seguros y faciles de revisar.
- No crear dependencias nuevas sin justificacion tecnica clara.
- Respetar el estilo y estructura de cada microservicio.
- Revisar `application.yml`, `config-repo/`, `compose.yml`, `pom.xml` y pruebas antes de proponer cambios de comportamiento.
- No tocar archivos `.env` reales salvo instruccion explicita; preferir `.env.example` para documentar variables.
- No modificar configuraciones de seguridad sin explicar impacto y forma de prueba.

## 6. Seguridad

- Mantener Keycloak como autoridad de identidad.
- Validar JWT mediante `issuer-uri` y `jwk-set-uri`.
- Preservar la extraccion de roles desde `realm_access.roles`.
- No exponer endpoints sensibles sin autenticacion.
- Revisar cuidadosamente cambios en `SecurityConfig.java`.
- No registrar tokens JWT, passwords, Authorization headers, datos personales o payloads sensibles.
- Usar variables de entorno para credenciales y configuracion sensible.
- En Docker Compose, mantener secrets y passwords fuera del codigo cuando sea posible.
- En Gateway, validar rutas publicas y privadas antes de abrir permisos con `permitAll`.
- Mantener CORS restringido a origenes necesarios.
- Proteger endpoints administrativos, escritura, borrado y pagos mediante roles adecuados.

## 7. Convenciones

Java/Spring Boot:

- Mantener paquetes bajo `com.upeu.<servicio>`.
- Separar capas: `controller`, `service`, `repository`, `entity`, `dto`, `mapper`, `config`, `exception`.
- Usar DTOs para entradas y salidas.
- Mantener validaciones con `spring-boot-starter-validation`.
- Usar `GlobalExceptionHandler` cuando exista.
- Mantener migraciones SQL en `src/main/resources/db/migration`.
- Evitar logica de negocio en controladores.
- Al tocar integraciones HTTP internas, revisar Feign y Resilience4j.

Angular:

- Mantener paginas en `frontend/src/app/pages`.
- Mantener servicios compartidos en `frontend/src/app/core/services`.
- Mantener modelos en `frontend/src/app/core/models`.
- Mantener guards en `frontend/src/app/guards`.
- Usar `API_BASE_URL` desde `frontend/src/app/core/config/api.config.ts`.

Docker:

- Mantener Dockerfiles por servicio.
- No duplicar configuracion sensible dentro de imagenes.
- Usar variables de entorno para puertos, perfiles y credenciales.
- Verificar redes Docker como `ecom-prod-net` antes de cambiar Compose.
- No publicar puertos HTTP de microservicios internos al host; consumirlos por Gateway.

## 8. Comandos utiles

- `make help`
- `make build-infra`
- `make build-services`
- `make build-all`
- `make push-all`
- `make release`
- `make compose-infra`
- `make compose-keycloak`
- `make compose-kafka`
- `make compose-obs`
- `make compose-ms MS=auth-ms`
- `make compose-all`
- `make compose-down`
- `make images-list`

Docker Compose directos:

- `docker compose -f infra/compose.yml up -d --build`
- `docker compose -f keycloak/compose.yml up -d`
- `docker compose -f kafka/compose.yml up -d`
- `docker compose -f obs/compose.yml up -d`
- `docker compose -f servicio/<nombre-ms>/compose.yml up -d`

Maven:

- `mvn -f infra/config/pom.xml test`
- `mvn -f infra/eureka/pom.xml test`
- `mvn -f infra/gateway/pom.xml test`
- `mvn -f servicio/<nombre-ms>/pom.xml test`
- `mvn -f servicio/<nombre-ms>/pom.xml package`

Verificacion de estado:

- Config Server: `http://localhost:28888/actuator/health`
- Eureka: `http://localhost:28761`
- Gateway: `http://localhost:28082/actuator/health`
- Keycloak: `http://localhost:8080`
- Kafka UI: `http://localhost:28085`
- Grafana: `http://localhost:23000`

## 9. Flujo recomendado

1. Leer estructura, configuracion, README, `pom.xml`, `compose.yml` y codigo relacionado.
2. Ubicar servicio, capa, endpoint, configuracion o flujo afectado.
3. Aplicar cambios pequenos y enfocados.
4. Ejecutar pruebas Maven, Docker Compose o healthchecks segun corresponda.
5. Documentar archivos modificados, impacto y forma de prueba.

## 10. Archivos que no deben modificarse manualmente

- `target/`
- `build/`
- `dist/`
- `node_modules/`
- `.angular/cache/`
- `.idea/`
- `.vscode/` salvo instruccion explicita
- `logs/`
- `*.log`
- `*.class`
- `*.jar`
- `*.war`
- `*.gz`
- `*.zip`
- `*.tar`
- `*.tar.gz`
- caches o artefactos de herramientas
- `.env` con secretos reales

## 11. Formato de respuesta esperado

Cuando se realice una tarea, responder con:

- Resumen del cambio.
- Archivos modificados.
- Riesgo o impacto.
- Como probarlo.
- Siguientes pasos recomendados.
