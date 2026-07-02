# Desarrollo DEV

## Requisitos

| Herramienta | Uso |
|---|---|
| Java 17 | Ejecutar Spring Boot |
| Maven 3.8+ | Compilar y probar módulos |
| Docker Desktop | PostgreSQL, Keycloak, Kafka y observabilidad |
| Node.js 20+ | Ejecutar frontend Angular |
| PowerShell o bash | Comandos de verificación |
| Git | Control de versiones |

---

## Arranque recomendado

### 1. Infraestructura base

```powershell
make compose-infra
make compose-keycloak
make compose-kafka
make compose-obs
```

```bash
make compose-infra
make compose-keycloak
make compose-kafka
make compose-obs
```

### 2. Un microservicio específico

```powershell
make compose-ms MS=auth-ms
make compose-ms MS=producto-ms
```

```bash
make compose-ms MS=auth-ms
make compose-ms MS=producto-ms
```

### 3. Frontend Angular

En la rama `frontend_Smart`:

```powershell
cd frontend
npm install
npm start
```

```bash
cd frontend
npm install
npm start
```

El cliente queda disponible en `http://localhost:4200` y consume el Gateway.

### 4. Ejecución Maven local

```powershell
mvn -f infra/config/pom.xml spring-boot:run
mvn -f infra/eureka/pom.xml spring-boot:run
mvn -f infra/gateway/pom.xml spring-boot:run
```

```bash
mvn -f infra/config/pom.xml spring-boot:run
mvn -f infra/eureka/pom.xml spring-boot:run
mvn -f infra/gateway/pom.xml spring-boot:run
```

Puertos esperados en Maven local:

| Componente | URL |
|---|---|
| Config Server | `http://localhost:18888/actuator/health` |
| Eureka | `http://localhost:18761` |
| Gateway | `http://localhost:18080/actuator/health` |

---

## Verificaciones rápidas con Docker Compose

```powershell
curl http://localhost:28082/actuator/health
curl http://localhost:28761
curl http://localhost:8080/realms/smartcampus/.well-known/openid-configuration
curl http://localhost:28085
curl http://localhost:23000
```

```bash
curl http://localhost:28082/actuator/health
curl http://localhost:28761
curl http://localhost:8080/realms/smartcampus/.well-known/openid-configuration
curl http://localhost:28085
curl http://localhost:23000
```

---

## Pruebas Maven por módulo

```powershell
mvn -f infra/config/pom.xml test
mvn -f infra/eureka/pom.xml test
mvn -f infra/gateway/pom.xml test
mvn -f servicio/producto-ms/pom.xml test
```

```bash
mvn -f infra/config/pom.xml test
mvn -f infra/eureka/pom.xml test
mvn -f infra/gateway/pom.xml test
mvn -f servicio/producto-ms/pom.xml test
```

---

## Archivos trabajados en DEV

| Archivo | Propósito |
|---|---|
| `infra/config/src/main/resources/application.yml` | Config Server |
| `infra/config/config-repo/*-dev.yml` | Configuración DEV por servicio |
| `infra/eureka/src/main/resources/application.yml` | Registro de servicios |
| `infra/gateway/src/main/resources/application.yml` | Bootstrap del Gateway |
| `servicio/*/src/main/resources/application.yml` | Importación del Config Server |
| `servicio/*/compose-dev.yml` | PostgreSQL o dependencias locales por servicio |
