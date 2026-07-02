# Puertos

## Puertos principales

Estos puertos corresponden al despliegue con Docker Compose, que es el flujo recomendado para validar el sistema completo.

| Componente | Puerto host | URL |
|---|---:|---|
| Gateway | 28082 | `http://localhost:28082` |
| Config Server | 28888 | `http://localhost:28888` |
| Eureka | 28761 | `http://localhost:28761` |
| Keycloak | 8080 | `http://localhost:8080` |
| Kafka UI | 28085 | `http://localhost:28085` |
| Grafana | 23000 | `http://localhost:23000` |

!!! note
    El README local usa `28888` para Config Server. Si tienes apuntes con `2888`, usa el valor real de `infra/compose.yml`.

---

## Puertos Maven local

Cuando se ejecutan los módulos de infraestructura directamente con Maven, se usan los puertos definidos en sus `application.yml`.

| Componente | Puerto local | Archivo |
|---|---:|---|
| Gateway | 18080 | `infra/gateway/src/main/resources/application.yml` |
| Config Server | 18888 | `infra/config/src/main/resources/application.yml` |
| Eureka | 18761 | `infra/eureka/src/main/resources/application.yml` |

---

## Rutas públicas de diagnóstico

| Componente | Endpoint |
|---|---|
| Gateway health | `http://localhost:28082/actuator/health` |
| Eureka dashboard | `http://localhost:28761` |
| Keycloak realm | `http://localhost:8080/realms/smartcampus` |
| JWKS | `http://localhost:8080/realms/smartcampus/protocol/openid-connect/certs` |

---

## Regla de exposición

Los microservicios de negocio no se consumen por puertos host. Se accede mediante Gateway:

```text
Cliente -> http://localhost:28082 -> Gateway -> lb://servicio
```

Esto evita acoplar el cliente a puertos internos y permite balanceo por Eureka.
