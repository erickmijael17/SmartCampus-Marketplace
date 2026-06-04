# Microservicio Ordenes

Microservicio para registrar ordenes de compra en CampusMarket.

## Responsabilidad
- Crear ordenes desde seleccion de compra.
- Consultar ordenes por comprador.
- Actualizar estado de la orden.

## Endpoints
- POST /api/v1/ordenes
- GET /api/v1/ordenes
- GET /api/v1/ordenes/{id}
- GET /api/v1/ordenes/usuario/{idComprador}
- PUT /api/v1/ordenes/{id}
- DELETE /api/v1/ordenes/{id}
