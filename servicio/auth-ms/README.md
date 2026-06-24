# auth-ms

Microservicio de autenticacion. Mantiene la ruta de login del sistema y delega la autenticacion a Keycloak mediante password grant.

## Puertos

| Recurso | DEV | PROD Docker |
|---|---:|---:|
| App | dinamico (`server.port: 0`) | interno `8080` |
| PostgreSQL | 15431 | 25431 -> 5432 |

`auth-ms` no publica su puerto HTTP al host en Docker Compose. El acceso externo debe hacerse por Gateway.

## DEV con Maven

```bash
cd servicio/auth-ms
docker compose -f compose-dev.yml up -d
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Ver en Eureka DEV: `http://localhost:18761`

## PROD con Docker Compose

```bash
docker compose -f servicio/auth-ms/compose.yml up -d --build
```

Links:

- Eureka PROD: `http://localhost:28761`
- Gateway PROD: `http://localhost:28082`
- Login por Gateway: `POST http://localhost:28082/auth/login`
- Base de datos: `localhost:25431`

## Variables relevantes

- `CONFIG_SERVER_URL`
- `KEYCLOAK_URL`
- `JWT_SECRET`
- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASS`

## Endpoints

- `POST /auth/login`
- `GET /actuator/health`

## Keycloak

`auth-ms` llama a:

```text
${KEYCLOAK_URL}/realms/smartcampus/protocol/openid-connect/token
```

La respuesta se parsea para devolver `preferred_username` y `realm_access.roles`.
