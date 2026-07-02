# Dominio de negocio

## Contexto

SmartCampus Marketplace resuelve necesidades de compra, venta y comunicación dentro de la comunidad universitaria UPeU. El sistema permite que estudiantes publiquen productos, compradores creen órdenes, vendedores gestionen inventario y administradores controlen el ecosistema.

---

## Actores

| Actor | Necesidad |
|---|---|
| Estudiante comprador | Buscar productos, guardar favoritos, conversar y comprar |
| Vendedor | Publicar productos, controlar stock y atender chats |
| Administrador | Moderar categorías, usuarios, publicaciones y reportes |

---

## Capacidades principales

| Capacidad | Servicios involucrados |
|---|---|
| Autenticación | `auth-ms`, Keycloak, Gateway |
| Catálogo y categorías | `catalogo-ms`, `categoria-ms` |
| Publicación de productos | `producto-ms`, `publicacion-ms`, `media-ms` |
| Stock | `inventario-ms`, `producto-ms` |
| Compra | `carrito-ms`, `orden-ms`, `pago-ms` |
| Comunicación | `chat-ms`, `notification-ms` |
| Reputación | `calificacion-ms` |
| Búsqueda | `search-ms` |
| Perfil universitario | `persona-ms` |

---

## Entidades principales

| Entidad | Descripción |
|---|---|
| Usuario | Persona autenticada en Keycloak |
| Persona | Perfil UPeU del usuario |
| Producto | Artículo o servicio publicado |
| Categoría | Clasificación de productos |
| Inventario | Stock disponible |
| Carrito | Selección temporal de compra |
| Orden | Intención de compra confirmada |
| Pago | Resultado de transacción |
| Publicación | Anuncio visible del marketplace |
| Calificación | Reseña posterior a la compra |

---

## Flujo de ejemplo

1. El estudiante inicia sesión con su cuenta UPeU.
2. Busca un producto de segunda mano.
3. Agrega el producto al carrito.
4. Crea una orden.
5. Registra el pago.
6. El vendedor recibe notificación.
7. El comprador califica la experiencia.
