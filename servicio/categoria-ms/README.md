# Microservicio Categorias

Microservicio responsable de gestionar el catalogo de categorias de los productos en CampusMarket.

## Responsabilidad
- CRUD de categorias.
- Listado publico de categorias.

## Endpoints
- `GET /api/v1/categorias/health`
- `GET /api/v1/categorias`
- `GET /api/v1/categorias/{id}`
- `POST /api/v1/categorias`
- `PUT /api/v1/categorias/{id}`
- `DELETE /api/v1/categorias/{id}`

## Dependencias
- Gateway y Eureka (descubrimiento de servicios y enrutamiento)
- Config Server (configuracion centralizada)
- Base de datos relacional (PostgreSQL en produccion)
