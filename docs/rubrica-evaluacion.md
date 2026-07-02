# Rúbrica de evaluación

## Evaluación del producto

| Criterio | Evidencia | Nivel esperado |
|---|---|---|
| Servicio REST funcional | Endpoints CRUD y DTOs | Funciona por Gateway |
| Configuración centralizada | `infra/config/config-repo` | DEV/PROD separados |
| Descubrimiento | Eureka | Servicios registrados |
| Gateway | `lb://...` | Punto único de acceso |
| Seguridad | Keycloak + JWT | Rutas protegidas por rol |
| Resiliencia | Feign + Circuit Breaker | Fallback controlado |
| Kafka | Eventos orden/pago | Productor y consumidor |
| Consistencia | Estados de orden/pago | Idempotencia o compensación documentada |
| Observabilidad | Prometheus, Loki, Grafana | Health, métricas y logs |
| Frontend | Angular | Integrado por Gateway |
| Despliegue | Docker Compose | Reproducible |
| Documentación | MkDocs | Navegable y defendible |

---

## Rúbrica por sesión

| Sección | Peso sugerido | Qué se evalúa |
|---|---:|---|
| Introducción | 10% | Propósito, resultado y motivación |
| Explica | 20% | Conceptos, diagramas y arquitectura |
| Aplica | 35% | Comandos reales, archivos y evidencias |
| Crea | 20% | Actividad autónoma y extensión |
| Cierre evaluativo | 15% | Checklist, preguntas y defensa |

---

## Checklist de defensa

- Puedo explicar qué problema resuelve cada microservicio.
- Puedo levantar infraestructura y servicios con Docker Compose.
- Puedo obtener un token y llamar un endpoint protegido.
- Puedo explicar una ruta del Gateway.
- Puedo mostrar un evento Kafka.
- Puedo abrir Grafana y revisar métricas/logs.
- Puedo identificar qué archivo cambiar para DEV o PROD.
- Puedo justificar las decisiones de seguridad.
