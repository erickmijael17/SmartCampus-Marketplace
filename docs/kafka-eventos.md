# Kafka y eventos

## Propósito

Kafka desacopla procesos que no deben bloquear el flujo principal del usuario, como pagos, notificaciones, auditoría o consistencia eventual entre órdenes y pagos.

---

## Flujo principal

```mermaid
sequenceDiagram
    participant Orden as orden-ms
    participant Kafka as Kafka
    participant Pago as pago-ms
    participant Noti as notification-ms

    Orden->>Kafka: orden.creada
    Kafka->>Pago: consumir orden.creada
    Pago->>Kafka: pago.aprobado / pago.rechazado
    Kafka->>Noti: consumir evento de pago
```

---

## Servicios productores y consumidores

| Servicio | Rol Kafka | Archivos |
|---|---|---|
| `orden-ms` | Productor de `EventoOrden` | `KafkaConfiguracion.java`, `ProductorOrden.java` |
| `pago-ms` | Consumidor de orden y productor de pago | `ConsumidorPago.java`, `ProductorPago.java` |
| `notification-ms` | Consumidor esperado de eventos | `notification-ms` |

---

## Configuración

En DEV:

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:41092
```

En PROD:

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:kafka:9092}
```

---

## Verificación

```powershell
docker compose -f kafka/compose.yml up -d
curl http://localhost:28085
```

```bash
docker compose -f kafka/compose.yml up -d
curl http://localhost:28085
```

---

## Eventos de dominio propuestos

| Evento | Productor | Consumidor | Uso |
|---|---|---|---|
| `orden.creada` | `orden-ms` | `pago-ms`, `notification-ms` | Iniciar pago/notificación |
| `pago.aprobado` | `pago-ms` | `orden-ms`, `notification-ms` | Confirmar compra |
| `pago.rechazado` | `pago-ms` | `orden-ms`, `notification-ms` | Compensar orden |
| `producto.publicado` | `publicacion-ms` | `search-ms` | Indexar búsqueda |
