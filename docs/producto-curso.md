# Producto del curso

## Contexto académico

| Campo | Información |
|---|---|
| Curso | Desarrollo de Aplicaciones Distribuidas — DISTribuidas 2026-2 |
| Universidad | Universidad Peruana Unión — Juliaca, Puno, Perú |
| Docente | Sullon Macalupu Abel Angel |
| Semestre | 2026-2 |
| Ciclo | 5 |
| Créditos | 3 |
| Proyecto | SmartCampus Marketplace |

---

## Producto final exigido

> Sistema distribuido de microservicios end-to-end, configurable, escalable, seguro, resiliente, consistente, observable, integrado con frontend y defendido técnicamente.

SmartCampus Marketplace cumple ese objetivo mediante:

- Microservicios independientes en `servicio/`.
- Configuración centralizada en `infra/config/config-repo`.
- Registro y descubrimiento con Eureka.
- Gateway como punto único de acceso.
- Keycloak como autoridad de identidad.
- Kafka para eventos de órdenes, pagos y mensajes de chat.
- Frontend Angular 20 integrado por Gateway.
- Observabilidad con Prometheus, Loki, Promtail y Grafana.
- Despliegue reproducible con Docker Compose.

---

## Sistema de calificación

| Componente | Peso | Evidencia en esta documentación |
|---|---:|---|
| Evaluación del producto (EP) | 70% | Sistema funcionando, arquitectura, seguridad, Kafka, observabilidad y despliegue |
| Evaluación de sesiones (ES) | 20% | 16 archivos en `docs/sesiones/` |
| Competencia general (ECG) | 10% | Aporte individual, decisiones técnicas y defensa |

---

## Calendario de sesiones

| Unidad | Sesiones | Evaluación | Peso |
|---|---|---|---:|
| U1 — Sistema distribuido base | S1 a S5 | 18/09/2026 | 20% |
| U2 — Sistema distribuido robusto | S6 a S12 | 30/10/2026 | 35% |
| U3 — Validación y consolidación | S13 a S16 | 20/11/2026 | 35% |

---

## Evidencias esperadas

| Evidencia | Ruta o componente |
|---|---|
| Config Server | `infra/config` |
| Eureka | `infra/eureka` |
| Gateway | `infra/gateway` |
| Configuración por ambiente | `infra/config/config-repo/*-{dev,prod}.yml` |
| Keycloak | `keycloak/compose.yml` |
| Kafka | `kafka/compose.yml` |
| Observabilidad | `obs/compose.yml` |
| Frontend Angular | `frontend/` en rama `frontend_Smart` |
| Microservicios | `servicio/*` |
| Build y despliegue | `Makefile`, `compose.yml`, `Dockerfile` |
| Documentación | `docs/`, `mkdocs.yml`, `.github/workflows/docs.yml` |
