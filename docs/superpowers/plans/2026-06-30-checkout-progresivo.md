# Checkout Progresivo Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convertir el detalle de producto en una vista limpia con boton de compra y un checkout progresivo conectado a `orden-ms` y `pago-ms`.

**Architecture:** El componente `ListingDetailPageComponent` mantiene el detalle y controla un estado de checkout local. `MarketplaceService.checkout()` conserva la secuencia `orden-ms` -> `pago-ms` a traves del Gateway. Los endpoints de Mercado Pago se consumen solo desde `pago-ms`.

**Tech Stack:** Angular 20 standalone components, TypeScript, RxJS, Spring Boot Gateway, orden-ms, pago-ms, Mercado Pago API.

## Global Constraints

- Angular nunca llama microservicios directamente.
- Toda API frontend usa `GatewayService` o configuracion centralizada.
- No capturar tarjeta, CVV ni codigo Yape en frontend.
- No agregar nuevas tecnologias.
- No hacer commits automaticos.

---

### Task 1: Checkout UI State

**Files:**
- Modify: `frontend/src/app/pages/listing-detail-page.component.ts`
- Test: `frontend/src/app/pages/listing-detail-page.component.spec.ts`

**Interfaces:**
- Produces: `checkoutOpen: boolean`, `openCheckout(): void`, `closeCheckout(): void`, `submitCheckout(): void`, `orderSubtotal`, `orderTotal`.

- [ ] Write a component test verifying the checkout panel is hidden on initial render.
- [ ] Write a component test verifying `Comprar ahora` opens checkout for authenticated users.
- [ ] Implement `checkoutOpen` and open/close methods.

### Task 2: Checkout Markup and Styles

**Files:**
- Modify: `frontend/src/app/pages/listing-detail-page.component.html`
- Modify: `frontend/src/app/pages/listing-detail-page.component.css`

**Interfaces:**
- Consumes: state and methods from Task 1.

- [ ] Replace the always-visible checkout form with a compact purchase card.
- [ ] Add a two-column checkout panel with billing, order summary and payment selector.
- [ ] Add responsive CSS for mobile and desktop.

### Task 3: Payment Submission

**Files:**
- Modify: `frontend/src/app/pages/listing-detail-page.component.ts`
- Modify: `frontend/src/app/core/services/marketplace.service.spec.ts`

**Interfaces:**
- Consumes: `MarketplaceService.checkout(listing, quantity, paymentMethod)`.

- [ ] Keep order creation and Mercado Pago preference creation inside `MarketplaceService.checkout()`.
- [ ] Map UI payment methods to `YAPE` and `TARJETA`.
- [ ] Show stage-specific status messages.

### Task 4: Verification

**Files:**
- Build/test only.

- [ ] Run `npx.cmd tsc -p tsconfig.spec.json --noEmit`.
- [ ] Run `npm.cmd run build`.
- [ ] Run Maven compile for touched backend services only if backend files changed.
- [ ] Run `git status --short`.
