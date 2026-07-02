# SmartCampus Marketplace

**SmartCampus Marketplace** es una plataforma distribuida para compra, venta y gestión de productos o servicios dentro del entorno universitario UPeU. El sistema integra microservicios Spring Boot, configuración centralizada, descubrimiento con Eureka, Gateway, Keycloak, Kafka, PostgreSQL, frontend Angular y observabilidad con Prometheus, Loki y Grafana.

Esta documentación combina una guía académica por sesiones con evidencia técnica del sistema real: diagramas Mermaid, rutas verificables, comandos ejecutables, configuración YAML y tablas de archivos trabajados.

---

## Producto final del curso

Sistema distribuido de microservicios end-to-end, configurable, escalable, seguro, resiliente, consistente, observable, integrado con frontend y defendido técnicamente.

| Componente | Tecnología en SmartCampus |
|---|---|
| Backend | Java 17, Spring Boot 3.x, Spring Cloud |
| Configuración | Spring Cloud Config Server |
| Registro | Eureka Server |
| Gateway | Spring Cloud Gateway |
| Seguridad | Keycloak realm `smartcampus`, JWT, JWKS |
| Datos | PostgreSQL, Flyway |
| Mensajería | Apache Kafka, Kafka UI |
| Observabilidad | Prometheus, Loki, Promtail, Grafana |
| Frontend | Angular 20, TypeScript, RxJS |
| Despliegue | Docker Compose |
| Documentación | MkDocs Material, GitHub Pages |

---

## Microservicios activos del MVP integrado

| Microservicio | Responsabilidad |
|---|---|
| `auth-ms` | Login y delegación de autenticación hacia Keycloak |
| `categoria-ms` | Gestión de categorías del marketplace |
| `producto-ms` | CRUD y detalle de productos |
| `orden-ms` | Creación y gestión de órdenes |
| `pago-ms` | Mercado Pago, confirmación, validación manual y eventos |
| `publicacion-ms` | Publicaciones/anuncios del marketplace |
| `favoritos-ms` | Productos favoritos por usuario |
| `chat-ms` | Mensajería, comprobantes y venta validada |
| `media-ms` | Archivos e imágenes |
| `calificacion-ms` | Reseñas y calificaciones |

!!! note "Actualización frontend"
    La rama `frontend_Smart` incorpora el cliente Angular 20, rutas protegidas, servicios HTTP por Gateway y flujo Mercado Pago conectado con `pago-ms` y `chat-ms`.

---

## Cómo navegar esta documentación

1. Revisa [Producto del curso](producto-curso.md) para entender qué se evalúa.
2. Lee [Arquitectura](arquitectura.md) para ubicar componentes y flujos.
3. Sigue las 16 sesiones en orden desde [Índice de sesiones](sesiones/indice.md).
4. Revisa [Frontend Angular](frontend-angular.md) y [Pagos Mercado Pago](pagos-mercadopago.md) para los flujos nuevos.
5. Usa [Desarrollo DEV](desarrollo.md), [Producción PROD](produccion.md) y [Puertos](puertos.md) para ejecutar el sistema.
6. Cierra con [Rúbrica](rubrica-evaluacion.md) para preparar la defensa técnica.
