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

- `/auth/**` -> `lb://auth-ms`
- `/api/v1/productos/**` -> `lb://producto-ms`
- `/api/v1/carritos/**` -> `lb://carrito-ms`
- `/api/v1/inventarios/**` -> `lb://inventario-ms`

## Seguridad

Gateway valida JWT emitidos por Keycloak usando JWKS. Los roles se leen desde el claim `realm_access.roles` y se transforman a autoridades `ROLE_*`.

El login no lo procesa Gateway directamente. Gateway solo enruta hacia `auth-ms`:

```http
POST http://localhost:28082/auth/login
```

Para que funcione, `auth-ms` debe estar levantado y registrado en Eureka como `AUTH-MS`.

## PROD Docker

```bash
make compose-infra
```

Verificar:

```bash
curl http://localhost:28082/actuator/health
```

En Eureka debe aparecer `GATEWAY`.
