# infra

Infraestructura base del sistema distribuido: Config Server, Eureka Server y API Gateway.

## Componentes

| Directorio | Puerto host DEV | Puerto host PROD | Puerto container | Rol |
|---|---:|---:|---:|---|
| `config/` | 18888 | 28888 | 8888 | Configuracion centralizada |
| `eureka/` | 18761 | 28761 | 8761 | Service discovery |
| `gateway/` | 18080 | 28082 | 8080 | Punto unico de entrada HTTP |

## DEV con Maven local

Levantar cada servicio en su propia terminal:

```bash
cd infra/config && mvn spring-boot:run
cd infra/eureka && mvn spring-boot:run
cd infra/gateway && mvn spring-boot:run
```

Links:

- Config Server: `http://localhost:18888/catalogo-ms/dev`
- Eureka Dashboard: `http://localhost:18761`
- Gateway: `http://localhost:18080`

## PROD con Docker Compose

Desde la raiz del repositorio:

```bash
docker compose -f infra/compose.yml up -d --build
```

Links:

- Config Server: `http://localhost:28888/actuator/health`
- Eureka Dashboard: `http://localhost:28761`
- Gateway health: `http://localhost:28082/actuator/health`

## Seguridad

Gateway valida tokens emitidos por Keycloak mediante `issuer-uri` y `jwk-set-uri`. Las rutas hacia microservicios usan Eureka y `lb://...`.

Detalle especifico del Gateway: [gateway/README.md](gateway/README.md).
