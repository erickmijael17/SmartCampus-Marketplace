# Microservicio Publicacion

Microservicio para la gestion del estado de visibilidad y configuraciones especificas de las publicaciones del marketplace.

## Responsabilidad
- Administrar el ciclo de vida de publicacion de un producto (borrador, publicado, pausado, etc.).
- Filtrado de productos visibles.

## Endpoints
- `GET /api/v1/publicaciones/health`
- `GET /api/v1/publicaciones`
- `GET /api/v1/publicaciones/{id}`
- `POST /api/v1/publicaciones`
- `PUT /api/v1/publicaciones/{id}`
- `DELETE /api/v1/publicaciones/{id}`

## Dependencias
- Gateway y Eureka
- Config Server
- Base de datos relacional (PostgreSQL en produccion)
