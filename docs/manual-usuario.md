# Manual de usuario

## Perfiles

| Rol | Pantallas esperadas | Acciones |
|---|---|---|
| Estudiante | Home, detalle, favoritos, pagos, chat | Comprar, guardar favoritos, conversar |
| Vendedor | Publicar, perfil, chat, ventas | Publicar, revisar ventas, responder |
| Administrador | Categorías, usuarios, publicaciones, métricas | Moderar y administrar |

---

## Flujo de login

1. El usuario abre el frontend.
2. Ingresa credenciales en la pantalla de acceso.
3. El frontend llama `POST /auth/login` por Gateway.
4. `auth-ms` delega la autenticación a Keycloak.
5. El cliente recibe un JWT y lo envía en cada request.

---

## Comprar un producto

1. Abrir catálogo.
2. Seleccionar un producto.
3. Presionar compra desde el detalle.
4. Crear orden con método `MERCADO_PAGO`.
5. Generar preferencia de pago.
6. Completar checkout en Mercado Pago.
7. Volver a la pantalla de resultado.
8. Validar la transacción si el pago queda pendiente.
9. Revisar el chat con el vendedor para coordinar entrega.

---

## Publicar un producto

1. Ingresar como vendedor.
2. Crear publicación con nombre, descripción, categoría y precio.
3. Asociar imágenes mediante `media-ms`.
4. Publicar producto asociado a `producto-ms` y `publicacion-ms`.
5. Revisar mensajes de compradores en chat.

---

## Validar pago Mercado Pago

1. Entrar a la pantalla de resultado de pago.
2. Revisar `pagoId`, `idOrden` y estado.
3. Ingresar el número de transacción `paymentId` si el sistema solicita validación.
4. El backend verifica `external_reference = ORDEN-{idOrden}`.
5. Si el pago está aprobado, se crea un mensaje automático en `chat-ms`.

---

## Incidencias comunes

| Problema | Qué revisar |
|---|---|
| No inicia sesión | Keycloak y `auth-ms` activos |
| Token inválido | `issuer-uri` y `jwk-set-uri` correctos |
| No carga productos | Gateway, Eureka y `producto-ms` |
| Pago queda pendiente | Revisar `paymentId`, `MP_ACCESS_TOKEN` y endpoint de validación |
| No aparece mensaje en chat | Revisar `chat-ms`, Kafka y evento `pago.aprobado` |
| No aparecen métricas | Prometheus y `/actuator/prometheus` |
