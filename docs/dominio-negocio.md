# Dominio de negocio

## Contexto

SmartCampus Marketplace resuelve necesidades de compra, venta y comunicación dentro de la comunidad universitaria UPeU. El sistema permite que estudiantes publiquen productos, compradores creen órdenes, paguen con Mercado Pago, conversen con vendedores y registren evidencia de venta validada.

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
| Catálogo y categorías | `categoria-ms`, `producto-ms` |
| Publicación de productos | `producto-ms`, `publicacion-ms`, `media-ms` |
| Compra | `orden-ms`, `pago-ms`, Mercado Pago |
| Comunicación | `chat-ms` |
| Reputación | `calificacion-ms` |
| Perfil universitario | `auth-ms` |

---

## Entidades principales

| Entidad | Descripción |
|---|---|
| Usuario | Persona autenticada en Keycloak |
| Persona | Perfil UPeU del usuario |
| Producto | Artículo o servicio publicado |
| Categoría | Clasificación de productos |
| Orden | Intención de compra confirmada |
| Pago | Transacción Mercado Pago o validación manual |
| Publicación | Anuncio visible del marketplace |
| Calificación | Reseña posterior a la compra |
| Conversación | Chat entre comprador y vendedor |

---

## Flujo de ejemplo

1. El estudiante inicia sesión con su cuenta UPeU.
2. Busca un producto de segunda mano.
3. Crea una orden desde el detalle.
4. Genera preferencia de pago con Mercado Pago.
5. Confirma o valida el número de transacción.
6. `pago-ms` publica `pago.aprobado`.
7. `chat-ms` registra un mensaje de venta validada.
8. El comprador y vendedor coordinan entrega por chat.
