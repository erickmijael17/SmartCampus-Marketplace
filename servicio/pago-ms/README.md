# Microservicio Pagos

Microservicio para registrar y consultar pagos de ordenes en CampusMarket.

## Responsabilidad
- Registrar pago asociado a una orden.
- Actualizar estado de pago.
- Consultar pagos por comprador.
- Crear preferencias de pago con Mercado Pago Checkout Pro.
- Sincronizar pagos por retorno/webhook de Mercado Pago.

## Endpoints
- POST /api/v1/pagos
- GET /api/v1/pagos
- GET /api/v1/pagos/{id}
- GET /api/v1/pagos/usuario/{idComprador}
- GET /api/v1/pagos/vendedor/{idVendedor}/resumen
- PUT /api/v1/pagos/{id}
- POST /api/v1/pagos/{id}/validar-transaccion
- DELETE /api/v1/pagos/{id}
- GET /api/v1/pagos/instancia
- POST /api/v1/pagos/mercadopago/preference
- GET /api/v1/pagos/mercadopago/return
- GET /api/v1/pagos/mercadopago/confirmar
- POST /api/v1/pagos/mercadopago/webhook

## Mercado Pago

Checkout Pro usa credenciales por variables de entorno:

- `MP_ACCESS_TOKEN`: access token privado usado solo por pago-ms.
- `MP_PUBLIC_KEY`: public key de prueba o produccion, reservada para futuras integraciones frontend.
- `MP_NOTIFICATION_URL`: URL publica del webhook.
- `MP_SUCCESS_URL`, `MP_FAILURE_URL`, `MP_PENDING_URL`: rutas de retorno al frontend.

En desarrollo local, `FRONTEND_URL=http://localhost:4200` permite crear preferencias,
pero `pago-ms` omite `auto_return` porque Mercado Pago rechaza el retorno automatico
cuando las `back_urls` no son HTTPS publicas.

Para probar retorno automatico con Checkout Pro, expone el frontend con Tunnelmole
u otro tunel HTTPS y configura:

```env
FRONTEND_URL=https://TU_URL.tunnelmole.net
```

No guardar credenciales reales en el repositorio.
