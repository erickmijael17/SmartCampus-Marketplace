# Producción PROD local

## Objetivo

Ejecutar SmartCampus Marketplace con Docker Compose, usando redes internas, variables de entorno y Config Server para separar configuración del código.

---

## Orden de arranque

```powershell
make compose-infra
make compose-keycloak
make compose-kafka
make compose-obs
make compose-ms MS=auth-ms
make compose-ms MS=catalogo-ms
make compose-ms MS=producto-ms
make compose-ms MS=carrito-ms
make compose-ms MS=inventario-ms
make compose-ms MS=orden-ms
make compose-ms MS=pago-ms
```

```bash
make compose-infra
make compose-keycloak
make compose-kafka
make compose-obs
make compose-ms MS=auth-ms
make compose-ms MS=catalogo-ms
make compose-ms MS=producto-ms
make compose-ms MS=carrito-ms
make compose-ms MS=inventario-ms
make compose-ms MS=orden-ms
make compose-ms MS=pago-ms
```

---

## Redes Docker

| Red | Uso |
|---|---|
| `ecom-prod-net` | Comunicación entre infraestructura, Keycloak, Kafka, observabilidad y servicios |
| Redes internas por servicio | Aíslan PostgreSQL y microservicio |

Los microservicios no publican sus puertos HTTP al host. Se consumen por Gateway.

---

## Validación post-despliegue

```powershell
docker compose -f infra/compose.yml ps
docker compose -f keycloak/compose.yml ps
docker compose -f kafka/compose.yml ps
docker compose -f obs/compose.yml ps
curl http://localhost:28082/actuator/health
```

```bash
docker compose -f infra/compose.yml ps
docker compose -f keycloak/compose.yml ps
docker compose -f kafka/compose.yml ps
docker compose -f obs/compose.yml ps
curl http://localhost:28082/actuator/health
```

---

## Variables esperadas

| Variable | Uso |
|---|---|
| `CONFIG_SERVER_URL` | URL del Config Server desde cada contenedor |
| `KEYCLOAK_URL` | Emisor OAuth2 del realm `smartcampus` |
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASS` | Conexión PostgreSQL por servicio |
| `KAFKA_BOOTSTRAP_SERVERS` | Broker Kafka para eventos |

!!! danger "Seguridad"
    No subas archivos `.env` con secretos reales. Usa `.env.example` para documentar nombres de variables y valores de demostración.
