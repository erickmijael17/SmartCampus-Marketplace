# AGENTS.md — Guia para agentes IA en SmartCampus Marketplace

## 1. Identidad del proyecto

SmartCampus Marketplace es una plataforma de mercado digital universitario basada en microservicios. Permite publicar, buscar, comprar y gestionar productos o servicios dentro de un entorno academico.

**Stack tecnologico:**

| Componente | Tecnologia |
|------------|-----------|
| Frontend | Angular 20 (standalone components, TypeScript, RxJS) |
| Backend | Java 17, Spring Boot 3.2.x |
| API Gateway | Spring Cloud Gateway (WebFlux) |
| Service Discovery | Netflix Eureka |
| Configuracion | Spring Cloud Config Server (native) |
| Base de datos | PostgreSQL por microservicio |
| Migraciones | Flyway |
| Identidad | Keycloak 25.x (realm `smartcampus`) |
| Mensajeria | Apache Kafka (KRaft mode) |
| Observabilidad | Prometheus, Loki, Promtail, Grafana |
| Contenedores | Docker Compose |
| CI/CD | GitHub Actions (build de imagenes Docker) |

## 2. Regla principal de arquitectura

La arquitectura obligatoria del proyecto es:

```txt
Angular -> GatewayService / ApiConfig -> Spring Cloud Gateway -> lb://MICROSERVICIO -> Controller -> Service -> Repository/DB
```

**Prohibido:**

```txt
Angular -> microservicio directo
Angular -> localhost:puerto-interno-ms
Angular -> rutas relativas que terminen resolviendo contra localhost:4200
```

El Gateway es el **unico punto de entrada HTTP** a cualquier microservicio. Ningun microservicio interno debe ser llamado directamente desde el frontend.

### Flujo de autenticacion

```txt
Angular -> Gateway -> auth-ms -> Keycloak (password grant) -> JWT
```

Angular no debe llamar directo a Keycloak ni a auth-ms fuera del Gateway.

## 3. Reglas no negociables

1. El frontend **nunca** llama microservicios directamente.
2. Toda URL de API en Angular debe construirse con `GatewayService`, `ApiConfig` o servicios API centralizados.
3. El Gateway es la **unica API publica** del backend.
4. Cada endpoint debe tener un **unico microservicio dueno**.
5. No crear rutas duplicadas en Gateway.
6. Las rutas `lb://SERVICE` deben estar en **mayusculas**.
7. No mezclar responsabilidades entre microservicios.
8. No usar mocks sin marcarlos como `// TEMPORAL_MOCK`.
9. No agregar nuevas tecnologias sin justificar.
10. No hacer cambios masivos sin plan previo.
11. No hacer commits automaticos.
12. No usar `git add .`, `git reset`, `git clean` ni cambiar de rama sin autorizacion.
13. No modificar `.env` con secretos reales.
14. Mantener documentacion actualizada si se cambia arquitectura, rutas o flujo frontend-backend.
15. Siempre revisar `gateway-dev.yml` y `gateway-prod.yml` antes de crear o modificar rutas.

## 4. Mapa de microservicios

| Servicio | Responsabilidad | Ruta Gateway esperada |
|----------|----------------|----------------------|
| `auth-ms` | Login, register, me, integracion Keycloak | `/auth/**` |
| `producto-ms` | CRUD de productos del marketplace | `/api/v1/productos/**` |
| `categoria-ms` | CRUD de categorias | `/api/v1/categorias/**` |
| `orden-ms` | Ordenes de compra | `/api/v1/ordenes/**` |
| `pago-ms` | Procesamiento de pagos | `/api/v1/pagos/**` |
| `persona-ms` | **Fusionado en auth-ms** | `/auth/**` y `/api/v1/personas/**` |
| `chat-ms` | Mensajeria entre usuarios | `/api/v1/chats/**` |
| `favoritos-ms` | Productos favoritos por usuario | `/api/v1/favoritos/**` |
| `calificacion-ms` | Calificaciones y resenas | `/api/v1/calificaciones/**` |
| `media-ms` | Imagenes y archivos multimedia | `/api/v1/media/**` |
| `publicacion-ms` | Publicaciones y visibilidad de productos | `/api/v1/publicaciones/**` |

## 5. Reglas para frontend Angular

- No usar `HttpClient` con URLs relativas para endpoints backend **sin pasar por `GatewayService`**.
- Usar siempre `GatewayService.baseUrl()` o configuracion centralizada (`API_CONFIG`).
- Revisar siempre estos archivos antes de modificar el frontend:

  - `frontend/src/app/core/services/gateway.service.ts`
  - `frontend/src/app/core/config/api.config.ts`
  - `frontend/src/app/core/services/auth-api.service.ts`
  - `frontend/src/app/core/services/marketplace.service.ts`
  - `frontend/src/app/core/services/chat.service.ts`
  - `frontend/src/app/core/services/session.service.ts`
  - `frontend/src/app/core/interceptors/auth-token.interceptor.ts`
  - `frontend/src/app/guards/auth.guard.ts`
  - `frontend/src/app/guards/guest.guard.ts`
  - `frontend/src/app/app.routes.ts`
- Si una pantalla usa mock o localStorage, marcarlo como `// TEMPORAL_MOCK`.
- No crear rutas Angular nuevas sin verificar endpoint Gateway y microservicio dueno.
- Mantener DEV en `http://localhost:18080` y PROD en `http://localhost:28082`.
- Los componentes de pagina deben usar la convencion de **subdirectorios** (`pages/<nombre>/`).
- No crear nuevos componentes flat `*-page.component.*`; migrar los existentes cuando se toque cada pantalla.
- Los tokens deben guardarse, leerse y limpiarse desde `SessionService`. No duplicar manejo de sesion en componentes.
- Las peticiones autenticadas deben enviar `Authorization: Bearer <token>` mediante el interceptor HTTP, no armando headers manualmente.
- Proteger rutas privadas con `auth.guard`, rutas de invitados con `guest.guard`.

## 6. Reglas para Gateway

- Gateway es la **unica API publica** del backend.
- Usar `lb://SERVICE` en **mayusculas** (ej: `lb://PRODUCTO-MS`).
- No duplicar paths entre rutas.
- Mantener `gateway-dev.yml` y `gateway-prod.yml` alineados en rutas CRUD.
- **Solo lectura publica** cuando corresponda:

  - `GET /api/v1/productos/**` puede ser publico
  - `GET /api/v1/categorias/**` puede ser publico
  - `POST`, `PUT`, `DELETE` sobre cualquier recurso deben requerir JWT salvo justificacion explicita
- Ordenes, pagos, perfil, chat, favoritos y escritura de productos deben requerir JWT.
- Las rutas Swagger/OpenAPI pueden estar publicas en DEV pero no en PROD.
- Mantener CORS restringido a origenes necesarios (`localhost:4200`, `localhost:4300`).
- No exponer endpoints sensibles sin autenticacion.

## 7. Reglas para microservicios

- Cada microservicio debe tener una **sola responsabilidad** (principio de responsabilidad unica).
- Cada microservicio debe tener su **propia base de datos** PostgreSQL.
- Usar DTOs para entrada y salida de datos.
- Mantener el patron de capas:

  ```txt
  controller/ -> service/ (o service/impl/) -> repository/ -> entity/
                                            -> dto/
                                            -> mapper/ (cuando aplique)
                                            -> config/
                                            -> exception/ (cuando aplique)
  ```
- No exponer entidades JPA directamente en los controladores.
- Agregar `GlobalExceptionHandler`, OpenAPI/Swagger, tests y correlation filter cuando se implemente funcionalidad real.
- Comunicacion interna entre microservicios solo con **OpenFeign** (sincrono) o **Kafka** (asincrono), cuando este justificado.
- Los metodos HTTP en los controladores deben seguir el estandar REST:
  - `GET` para lectura
  - `POST` para creacion
  - `PUT` para actualizacion
  - `DELETE` para eliminacion
- Evitar logica de negocio en controladores.

## 8. Estados de funcionalidad

Todo agente debe clasificar cada funcionalidad antes de afirmar que esta terminada:

| Estado | Significado |
|--------|-------------|
| `REAL` | Conectado a Gateway y microservicio, funcional end-to-end |
| `PARCIAL` | Existe backend o frontend, pero falta integracion completa |
| `MOCK` | Simulado con datos locales, localStorage o hardcodeados |
| `NO_INTEGRADO` | Existe visualmente pero no llama backend |
| `PENDIENTE` | Planificado pero no implementado |

## 9. Prioridades actuales

Orden de importancia para el desarrollo:

1. Gateway activo y URLs absolutas desde Angular.
2. Register real.
3. Login real.
4. `/auth/me` real.
5. Productos y categorias reales.
6. Publicar producto autenticado.
7. Detalle de producto.
8. Orden y pago con manejo de errores.
9. Perfil real (persona-ms o auth-ms).
10. Chat real (chat-ms).
11. Media real (media-ms, subida de imagenes).
12. Favoritos y calificaciones.
13. Limpieza de componentes duplicados (flat -> subdirectorios).
14. Agregar tests a microservicios.

## 10. Validaciones obligatorias

Antes de finalizar cualquier tarea:

```bash
# Si se toco frontend
cd frontend && npm run build

# Si se toco Gateway
mvn -f infra/gateway/pom.xml clean compile

# Si se toco un microservicio
mvn -f servicio/<nombre-ms>/pom.xml clean compile

# Siempre revisar estado
git status --short
```

No ejecutar build de frontend si solo se modifico documentacion o backend.

## 11. Formato de reporte final

Todo agente debe entregar al finalizar una tarea:

1. **Que reviso** — Archivos leidos y contexto analizado.
2. **Que modifico** — Cambios realizados y por que.
3. **Archivos modificados** — Lista de rutas de archivos.
4. **Comandos ejecutados** — Comandos y sus salidas relevantes.
5. **Resultado de build/test** — Compilacion y pruebas.
6. **Estado de `git status --short`** — Archivos nuevos, modificados, eliminados.
7. **Riesgos pendientes** — Problemas conocidos no resueltos.
8. **Proxima fase recomendada** — Siguiente paso en la prioridad.

## 12. Archivos que no deben modificarse manualmente

- `target/`
- `build/`
- `dist/`
- `node_modules/`
- `.angular/cache/`
- `.idea/`
- `.vscode/` salvo instruccion explicita
- `logs/`
- `*.log`
- `*.class`
- `*.jar`
- `*.war`
- `*.gz`
- `*.zip`
- `*.tar`
- `*.tar.gz`
- caches o artefactos de herramientas
- `.env` con secretos reales

## 13. Comandos utiles

```bash
# Docker
docker network create ecom-prod-net
docker compose -f infra/compose.yml up -d --build
docker compose -f keycloak/compose.yml up -d
docker compose -f kafka/compose.yml up -d
docker compose -f obs/compose.yml up -d

# Maven
mvn -f infra/config/pom.xml test
mvn -f infra/eureka/pom.xml test
mvn -f infra/gateway/pom.xml test
mvn -f servicio/<nombre-ms>/pom.xml test
mvn -f servicio/<nombre-ms>/pom.xml package

# Frontend
cd frontend && npm install && npm run build && npm start

# Health checks
curl http://localhost:18888/actuator/health   # Config Server (dev)
curl http://localhost:18761/eureka/apps       # Eureka (dev)
curl http://localhost:18080/actuator/health   # Gateway (dev)
curl http://localhost:28082/actuator/health   # Gateway (prod)
```

## 14. Clasificacion MVP de microservicios

Matriz completa: [`docs/MVP_MICROSERVICES.md`](docs/MVP_MICROSERVICES.md).

| Tier | Servicios |
|------|-----------|
| **MVP activo (11)** | auth-ms, persona-ms, producto-ms, categoria-ms, orden-ms, pago-ms, publicacion-ms, media-ms, favoritos-ms, calificacion-ms, chat-ms |
| **Eliminados del repo** | carrito-ms, inventario-ms, notification-ms, search-ms, catalogo-ms |

Reglas para agentes:

- No reintroducir rutas Gateway para microservicios eliminados sin validacion del equipo.
- No anadir rutas Gateway duplicadas para categorias; `categoria-ms` es el unico dueno.
- Priorizar integracion real sobre mocks (`TEMPORAL_MOCK`) en servicios MVP activos.

## 15. Referencias

- `docs/MVP_MICROSERVICES.md` — Matriz MVP activo vs pausado segun frontend Angular.
- `frontend/docs/API_CONTRACT.md` — Contrato HTTP frontend-backend.
- `estructura_proyecto.md` — Documentacion oficial de arquitectura y estructura del proyecto.
- `infra/config/config-repo/gateway-dev.yml` — Rutas del Gateway en desarrollo (31 rutas).
- `infra/config/config-repo/gateway-prod.yml` — Rutas del Gateway en produccion (15 rutas).
- `frontend/src/app/core/config/api.config.ts` — Endpoints centralizados del frontend.
