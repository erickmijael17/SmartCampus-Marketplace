# Matriz MVP de microservicios - SmartCampus Marketplace

Documento de referencia para agentes, frontend y backend. Basado en auditoria frontend-backend: Angular consume solo via Gateway.

## Resumen

| Tier | Cantidad | Descripcion |
|------|----------|-------------|
| **MVP activo** | 11 | Consumidos por Angular o criticos para checkout |
| **Eliminados** | 5 | Removidos del repo, Config Repo y Gateway para reducir complejidad |

Contrato API frontend: [`frontend/docs/API_CONTRACT.md`](../frontend/docs/API_CONTRACT.md).

---

## MVP activo (11 microservicios)

| Microservicio | Clasificacion | Frontend | Gateway |
|---------------|---------------|----------|---------|
| auth-ms | NECESARIO_ACTUALMENTE | Login, Register, `/auth/me` | `/auth/**` |
| persona-ms | NECESARIO_ACTUALMENTE | Perfil, enriquecimiento de sesion | `/api/v1/personas/**` |
| producto-ms | NECESARIO_ACTUALMENTE | Home, detalle, publicar, perfil | `/api/v1/productos/**` |
| categoria-ms | NECESARIO_ACTUALMENTE | Home, publicar, detalle enriquecido desde producto-ms | `/api/v1/categorias/**` |
| orden-ms | NECESARIO_ACTUALMENTE | Checkout | `/api/v1/ordenes/**` |
| pago-ms | NECESARIO_ACTUALMENTE | Checkout | `/api/v1/pagos/**` |
| publicacion-ms | NECESARIO_ACTUALMENTE | Publicar, favoritos, media | `/api/v1/publicaciones/**` |
| media-ms | NECESARIO_ACTUALMENTE | Imagenes metadata | `/api/v1/media/**` |
| favoritos-ms | NECESARIO_ACTUALMENTE | Detalle, perfil | `/api/v1/favoritos/**` |
| calificacion-ms | NECESARIO_ACTUALMENTE | Perfil, detalle | `/api/v1/calificaciones/**` |
| chat-ms | NECESARIO_ACTUALMENTE | Chat, mensajes vendedor | `/api/v1/chats/**` |

---

## Microservicios eliminados

| Microservicio | Motivo |
|---------------|--------|
| carrito-ms | El checkout actual es directo producto -> orden -> pago |
| inventario-ms | No existe UI de stock ni consumo Angular actual |
| notification-ms | No existe pantalla ni consumo Angular actual |
| search-ms | La busqueda actual se resuelve localmente en Home |
| catalogo-ms | Duplicaba categorias; `categoria-ms` es el unico dueno |

No reintroducir estos servicios ni sus rutas Gateway sin validacion del equipo.

---

## Mapa pantalla -> microservicio

| Ruta Angular | Microservicios |
|--------------|----------------|
| `/login`, `/register` | auth-ms, persona-ms |
| `/` | producto-ms, categoria-ms, publicacion-ms*, media-ms* |
| `/publish` | publicacion-ms, producto-ms, categoria-ms, media-ms |
| `/listing/:id` | producto-ms, publicacion-ms*, media-ms*, orden-ms, pago-ms, favoritos-ms, chat-ms, calificacion-ms |
| `/profile` | persona-ms, producto-ms, favoritos-ms, publicacion-ms, media-ms, calificacion-ms |
| `/chat` | chat-ms |

\* Requiere JWT para enriquecer publicaciones/media; invitados ven datos publicos y placeholders locales.

---

## Solapamientos resueltos

| Antes | Decision actual |
|-------|-----------------|
| producto-ms vs publicacion-ms | Mantener ambos: producto=comercio, publicacion=social |
| catalogo-ms vs categoria-ms | Eliminar `catalogo-ms`; usar solo `categoria-ms` |
| search-ms vs filtro Home | Eliminar `search-ms`; mantener busqueda local mientras no exista busqueda server-side real |
| carrito-ms vs orden-ms/pago-ms | Eliminar `carrito-ms`; checkout directo |

---

## Docker Compose MVP

Infra obligatoria: `infra/compose.yml`, Keycloak y Kafka para ordenes/pagos.

```bash
docker compose -f servicio/auth-ms/compose.yml up -d --build
docker compose -f servicio/persona-ms/compose.yml up -d --build
docker compose -f servicio/categoria-ms/compose.yml up -d --build
docker compose -f servicio/producto-ms/compose.yml up -d --build
docker compose -f servicio/publicacion-ms/compose.yml up -d --build
docker compose -f servicio/media-ms/compose.yml up -d --build
docker compose -f servicio/orden-ms/compose.yml up -d --build
docker compose -f servicio/pago-ms/compose.yml up -d --build
docker compose -f servicio/favoritos-ms/compose.yml up -d --build
docker compose -f servicio/calificacion-ms/compose.yml up -d --build
docker compose -f servicio/chat-ms/compose.yml up -d --build
```
