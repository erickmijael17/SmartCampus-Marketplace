# Guia del Estudiante: SmartCampus Marketplace con Docker Compose

Esta guia resume como levantar y probar el backend de SmartCampus Marketplace usando Docker Compose.

## 1. Requisitos

- Java JDK 17
- Maven
- Docker Desktop o Docker Engine
- Make opcional

## 2. Levantar servicios base

Desde la raiz del repositorio:

```bash
make compose-infra
make compose-keycloak
make compose-kafka
make compose-obs
```

O en un solo paso:

```bash
make compose-all
```

Servicios esperados:

- Gateway: `http://localhost:28082`
- Eureka: `http://localhost:28761`
- Config Server: `http://localhost:28888`
- Keycloak: `http://localhost:8080`
- Kafka UI: `http://localhost:28085`
- Grafana: `http://localhost:23000`

## 3. Levantar microservicios

Cada microservicio se levanta con su compose propio:

```bash
make compose-ms MS=auth-ms
make compose-ms MS=producto-ms
make compose-ms MS=catalogo-ms
```

Los microservicios no exponen su puerto HTTP al host. Deben consumirse a traves del Gateway.

## 4. Probar Keycloak

```bash
curl http://127.0.0.1:8080/realms/smartcampus/.well-known/openid-configuration
curl http://127.0.0.1:8080/realms/smartcampus/protocol/openid-connect/certs
```

El realm `smartcampus` y el cliente `marketplace-client` se importan desde `keycloak/realm-smartcampus.json`.

## 5. Probar autenticacion

`auth-ms` funciona como proxy de login hacia Keycloak. La peticion externa debe ir por Gateway:

```http
POST http://localhost:28082/auth/login
Content-Type: application/json

{
  "username": "usuario_prueba",
  "password": "clave_usuario"
}
```

La respuesta incluye el JWT emitido por Keycloak, el tipo de token, expiracion, username y roles.

## 6. Probar autorizacion

Los microservicios validan la firma del JWT con JWKS y leen roles desde `realm_access.roles`.

- Con rol `ADMIN`, probar una operacion protegida como `POST /api/v1/productos`.
- Con un usuario sin el rol requerido, la respuesta esperada es `403 Forbidden`.

Siempre llamar a los endpoints por Gateway:

```text
http://localhost:28082/api/v1/productos
http://localhost:28082/api/v1/carritos
http://localhost:28082/api/v1/inventarios
```

## 7. Apagar el entorno

```bash
make compose-down
```
