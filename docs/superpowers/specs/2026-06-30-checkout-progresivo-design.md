# Checkout progresivo de producto

## Objetivo

Separar la vista de detalle del producto del flujo de compra. El formulario de facturacion y la seleccion de metodo de pago no deben aparecer anclados al producto al cargar la pagina; deben abrirse solo cuando el usuario elige comprar.

## Alcance

- Mantener el detalle del producto como vista principal: imagen, categoria, estado, titulo, descripcion, precio, vendedor y acciones.
- Reemplazar el formulario incrustado por un boton principal `Comprar ahora`.
- Mostrar un checkout progresivo dentro de la misma pagina al iniciar compra.
- Mantener el flujo de API obligatorio:

```txt
Angular -> Gateway -> orden-ms -> pago-ms -> Mercado Pago API
```

## Flujo UI

1. El usuario abre el detalle del producto.
2. Si pulsa `Comprar ahora` sin sesion, se redirige a `/login` con `returnUrl`.
3. Si tiene sesion, aparece el panel de checkout.
4. El panel muestra:
   - Detalles de facturacion.
   - Resumen del pedido.
   - Metodo de pago: `Tarjeta` o `Yape`.
5. El usuario pulsa `Continuar con Mercado Pago`.
6. Angular crea la orden y solicita la preferencia a `pago-ms`.
7. Si `pago-ms` responde con `sandboxInitPoint` o `initPoint`, el navegador redirige a Mercado Pago.

## Integracion de pago

SmartCampus no captura numero de tarjeta, codigo CVV ni codigo Yape. Esos datos los valida Mercado Pago en su checkout oficial. La seleccion `Tarjeta` o `Yape` se envia como `metodoPago` para trazabilidad interna, pero la validacion real ocurre en Mercado Pago con las credenciales configuradas en `pago-ms`.

## Manejo de errores

- Si falla la carga del producto, se muestra error de carga.
- Si falta facturacion, se muestra error de formulario.
- Si falla `orden-ms`, se muestra error de creacion de orden.
- Si falla `pago-ms` o Mercado Pago, se muestra error de preferencia de pago.

## Restricciones

- No llamar microservicios directos desde Angular.
- No exponer ni manejar credenciales Mercado Pago en frontend.
- No agregar librerias nuevas.
- No hacer commits automaticos.
