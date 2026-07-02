# Microservicio Calificaciones

Microservicio responsable de gestionar las calificaciones y resenas de los productos y vendedores en CampusMarket.

## Responsabilidad
- Permitir a los compradores calificar una compra una vez concretada.
- Listar las calificaciones asociadas a un producto o vendedor.

## Endpoints
- `GET /api/v1/calificaciones/health`
- `GET /api/v1/calificaciones`
- `GET /api/v1/calificaciones/{id}`
- `POST /api/v1/calificaciones`
- `PUT /api/v1/calificaciones/{id}`
- `DELETE /api/v1/calificaciones/{id}`

## Dependencias
- Gateway y Eureka
- Config Server
- Base de datos relacional (PostgreSQL en produccion)
