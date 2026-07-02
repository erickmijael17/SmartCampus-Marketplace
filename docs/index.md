# SmartCampus Marketplace

**SmartCampus Marketplace** es una plataforma distribuida para compra, venta y gestión de productos o servicios dentro del entorno universitario UPeU. El sistema integra microservicios Spring Boot, configuración centralizada, descubrimiento con Eureka, Gateway, Keycloak, Kafka, PostgreSQL y observabilidad con Prometheus, Loki y Grafana.

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
| Despliegue | Docker Compose |
| Documentación | MkDocs Material, GitHub Pages |

---

## Microservicios detectados en el repositorio

| Microservicio | Responsabilidad |
|---|---|
| `auth-ms` | Login y delegación de autenticación hacia Keycloak |
| `catalogo-ms` | Catálogo base de categorías |
| `categoria-ms` | Gestión de categorías del marketplace |
| `producto-ms` | CRUD y detalle de productos |
| `inventario-ms` | Existencias y stock por producto |
| `carrito-ms` | Carrito de compra por usuario |
| `orden-ms` | Creación y gestión de órdenes |
| `pago-ms` | Registro y eventos de pagos |
| `persona-ms` | Perfil de usuarios UPeU |
| `publicacion-ms` | Publicaciones/anuncios del marketplace |
| `favoritos-ms` | Productos favoritos por usuario |
| `chat-ms` | Mensajería entre comprador y vendedor |
| `media-ms` | Archivos e imágenes |
| `calificacion-ms` | Reseñas y calificaciones |
| `notification-ms` | Notificaciones y eventos |
| `search-ms` | Búsqueda del marketplace |

!!! note "Nota sobre frontend"
    La rama publicada actualmente es `main` y en este checkout no aparece una carpeta `frontend/`. La sesión S11 documenta la integración esperada para la rama `frontend_Smart` cuando el cliente Angular esté disponible en el repositorio local.

---

## Cómo navegar esta documentación

1. Revisa [Producto del curso](producto-curso.md) para entender qué se evalúa.
2. Lee [Arquitectura](arquitectura.md) para ubicar componentes y flujos.
3. Sigue las 16 sesiones en orden desde [Índice de sesiones](sesiones/indice.md).
4. Usa [Desarrollo DEV](desarrollo.md), [Producción PROD](produccion.md) y [Puertos](puertos.md) para ejecutar el sistema.
5. Cierra con [Rúbrica](rubrica-evaluacion.md) para preparar la defensa técnica.
