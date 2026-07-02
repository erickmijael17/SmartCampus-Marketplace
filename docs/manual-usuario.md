# Manual de usuario

## Perfiles

| Rol | Pantallas esperadas | Acciones |
|---|---|---|
| Estudiante | Productos, favoritos, carrito, órdenes, chat | Comprar, guardar favoritos, comunicarse |
| Vendedor | Publicaciones, productos, inventario, chat | Publicar, editar, responder |
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
3. Agregar al carrito.
4. Confirmar orden.
5. Registrar pago.
6. Revisar estado de orden.

---

## Publicar un producto

1. Ingresar como vendedor.
2. Crear publicación con nombre, descripción, categoría y precio.
3. Asociar imágenes mediante `media-ms`.
4. Verificar stock en `inventario-ms`.
5. Publicar.

---

## Incidencias comunes

| Problema | Qué revisar |
|---|---|
| No inicia sesión | Keycloak y `auth-ms` activos |
| Token inválido | `issuer-uri` y `jwk-set-uri` correctos |
| No carga productos | Gateway, Eureka y `producto-ms` |
| No aparecen métricas | Prometheus y `/actuator/prometheus` |
