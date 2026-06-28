# catalogo-ms — DEPRECADO para MVP

## Estado

**DEPRECADO** para el MVP de SmartCampus Marketplace. No usar para nuevas integraciones.

## Motivo

- Las categorías del marketplace las gestiona **categoria-ms** (`/api/v1/categorias/**`).
- El frontend Angular consume exclusivamente **categoria-ms**.
- Este servicio duplica CRUD de categorías en `/api/v1/catalogo/categorias/**`, ruta **no expuesta** en Gateway.
- Gateway solo enruta `/api/v1/catalogo/instancia` (health/instance).

## Acción recomendada

| Acción | Cuándo |
|--------|--------|
| No levantar en Compose MVP | Inmediato |
| Mantener código en repo | Hasta validación del equipo |
| Fusionar funcionalidad útil en categoria-ms | Fase backend futura |
| Eliminar rutas Gateway de catalogo-ms | Tras confirmación del equipo |

## Referencias

- [`docs/MVP_MICROSERVICES.md`](../../docs/MVP_MICROSERVICES.md)
- [`categoria-ms`](../categoria-ms/) — servicio canónico de categorías
