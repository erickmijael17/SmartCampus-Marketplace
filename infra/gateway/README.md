# gateway

API Gateway de la plataforma. Expone el punto de entrada HTTP, aplica seguridad JWT en el borde y enruta hacia los microservicios registrados en Eureka.

## Puertos

| Modo | URL |
|---|---|
| DEV Maven | http://localhost:18080 |
| PROD Docker | http://localhost:28082 |

## Requisitos de seguridad

Gateway valida los JWT emitidos por `auth-ms`. Para que login y rutas protegidas funcionen:

- `infra/.env` y `services/auth-ms/.env` deben tener el mismo `JWT_SECRET`.
- `gateway-prod.yml` y `auth-ms-prod.yml` deben usar el mismo `jwt.issuer`.
- Actualmente el issuer compartido es `auth`.

Ejemplo:

```env
JWT_SECRET=1s3alJJATsWK91vf5zrODYlQa+LauM/9udaLZlQhHlpu46g/KzmSS5c3CGy6xF9kzAqBhvjmKBuZO/pSL7tfOg==
```

Si el secreto no es Base64 válido, Gateway falla al arrancar con:

```text
Illegal base64 character
```

## PROD Docker

```bash
cd infra
docker compose up -d --build --force-recreate config eureka gateway
docker compose logs -f gateway
```

Verificar:

```text
http://localhost:28082/actuator/health
http://localhost:28761
```

En Eureka debe aparecer `GATEWAY`.

## Login y usuarios

El alta de usuarios y login no los hace Gateway directamente. Gateway solo enruta:

| Operacion | Endpoint por Gateway |
|---|---|
| Registrar usuario | `POST http://localhost:28082/auth/register` |
| Login | `POST http://localhost:28082/auth/login` |

Para que funcione, primero debe estar levantado `auth-ms` y registrado en Eureka como `AUTH-MS`.
