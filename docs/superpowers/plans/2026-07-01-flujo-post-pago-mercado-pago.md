# Flujo Post Pago Mercado Pago Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Confirmar pagos de Mercado Pago, actualizar pago/orden y enviar comprobante automatico al chat.

**Architecture:** Angular confirma el retorno por Gateway contra `pago-ms`. `pago-ms` verifica el `payment_id` con Mercado Pago, usa snapshot local para actualizar `orden-ms` por Feign y crea un comprobante en `chat-ms` por Feign. `chat-ms` crea o reutiliza una conversacion comprador-publicador y guarda un mensaje automatico.

**Tech Stack:** Angular 20 standalone, Spring Boot 3.2, OpenFeign, JPA, PostgreSQL, Mercado Pago Checkout Pro.

## Global Constraints

- No crear roles fijos de vendedor/comprador.
- `idVendedor` significa dueño/publicador de la publicacion en esa transaccion.
- No guardar datos sensibles de tarjeta, numero completo ni CVV.
- No confiar solo en `status` de la URL; verificar siempre con Mercado Pago desde `pago-ms`.
- Todo acceso desde Angular debe pasar por Gateway.
- No hacer commits automaticos.
- No romper el flujo actual que redirige y permite pagar en Sandbox.

---

### Task 1: Snapshot de compra en pago-ms

**Files:**
- Modify: `servicio/pago-ms/src/main/java/com/upeu/pagos/entity/Pago.java`
- Modify: `servicio/pago-ms/src/main/java/com/upeu/pagos/dto/MercadoPagoPreferenceRequest.java`
- Modify: `servicio/pago-ms/src/main/java/com/upeu/pagos/service/impl/MercadoPagoCheckoutServiceImpl.java`
- Create: `servicio/pago-ms/src/main/resources/db/migration/V5__payment_snapshot.sql`
- Modify tests under `servicio/pago-ms/src/test/java/com/upeu/pagos/service/impl/`

**Interfaces:**
- Consumes Angular preference payload fields: `ordenId`, `idComprador`, `idVendedor`, `publicacionId`, `titulo`, `descripcion`, `cantidad`, `precio`, `metodoPago`.
- Produces `Pago` rows with purchase snapshot available after Mercado Pago return/webhook.

- [ ] Write failing service test proving snapshot fields are saved.
- [ ] Add entity columns and migration.
- [ ] Extend request DTO aliases for `idVendedor`/`idPublicador`.
- [ ] Save snapshot in `createPreference`.
- [ ] Run `mvn -f servicio/pago-ms/pom.xml test`.

### Task 2: Chat comprobante endpoint

**Files:**
- Modify: `servicio/chat-ms/src/main/java/com/upeu/chat/controller/ChatController.java`
- Modify: `servicio/chat-ms/src/main/java/com/upeu/chat/repository/ConversacionRepository.java`
- Modify: `servicio/chat-ms/src/main/java/com/upeu/chat/service/impl/ChatServiceImpl.java`
- Create DTOs under `servicio/chat-ms/src/main/java/com/upeu/chat/dto/`
- Add/modify tests under `servicio/chat-ms/src/test/java`

**Interfaces:**
- Consumes `POST /api/v1/chats/comprobantes` with comprador/publicador, orden, monto and message data.
- Produces `{ chatId, mensajeId, mensajeComprobanteEnviado }`.

- [ ] Write failing service/controller test for creating comprobante and reusing conversation.
- [ ] Add repository query for user pair in either order.
- [ ] Add DTOs and service method.
- [ ] Add controller endpoint.
- [ ] Run `mvn -f servicio/chat-ms/pom.xml test`.

### Task 3: Confirmacion en pago-ms

**Files:**
- Modify: `servicio/pago-ms/src/main/java/com/upeu/pagos/controller/MercadoPagoController.java`
- Modify: `servicio/pago-ms/src/main/java/com/upeu/pagos/service/MercadoPagoCheckoutService.java`
- Modify: `servicio/pago-ms/src/main/java/com/upeu/pagos/service/impl/MercadoPagoCheckoutServiceImpl.java`
- Create Feign client DTOs and clients under `servicio/pago-ms/src/main/java/com/upeu/pagos/client/`
- Create response DTO under `servicio/pago-ms/src/main/java/com/upeu/pagos/dto/`
- Modify tests under `servicio/pago-ms/src/test/java`

**Interfaces:**
- Produces `GET /api/v1/pagos/mercadopago/confirmar`.
- Calls Mercado Pago `GET /v1/payments/{id}`, `orden-ms` GET/PUT, and `chat-ms` comprobante endpoint.

- [ ] Write failing test for approved confirmation updating pago, order and chat.
- [ ] Add Feign clients for order and chat.
- [ ] Implement confirmation status mapping.
- [ ] Reuse confirmation from webhook.
- [ ] Run `mvn -f servicio/pago-ms/pom.xml test`.

### Task 4: Angular retorno post pago

**Files:**
- Modify: `frontend/src/app/core/services/pago-api.service.ts`
- Modify: `frontend/src/app/core/services/pago-api.service.spec.ts`
- Modify: `frontend/src/app/pages/payment-result/payment-result.component.ts`
- Modify: `frontend/src/app/pages/payment-result/payment-result.component.html`
- Modify: `frontend/src/app/pages/payment-result/payment-result.component.css` if needed.

**Interfaces:**
- Consumes `GET /api/v1/pagos/mercadopago/confirmar` via Gateway.
- Navigates to `/chat?chatId={id}` when confirmation returns a chat id.

- [ ] Write failing Angular service/component tests for confirmation.
- [ ] Add `confirmarMercadoPago` service method.
- [ ] Update `/pago/exito` component to verify and redirect.
- [ ] Run `npm.cmd run build`.

### Task 5: Final verification

- [ ] Run `mvn -f servicio/pago-ms/pom.xml clean compile`.
- [ ] Run `mvn -f servicio/chat-ms/pom.xml clean compile`.
- [ ] Run `npm.cmd run build` in `frontend`.
- [ ] Run `git status --short`.
