# S09 — Consistencia distribuida en procesos de negocio

> Esta sesión analiza cómo mantener coherencia entre orden, pago Mercado Pago y chat sin usar una base de datos compartida. El enfoque es consistencia eventual, validación e idempotencia.

---

## 1. Introducción
> Tiempo estimado: 20 min

### 1.1 Propósito
Documentar y validar reglas de consistencia entre orden, pago, Mercado Pago y mensaje de venta validada.

### 1.2 Resultado de aprendizaje
El estudiante identifica problemas de consistencia y propone compensaciones e idempotencia.

### 1.3 Producto de sesión
Flujo orden-pago-chat documentado con estados, eventos, validación manual y reglas de idempotencia.

### 1.4 Motivación de la sesión
Si un estudiante paga una orden pero el sistema no valida la transacción o duplica el comprobante en chat, el marketplace pierde confiabilidad.

### 1.5 Ubicación en el curso
- Unidad: U2 — Sistema distribuido robusto.
- Producto de unidad: consistencia eventual en procesos críticos.
- Avance del producto en esta sesión: diseño de estados y compensaciones.

---

## 2. Explica
> Tiempo estimado: 15 min

### 2.1 Conceptos clave

| Concepto | Aplicación |
|---|---|
| Consistencia eventual | Los servicios convergen después de eventos |
| Idempotencia | Procesar el mismo evento sin duplicar efectos |
| Compensación | Revertir o corregir si falla un paso |
| Estado de orden | `PENDIENTE`, `PAGADA`, `RECHAZADA`, `CANCELADA` |
| Estado de pago | `PENDIENTE`, `APROBADO`, `RECHAZADO`, `CANCELADO` |
| `external_reference` | Vincula Mercado Pago con `ORDEN-{idOrden}` |
| Idempotencia en chat | Evita mensajes duplicados de comprobante o venta validada |

### 2.2 Arquitectura del sistema en esta sesión

#### 2.2.1 Entorno DEV (Maven local)

```mermaid
stateDiagram-v2
    [*] --> PENDIENTE
    PENDIENTE --> PAGADA: pago.aprobado / validación Mercado Pago
    PENDIENTE --> RECHAZADA: pago.rechazado
    PAGADA --> ENTREGADA: entrega completada
    RECHAZADA --> CANCELADA: compensación
```

#### 2.2.2 Entorno PROD local (Docker Compose)

```mermaid
flowchart LR
    ORD["orden-ms"]
    PAY["pago-ms"]
    MP["Mercado Pago"]
    CHAT["chat-ms"]
    K["Kafka"]
    ORD --> K
    K --> PAY
    PAY --> MP
    MP --> PAY
    PAY --> K
    K --> CHAT
```

### 2.3 Observabilidad y diagnóstico
Revisar correlación entre logs de orden, pago, Kafka y chat. Un mismo `ordenId` y `mpPaymentId` debe poder rastrearse en todo el flujo.

---

## 3. Aplica — Actividad práctica guiada

### 3.1 Crear una orden

```bash
curl -X POST http://localhost:28082/api/v1/ordenes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <jwt>" \
  -d '{"idComprador":1,"idProducto":1,"cantidad":1,"precioUnitario":25.90,"metodoPago":"MERCADO_PAGO","idVendedor":2}'
```

```powershell
curl -Method POST http://localhost:28082/api/v1/ordenes `
  -Headers @{ "Content-Type"="application/json"; "Authorization"="Bearer <jwt>" } `
  -Body '{"idComprador":1,"idProducto":1,"cantidad":1,"precioUnitario":25.90,"metodoPago":"MERCADO_PAGO","idVendedor":2}'
```

### 3.2 Revisar pagos asociados

```bash
curl -H "Authorization: Bearer <jwt>" http://localhost:28082/api/v1/pagos
```

```powershell
curl -Headers @{ "Authorization"="Bearer <jwt>" } http://localhost:28082/api/v1/pagos
```

### 3.3 Validar transacción Mercado Pago

```bash
curl -X POST http://localhost:28082/api/v1/pagos/<pagoId>/validar-transaccion \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <jwt>" \
  -d '{"paymentId":"<mp-payment-id>"}'
```

```powershell
curl -Method POST http://localhost:28082/api/v1/pagos/<pagoId>/validar-transaccion `
  -Headers @{ "Content-Type"="application/json"; "Authorization"="Bearer <jwt>" } `
  -Body '{"paymentId":"<mp-payment-id>"}'
```

### 3.4 Tabla de archivos trabajados

| Archivo | Uso |
|---|---|
| `servicio/orden-ms/src/main/java/com/upeu/ordenes/service/impl/OrdenServiceImpl.java` | Creación de orden |
| `servicio/orden-ms/src/main/java/com/upeu/ordenes/evento/EventoOrden.java` | Evento de orden |
| `servicio/pago-ms/src/main/java/com/upeu/pagos/evento/EventoPago.java` | Evento de pago |
| `servicio/pago-ms/src/main/java/com/upeu/pagos/service/impl/MercadoPagoCheckoutServiceImpl.java` | Preferencia, confirmación y validación |
| `servicio/chat-ms/src/main/java/com/upeu/chat/service/impl/ChatServiceImpl.java` | Comprobante y mensaje de venta validada |
| `servicio/chat-ms/src/main/resources/db/migration/V2__venta_chat_metadata.sql` | Índices idempotentes |

---

## 4. Crea — Actividad autónoma

Explica cómo `ux_pagos_mp_payment_id` y los índices de `chat-ms` evitan duplicar pagos y mensajes si llega dos veces el mismo evento.

---

## 5. Cierre evaluativo

### Checklist
- [ ] El flujo tiene estados definidos.
- [ ] Hay eventos de dominio.
- [ ] Se documenta validación Mercado Pago.
- [ ] Se define una regla idempotente.
- [ ] El chat no duplica comprobantes.

### Pregunta de defensa
¿Por qué `external_reference` ayuda a probar que un `paymentId` pertenece a la orden correcta?
