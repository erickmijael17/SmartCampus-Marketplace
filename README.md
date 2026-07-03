# SmartCampus Marketplace

Plataforma de mercado digital universitario basada en microservicios. El backend usa Spring Boot, Spring Cloud Config, Eureka, Spring Cloud Gateway, Keycloak, PostgreSQL, Flyway, Kafka y un stack de observabilidad con Prometheus, Loki, Promtail y Grafana.

El despliegue documentado del proyecto se realiza con Docker Compose. El API Gateway es el punto unico de entrada HTTP para los microservicios.

## Arquitectura

1. **Identidad y seguridad**
   - Keycloak emite tokens JWT para el realm `smartcampus`.
   - `auth-ms` conserva la ruta de login `/auth/login` y delega la autenticacion a Keycloak.
   - Los microservicios validan JWT con `issuer-uri` y `jwk-set-uri`.

2. **Infraestructura**
   - `infra/config`: Config Server.
   - `infra/eureka`: Service discovery.
   - `infra/gateway`: Gateway HTTP unico, con rutas `lb://...` hacia Eureka.
   - `keycloak`: compose e import del realm `smartcampus`.

3. **Microservicios MVP**
   - `auth-ms`, `producto-ms`, `categoria-ms`, `orden-ms`, `pago-ms`, `favoritos-ms`, `chat-ms`, `media-ms`, `calificacion-ms` y `publicacion-ms`.

4. **Integraciones externas**
   - Mercado Pago para procesamiento de pagos y checkout.

5. **Eventos y observabilidad**
   - Kafka para mensajeria asincrona.
   - Prometheus, Loki, Promtail y Grafana para metricas y logs.

## Requisitos

- Java 17
- Maven 3.8+
- Docker Desktop o Docker Engine

## Puertos principales

| Servicio | URL |
|---|---|
| Gateway | `http://localhost:28082` |
| Config Server | `http://localhost:28888` |
| Eureka | `http://localhost:28761` |
| Keycloak | `http://localhost:8080` |
| Kafka UI | `http://localhost:28085` |
| Grafana | `http://localhost:23000` |

Los microservicios no deben publicarse directamente al host por HTTP. En Docker Compose quedan accesibles por red interna y se consumen a traves del Gateway.

## Inicio rapido

> **Requisito previo:** crear la red Docker compartida (solo la primera vez):
> ```bash
> docker network create ecom-prod-net
> ```

```bash
docker compose -f infra/compose.yml up -d --build
docker compose -f keycloak/compose.yml up -d
docker compose -f kafka/compose.yml up -d
docker compose -f obs/compose.yml up -d
```

Para levantar un microservicio especifico:

```bash
docker compose -f servicio/auth-ms/compose.yml up -d --build
```

## Verificaciones utiles

```bash
curl http://localhost:28082/actuator/health
curl http://localhost:28761
curl http://127.0.0.1:8080/realms/smartcampus/.well-known/openid-configuration
curl http://127.0.0.1:8080/realms/smartcampus/protocol/openid-connect/certs
```

## Login por Gateway

`auth-ms` debe estar registrado en Eureka como `AUTH-MS`. El cliente externo usa siempre Gateway:

```http
POST http://localhost:28082/auth/login
Content-Type: application/json

{
  "username": "usuario",
  "password": "clave"
}
```

## Microservicios y MVP

El frontend Angular consume **10 microservicios** via Gateway. Los servicios fuera del MVP fueron eliminados del repo y del Config Repo para reducir complejidad operativa.

- [Matriz MVP activo vs futuro](docs/MVP_MICROSERVICES.md)
- [Contrato API frontend](frontend/docs/API_CONTRACT.md)

## Documentacion relacionada

- [Guia del estudiante](GUIA_ESTUDIANTE.md)
- [Infraestructura](infra/README.md)
- [Gateway](infra/gateway/README.md)
- [Auth MS](servicio/auth-ms/README.md)
