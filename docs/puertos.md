# Puertos

## Puertos principales

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
