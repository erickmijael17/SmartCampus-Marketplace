# Microservicio Carrito

Microservicio para gestionar items del carrito de compra en CampusMarket.

## Responsabilidad
- Agregar productos al carrito de un comprador.
- Consultar carrito por comprador.
- Actualizar cantidad y eliminar items.

## Endpoints
- POST /api/v1/carritos
- GET /api/v1/carritos
- GET /api/v1/carritos/{id}
- GET /api/v1/carritos/usuario/{idComprador}
- PUT /api/v1/carritos/{id}
- DELETE /api/v1/carritos/{id}
