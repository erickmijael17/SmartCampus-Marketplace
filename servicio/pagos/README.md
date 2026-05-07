# Microservicio Pagos

Microservicio para registrar y consultar pagos de ordenes en CampusMarket.

## Responsabilidad
- Registrar pago asociado a una orden.
- Actualizar estado de pago.
- Consultar pagos por comprador.

## Endpoints
- POST /api/v1/pagos
- GET /api/v1/pagos
- GET /api/v1/pagos/{id}
- GET /api/v1/pagos/usuario/{idComprador}
- PUT /api/v1/pagos/{id}
- DELETE /api/v1/pagos/{id}
