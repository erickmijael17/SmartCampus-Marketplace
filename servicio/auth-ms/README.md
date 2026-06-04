# auth-ms

Microservicio de autenticación. Registra usuarios, realiza login y emite JWT.

## Puertos

| Recurso | DEV | PROD Docker |
|---|---:|---:|
| App | dinámico (`server.port: 0`) | 8042 -> 8080 |
| PostgreSQL | 15431 | 25431 -> 5432 |

## DEV (Maven)

```bash
cd services/auth-ms
docker compose -f compose-dev.yml up -d
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Ver en Eureka DEV: http://localhost:18761

## PROD (Docker)

```bash
cd services/auth-ms
docker compose up -d --build
```

Links:
- Eureka PROD: http://localhost:28761
- Gateway PROD: http://localhost:28080
- Directo al servicio: http://localhost:8042/actuator/health
- Base de datos: `localhost:25431`

## Ver la BD desde un IDE

| Campo | Valor |
|---|---|
| Motor | PostgreSQL |
| Host | `localhost` |
| Puerto | `25431` |
| Database | `ecom_auth_db` |
| User | `ecom` |
| Password | `ecom` |

## Endpoints

- `POST /auth/register`
- `POST /auth/login`
- `GET /actuator/health`

## JWT

`JWT_SECRET` debe coincidir con `infra/.env` para que Gateway valide los tokens emitidos por Auth.
