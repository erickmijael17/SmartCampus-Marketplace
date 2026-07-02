# Arquitectura

## Visión general

SmartCampus Marketplace implementa un marketplace universitario mediante microservicios. El cliente consume un **API Gateway**; los servicios se registran en **Eureka**, cargan configuración desde **Config Server**, validan identidad con **Keycloak** y se comunican mediante HTTP síncrono y eventos Kafka.

```mermaid
flowchart TB
    User["Usuario UPeU<br/>estudiante, vendedor, admin"]
    Front["Frontend Angular<br/>rama frontend_Smart"]
    KC["Keycloak<br/>realm smartcampus"]
    GW["Gateway<br/>:28082"]
    CFG["Config Server<br/>:28888"]
    EU["Eureka<br/>:28761"]

    subgraph Servicios["Microservicios de negocio"]
        AUTH["auth-ms"]
        CAT["catalogo-ms / categoria-ms"]
        PROD["producto-ms"]
        INV["inventario-ms"]
        CART["carrito-ms"]
        ORD["orden-ms"]
        PAY["pago-ms"]
        PER["persona-ms"]
        PUB["publicacion-ms"]
        FAV["favoritos-ms"]
        CHAT["chat-ms"]
        MEDIA["media-ms"]
        CAL["calificacion-ms"]
        NOTI["notification-ms"]
        SEARCH["search-ms"]
    end

    subgraph Eventos["Mensajería"]
        KAFKA["Kafka"]
        KUI["Kafka UI"]
    end

    subgraph Obs["Observabilidad"]
        PROM["Prometheus"]
        LOKI["Loki"]
        PROMTAIL["Promtail"]
        GRAF["Grafana"]
    end

    User --> Front
    Front -->|"POST /auth/login"| GW
    Front -->|"Bearer JWT"| GW
    AUTH --> KC
    GW --> AUTH & CAT & PROD & INV & CART & ORD & PAY & PER & PUB & FAV & CHAT & MEDIA & CAL & NOTI & SEARCH
    GW -. rutas lb:// .-> EU
    AUTH & CAT & PROD & INV & CART & ORD & PAY & PER & PUB & FAV & CHAT & MEDIA & CAL & NOTI & SEARCH -. registro .-> EU
    AUTH & CAT & PROD & INV & CART & ORD & PAY & PER & PUB & FAV & CHAT & MEDIA & CAL & NOTI & SEARCH -. config .-> CFG
    ORD -->|"orden.creada"| KAFKA
    KAFKA --> PAY
    PAY -->|"pago.*"| KAFKA
    KAFKA --> NOTI
    PROM --> GW
    PROMTAIL --> LOKI --> GRAF
```

---

## Infraestructura transversal

| Componente | Ruta | Función |
|---|---|---|
| Config Server | `infra/config` | Publica configuración centralizada desde `config-repo` |
| Eureka | `infra/eureka` | Registro y descubrimiento de servicios |
| Gateway | `infra/gateway` | Entrada única HTTP, CORS, JWT y enrutamiento |
| Keycloak | `keycloak` | Realm `smartcampus`, usuarios, roles y JWKS |
| Kafka | `kafka` | Eventos de dominio y desacoplamiento |
| Observabilidad | `obs` | Métricas, logs y paneles |

---

## Flujo de autenticación

```mermaid
sequenceDiagram
    participant UI as Cliente / Frontend
    participant GW as Gateway
    participant AUTH as auth-ms
    participant KC as Keycloak
    participant MS as Microservicio protegido

    UI->>GW: POST /auth/login
    GW->>AUTH: Enruta a auth-ms
    AUTH->>KC: Valida credenciales en realm smartcampus
    KC-->>AUTH: JWT RS256
    AUTH-->>UI: access_token
    UI->>GW: Request + Authorization: Bearer
    GW->>KC: Obtiene JWKS / valida firma
    GW->>MS: Reenvía request autenticado
    MS-->>UI: Respuesta de negocio
```

---

## Flujo de compra

```mermaid
sequenceDiagram
    participant E as Estudiante
    participant GW as Gateway
    participant PROD as producto-ms
    participant CART as carrito-ms
    participant ORD as orden-ms
    participant PAY as pago-ms
    participant K as Kafka

    E->>GW: GET /api/v1/productos
    GW->>PROD: Consulta productos
    E->>GW: POST /api/v1/carritos
    GW->>CART: Agrega producto al carrito
    E->>GW: POST /api/v1/ordenes
    GW->>ORD: Crea orden
    ORD->>K: Publica orden.creada
    E->>GW: POST /api/v1/pagos
    GW->>PAY: Registra pago
    PAY->>K: Publica pago.aprobado o pago.rechazado
```

---

## Rutas principales del Gateway

Las rutas se centralizan en `infra/config/config-repo/gateway-dev.yml` y `gateway-prod.yml`.

| Ruta | Servicio |
|---|---|
| `/auth/**` | `auth-ms` |
| `/api/v1/categorias/**` | `catalogo-ms` |
| `/api/v1/productos/**` | `producto-ms` |
| `/api/v1/carritos/**` | `carrito-ms` |
| `/api/v1/inventarios/**` | `inventario-ms` |
| `/api/v1/ordenes/**` | `orden-ms` |
| `/api/v1/pagos/**` | `pago-ms` |

!!! warning "Servicios extendidos"
    El repositorio también contiene `persona-ms`, `publicacion-ms`, `favoritos-ms`, `chat-ms`, `media-ms`, `calificacion-ms`, `notification-ms` y `search-ms`. Si se exponen por Gateway, sus rutas deben añadirse explícitamente al `config-repo` y documentarse en esta página.
