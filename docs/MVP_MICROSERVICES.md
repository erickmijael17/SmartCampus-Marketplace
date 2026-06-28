# Matriz MVP de microservicios — SmartCampus Marketplace

Documento de referencia para agentes, frontend y backend. Basado en auditoría frontend-backend (Angular consume solo vía Gateway).

## Resumen

| Tier | Cantidad | Descripción |
|------|----------|-------------|
| **MVP activo** | 11 | Consumidos por Angular o críticos para checkout |
| **Pausado / futuro** | 5 | En Gateway pero sin consumo frontend actual |
| **Deprecado** | 1 | `catalogo-ms` (CRUD categorías duplicado con `categoria-ms`) |

Contrato API frontend: [`frontend/docs/API_CONTRACT.md`](../frontend/docs/API_CONTRACT.md).

---

## MVP activo (11 microservicios)

| Microservicio | Clasificación | Frontend | Gateway |
|---------------|---------------|----------|---------|
| auth-ms | NECESARIO_ACTUALMENTE | Login, Register | `/auth/**` |
| persona-ms | NECESARIO_ACTUALMENTE | Perfil, enriquecimiento sesión | `/api/v1/personas/**` |
| producto-ms | NECESARIO_ACTUALMENTE | Home, detalle, publicar, perfil | `/api/v1/productos/**` |
| categoria-ms | NECESARIO_ACTUALMENTE | Home, publicar | `/api/v1/categorias/**` |
| orden-ms | NECESARIO_ACTUALMENTE | Checkout | `/api/v1/ordenes/**` |
| pago-ms | NECESARIO_ACTUALMENTE | Checkout | `/api/v1/pagos/**` |
| publicacion-ms | USADO_PARCIALMENTE | Publicar, favoritos, media | `/api/v1/publicaciones/**` |
| media-ms | USADO_PARCIALMENTE | Imágenes metadata | `/api/v1/media/**` |
| favoritos-ms | USADO_PARCIALMENTE | Detalle, perfil | `/api/v1/favoritos/**` |
| calificacion-ms | USADO_PARCIALMENTE | Perfil (lectura + POST en detalle) | `/api/v1/calificaciones/**` |
| chat-ms | USADO_PARCIALMENTE | Chat, mensajes vendedor | `/api/v1/chats/**` |

---

## Pausado / fase futura (5 microservicios)

| Microservicio | Clasificación | Motivo |
|---------------|---------------|--------|
| carrito-ms | NO_USADO_POR_FRONTEND | Checkout directo producto→orden→pago |
| inventario-ms | NO_USADO_POR_FRONTEND | Sin UI de stock |
| notification-ms | FASE_FUTURA | Sin pantalla; Kafka integrado para eventos |
| search-ms | NO_USADO_POR_FRONTEND | Búsqueda local en Home |
| catalogo-ms | DUPLICADO_O_SOLAPADO | Solo `/instancia` en Gateway; usar categoria-ms |

**No eliminar código** de estos servicios sin validación del equipo. Pausar en Compose MVP opcional.

---

## Mapa pantalla → microservicio

| Ruta Angular | Microservicios |
|--------------|----------------|
| `/login`, `/register` | auth-ms, persona-ms |
| `/` | producto-ms, categoria-ms, publicacion-ms*, media-ms* |
| `/publish` | publicacion-ms, producto-ms, categoria-ms, media-ms |
| `/listing/:id` | producto-ms, publicacion-ms*, media-ms*, orden-ms, pago-ms, favoritos-ms, chat-ms, calificacion-ms |
| `/profile` | persona-ms, producto-ms, favoritos-ms, publicacion-ms, media-ms, calificacion-ms |
| `/chat` | chat-ms |

\* Requiere JWT (invitados ven placeholders locales).

---

## Solapamientos documentados

| Par | Decisión MVP |
|-----|--------------|
| producto-ms vs publicacion-ms | Mantener ambos: producto=comercio, publicación=social |
| catalogo-ms vs categoria-ms | **Usar solo categoria-ms**; catalogo-ms deprecado |
| auth-ms vs persona-ms | Complementarios: JWT vs perfil numérico |
| search-ms vs filtro Home | Fase futura server-side |

---

## Docker Compose MVP sugerido

Infra obligatoria: `infra/compose.yml`, Keycloak, Kafka (si checkout async).

Microservicios mínimos:

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

Opcional fase futura: carrito-ms, inventario-ms, notification-ms, search-ms.

No levantar en MVP: **catalogo-ms** (deprecado).
