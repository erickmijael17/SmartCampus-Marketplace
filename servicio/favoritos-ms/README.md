# Microservicio Favoritos

Microservicio para gestionar la lista de deseos o productos favoritos de cada usuario.

## Responsabilidad
- Agregar y quitar productos de la lista de favoritos de un usuario.
- Obtener los favoritos de un usuario especifico.

## Endpoints
- `GET /api/v1/favoritos/health`
- `GET /api/v1/favoritos`
- `GET /api/v1/favoritos/{id}`
- `POST /api/v1/favoritos`
- `PUT /api/v1/favoritos/{id}`
- `DELETE /api/v1/favoritos/{id}`

## Dependencias
- Gateway y Eureka
- Config Server
- Base de datos relacional (PostgreSQL en produccion)
