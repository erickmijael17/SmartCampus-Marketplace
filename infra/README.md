# infra

Infraestructura base del sistema distribuido: Config Server, Eureka Server y API Gateway.

## Componentes

| Directorio | Puerto host DEV | Puerto host PROD | Puerto container | Rol |
|---|---:|---:|---:|---|
| `config/` | 18888 | 28888 | 8888 | Configuración centralizada |
| `eureka/` | 18761 | 28761 | 8761 | Service discovery |
| `gateway/` | 18080 | 28082 | 8080 | Punto único de entrada HTTP + JWT |

---

## DEV (Maven local)

Levantar cada servicio en su propia terminal, en este orden:

```bash
cd config    && mvn spring-boot:run   # http://localhost:18888
cd ../eureka && mvn spring-boot:run   # http://localhost:18761
cd ../gateway && mvn spring-boot:run  # http://localhost:18080
```

**Links:**
- Config Server: http://localhost:18888/catalogo-ms/dev
- Eureka Dashboard: http://localhost:18761
- Gateway: http://localhost:18080

> Los servicios backend Maven se conectan a estos puertos (18888, 18761, 18080).
> El Docker compose de infra usa puertos distintos (28888, 28761, 28082) para no pisarlos.

---

## PROD (Docker)

```bash
# Docker compila las imágenes y levanta todo (respeta dependencias)
docker compose up -d --build
```

Gateway espera a que Eureka esté saludable; Eureka espera a Config. Cada healthcheck usa el endpoint `/actuator/health` (hasta 40s de gracia inicial).

**Links:**
- Config Server: http://localhost:28888/catalogo-ms/prod
- Eureka Dashboard: http://localhost:28761
- Gateway health: http://localhost:28082/actuator/health

> `config` no se registra en Eureka. `eureka` y `gateway` aparecen en el dashboard.

Gateway necesita `JWT_SECRET` en `infra/.env`. Debe coincidir con el `JWT_SECRET` de `services/auth-ms/.env` para validar los tokens emitidos por auth.

Detalle específico del gateway: [`gateway/README.md`](gateway/README.md).

---

Documentación completa en [`../docs/`](../docs/).
