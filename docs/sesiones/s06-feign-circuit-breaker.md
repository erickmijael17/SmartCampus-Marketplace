# S06 — Comunicación síncrona resiliente con Feign y Circuit Breaker

> Esta sesión conecta microservicios por HTTP interno. SmartCampus usa OpenFeign para consultas entre servicios y Resilience4j para controlar fallos.

---

## 1. Introducción
> Tiempo estimado: 20 min

### 1.1 Propósito
Implementar y documentar comunicación síncrona entre microservicios con fallback.

### 1.2 Resultado de aprendizaje
El estudiante consume otro microservicio por nombre Eureka y maneja errores sin tumbar el flujo principal.

### 1.3 Producto de sesión
Clientes Feign activos en el flujo de checkout: `pago-ms` llama a `orden-ms` y `chat-ms` para actualizar estado y registrar comprobantes.

### 1.4 Motivación de la sesión
Una confirmación de pago necesita actualizar la orden y registrar comprobante en chat. Si `chat-ms` falla, el pago no debe perderse ni colapsar toda la compra.

### 1.5 Ubicación en el curso
- Unidad: U2 — Sistema distribuido robusto.
- Producto de unidad: comunicación entre servicios con respuesta controlada ante fallos.
- Avance del producto en esta sesión: integración síncrona resiliente.

---

## 2. Explica
> Tiempo estimado: 15 min

### 2.1 Conceptos clave

| Concepto | Uso |
|---|---|
| OpenFeign | Cliente HTTP declarativo |
| Eureka | Resolución por nombre |
| Circuit Breaker | Evita cascada de fallos |
| Fallback | Respuesta alternativa |
| Correlation ID | Trazabilidad entre servicios |

### 2.2 Arquitectura del sistema en esta sesión

#### 2.2.1 Entorno DEV (Maven local)

```mermaid
flowchart LR
    Pago["pago-ms"]
    Orden["orden-ms"]
    Chat["chat-ms"]
    Pago -->|"Feign orden-ms"| Orden
    Pago -->|"Feign chat-ms"| Chat
```

#### 2.2.2 Entorno PROD local (Docker Compose)

```mermaid
flowchart LR
    subgraph Net["Red Docker compartida"]
        Pago["pago-ms"]
        Orden["orden-ms"]
        Chat["chat-ms"]
        Eureka["eureka"]
    end
    Pago -. descubre .-> Eureka
    Pago -->|"lb://ORDEN-MS"| Orden
    Pago -->|"lb://CHAT-MS"| Chat
```

### 2.3 Observabilidad y diagnóstico
Revisar logs con correlation id y endpoints `/actuator/circuitbreakers` cuando el servicio tenga Resilience4j configurado.

---

## 3. Aplica — Actividad práctica guiada

### 3.1 Ubicar clientes Feign

```bash
grep -R "@FeignClient" -n servicio
```

```powershell
Select-String -Path servicio/**/*.java -Pattern "@FeignClient"
```

### 3.2 Compilar servicios integrados

```bash
mvn -f servicio/pago-ms/pom.xml -DskipTests compile
mvn -f servicio/chat-ms/pom.xml -DskipTests compile
```

```powershell
mvn -f servicio/pago-ms/pom.xml -DskipTests compile
mvn -f servicio/chat-ms/pom.xml -DskipTests compile
```

### 3.3 Tabla de archivos trabajados

| Archivo | Uso |
|---|---|
| `servicio/pago-ms/src/main/java/com/upeu/pagos/client/OrdenClient.java` | Feign hacia orden |
| `servicio/pago-ms/src/main/java/com/upeu/pagos/client/ChatClient.java` | Feign hacia chat |
| `servicio/pago-ms/src/main/java/com/upeu/pagos/client/MercadoPagoClient.java` | Cliente HTTP hacia Mercado Pago |
| `servicio/*/config/FeignTraceConfig.java` | Propagación de trazas |

---

## 4. Crea — Actividad autónoma

Describe un escenario de fallo de `chat-ms` durante la confirmación de pago y qué debería responder `pago-ms`.

---

## 5. Cierre evaluativo

### Checklist
- [ ] Existe al menos un cliente Feign.
- [ ] La llamada usa nombre del servicio.
- [ ] Hay estrategia de fallback o error controlado.
- [ ] Se documenta impacto en negocio.

### Pregunta de defensa
¿Cuál es la diferencia entre una llamada Feign directa y una llamada balanceada con Eureka?
