# Microservicio Chat

Microservicio de mensajeria interna entre comprador y vendedor para coordinar entregas o resolver dudas sobre un producto.

## Responsabilidad
- Iniciar chats entre usuarios.
- Enviar mensajes (texto y comprobantes).
- Validar venta/compra en el chat.
- Historial de mensajes.

## Endpoints
- `GET /api/v1/chats/health`
- `GET /api/v1/chats`
- `GET /api/v1/chats/{id}`
- `POST /api/v1/chats`
- `POST /api/v1/chats/comprobantes`
- `POST /api/v1/chats/mensaje-venta-validada`
- `PUT /api/v1/chats/{id}`
- `DELETE /api/v1/chats/{id}`
- `GET /api/v1/chats/{id}/mensajes`
- `POST /api/v1/chats/{id}/mensajes`

## Dependencias
- Gateway y Eureka
- Config Server
- Base de datos relacional (PostgreSQL en produccion)
