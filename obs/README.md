# obs

Stack de observabilidad: Prometheus, Loki, Promtail y Grafana.

## Servicios

| Servicio | Puerto host DEV | Puerto host PROD | Puerto container |
|---|---:|---:|---:|
| Prometheus | 19090 | 29090 | 9090 |
| Loki | 13100 | 23100 | 3100 |
| Grafana | 13000 | 23000 | 3000 |

## Inicio rápido

```bash
# DEV (red ecom-dev-net)
docker compose -f compose-dev.yml up -d
#   http://localhost:13000 — Grafana (admin/admin)

# PROD (red ecom-prod-net)
docker compose up -d
#   http://localhost:23000 — Grafana (admin/admin)
```

## Datasources

| Datasource | URL (Docker) |
|---|---|
| Prometheus | http://prometheus:29090 |
| Loki | http://loki:23100 |

Documentación en [`../docs/`](../docs/).
