# gateway

API Gateway de SmartCampus Marketplace. Expone el punto unico de entrada HTTP, aplica seguridad JWT en el borde y enruta hacia microservicios registrados en Eureka.

## Puertos

| Modo | URL |
|---|---|
| DEV Maven | `http://localhost:18080` |
| PROD Docker | `http://localhost:28082` |

## Rutas

Las rutas del Gateway se configuran en `infra/config/config-repo/gateway-dev.yml` y `infra/config/config-repo/gateway-prod.yml`.

Ejemplos:

- `/auth/**` y `/api/v1/personas/**` -> `lb://AUTH-MS`
- `/api/v1/productos/**` y `/api/v1/producto/instancia` -> `lb://PRODUCTO-MS`
- `/api/v1/categorias/**` -> `lb://CATEGORIA-MS`
- `/api/v1/publicaciones/**` -> `lb://PUBLICACION-MS`
- `/api/v1/media/**` -> `lb://MEDIA-MS`
- `/api/v1/favoritos/**` -> `lb://FAVORITOS-MS`
- `/api/v1/calificaciones/**` -> `lb://CALIFICACION-MS`
- `/api/v1/chats/**` -> `lb://CHAT-MS`
- `/api/v1/ordenes/**` -> `lb://ORDEN-MS`
- `/api/v1/pagos/**` -> `lb://PAGO-MS`

## Seguridad

Gateway valida JWT emitidos por Keycloak usando JWKS. Los roles se leen desde el claim `realm_access.roles` y se transforman a autoridades `ROLE_*`.

El login no lo procesa Gateway directamente. Gateway solo enruta hacia `auth-ms`:

```http
POST http://localhost:28082/auth/login
```

Para que funcione, `auth-ms` debe estar levantado y registrado en Eureka como `AUTH-MS`.

## PROD Docker

```bash
docker compose -f infra/compose.yml up -d --build
```

Verificar:

```bash
curl http://localhost:28082/actuator/health
```

En Eureka debe aparecer `GATEWAY`.
