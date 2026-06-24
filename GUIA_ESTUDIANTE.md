# Guia del Estudiante: levantar SmartCampus Marketplace

Esta guia explica como levantar el backend de SmartCampus Marketplace despues de clonar el repositorio.

Hay dos formas de ejecucion:

- **DEV**: infraestructura y microservicios Java con Maven (`mvn spring-boot:run`), bases de datos y servicios externos con Docker.
- **PROD local**: todo el backend en contenedores Docker Compose.

No mezcles DEV y PROD al mismo tiempo si usan los mismos puertos.

## 1. Requisitos

- Java JDK 17
- Maven 3.8 o superior
- Docker Desktop o Docker Engine
- Git
- `curl` o Postman para probar endpoints

Clonar y entrar al proyecto:

```bash
git clone <URL_DEL_REPOSITORIO>
cd SmartCampus-Marketplace
```

## 2. Variables locales necesarias

Define `JWT_SECRET` antes de levantar servicios. Para desarrollo local puede ser un valor propio no productivo.

PowerShell:

```powershell
$env:JWT_SECRET="dev-local-jwt-secret-change-me"
```

Bash:

```bash
export JWT_SECRET="dev-local-jwt-secret-change-me"
```

Usa la misma terminal para ejecutar los comandos Docker o Maven que dependan de esta variable.

## 3. Puertos principales

| Servicio | DEV | PROD local |
|---|---:|---:|
| Config Server | `http://localhost:18888` | `http://localhost:28888` |
| Eureka | `http://localhost:18761` | `http://localhost:28761` |
| Gateway | `http://localhost:18080` | `http://localhost:28082` |
| Keycloak | `http://localhost:8080` | `http://localhost:8080` |
| Kafka UI | `http://localhost:41085` | `http://localhost:28085` |
| Grafana | `http://localhost:13000` | `http://localhost:23000` |

Los microservicios deben consumirse por Gateway. En PROD no exponen su puerto HTTP al host.

## 4. Ejecucion DEV con Maven y Docker

Usa esta opcion cuando quieras desarrollar, depurar o modificar codigo Java.

### 4.1 Crear redes Docker necesarias

```bash
docker network create ecom-prod-net
docker network create ecom-dev-net
```

Si alguna red ya existe, Docker mostrara un mensaje de error. Puedes ignorarlo y continuar.

### 4.2 Levantar servicios externos

Keycloak usa el realm `smartcampus` desde `keycloak/realm-smartcampus.json`:

```bash
docker compose -f keycloak/compose.yml up -d
```

Kafka y observabilidad en modo DEV:

```bash
docker compose -f kafka/compose-dev.yml up -d
docker compose -f obs/compose-dev.yml up -d
```

### 4.3 Levantar infraestructura Java con Maven

Abre una terminal por servicio y ejecuta desde la raiz del repositorio:

Terminal 1, Config Server:

```bash
mvn -f infra/config/pom.xml spring-boot:run
```

Terminal 2, Eureka:

```bash
mvn -f infra/eureka/pom.xml spring-boot:run -Dspring-boot.run.profiles=dev
```

Terminal 3, Gateway:

```bash
mvn -f infra/gateway/pom.xml spring-boot:run -Dspring-boot.run.profiles=dev
```

Verifica:

```bash
curl http://localhost:18888/actuator/health
curl http://localhost:18761
curl http://localhost:18080/actuator/health
```

### 4.4 Levantar bases de datos DEV

Levanta solo las bases de datos de los microservicios que vayas a probar:

```bash
docker compose -f servicio/auth-ms/compose-dev.yml up -d
docker compose -f servicio/catalogo-ms/compose-dev.yml up -d
docker compose -f servicio/producto-ms/compose-dev.yml up -d
docker compose -f servicio/carrito-ms/compose-dev.yml up -d
docker compose -f servicio/orden-ms/compose-dev.yml up -d
docker compose -f servicio/pago-ms/compose-dev.yml up -d
docker compose -f servicio/inventario-ms/compose-dev.yml up -d
docker compose -f servicio/persona-ms/compose-dev.yml up -d
docker compose -f servicio/publicacion-ms/compose-dev.yml up -d
docker compose -f servicio/categoria-ms/compose-dev.yml up -d
docker compose -f servicio/calificacion-ms/compose-dev.yml up -d
docker compose -f servicio/chat-ms/compose-dev.yml up -d
docker compose -f servicio/notification-ms/compose-dev.yml up -d
docker compose -f servicio/media-ms/compose-dev.yml up -d
docker compose -f servicio/favoritos-ms/compose-dev.yml up -d
docker compose -f servicio/search-ms/compose-dev.yml up -d
```

Puertos de PostgreSQL en DEV:

| Microservicio | Puerto host |
|---|---:|
| auth-ms | `15431` |
| catalogo-ms | `15432` |
| producto-ms | `15433` |
| carrito-ms | `15434` |
| pago-ms | `15435` |
| orden-ms | `15436` |
| inventario-ms | `15438` |
| persona-ms | `15440` |
| publicacion-ms | `15441` |
| categoria-ms | `15442` |
| calificacion-ms | `15443` |
| chat-ms | `15444` |
| notification-ms | `15445` |
| media-ms | `15446` |
| favoritos-ms | `15447` |
| search-ms | `15448` |

### 4.5 Levantar microservicios con Maven

Abre una terminal por microservicio. Ejemplo minimo recomendado:

```bash
mvn -f servicio/auth-ms/pom.xml spring-boot:run -Dspring-boot.run.profiles=dev
mvn -f servicio/catalogo-ms/pom.xml spring-boot:run -Dspring-boot.run.profiles=dev
mvn -f servicio/producto-ms/pom.xml spring-boot:run -Dspring-boot.run.profiles=dev
```

Para otros microservicios usa el mismo patron:

```bash
mvn -f servicio/<nombre-ms>/pom.xml spring-boot:run -Dspring-boot.run.profiles=dev
```

En DEV los microservicios usan `server.port: 0`, por eso Spring asigna un puerto aleatorio. Revisa Eureka en `http://localhost:18761` para confirmar que aparecen registrados.

### 4.6 Probar DEV por Gateway

Siempre llama por Gateway DEV:

```bash
curl http://localhost:18080/actuator/health
curl http://localhost:18080/api/v1/productos
curl http://localhost:18080/api/v1/carritos
curl http://localhost:18080/api/v1/inventarios
curl http://localhost:18080/api/v1/categorias
curl http://localhost:18080/api/v1/publicaciones
```

Probar Keycloak:

```bash
curl http://127.0.0.1:8080/realms/smartcampus/.well-known/openid-configuration
curl http://127.0.0.1:8080/realms/smartcampus/protocol/openid-connect/certs
```

Login por Gateway DEV:

```http
POST http://localhost:18080/auth/login
Content-Type: application/json

{
  "username": "usuario_prueba",
  "password": "clave_usuario"
}
```

## 5. Ejecucion PROD local con Docker Compose

Usa esta opcion cuando quieras levantar el sistema como contenedores, similar al despliegue local documentado.

### 5.1 Crear red PROD

```bash
docker network create ecom-prod-net
```

Si la red ya existe, continua.

### 5.2 Levantar infraestructura base

Desde la raiz del repositorio:

```bash
docker compose -f keycloak/compose.yml up -d
docker compose -f infra/compose.yml up -d --build
docker compose -f kafka/compose.yml up -d
docker compose -f obs/compose.yml up -d
```

Verifica:

```bash
curl http://localhost:28888/actuator/health
curl http://localhost:28761
curl http://localhost:28082/actuator/health
```

### 5.3 Levantar microservicios en PROD

Levanta los servicios que necesites:

```bash
docker compose -f servicio/auth-ms/compose.yml up -d --build
docker compose -f servicio/catalogo-ms/compose.yml up -d --build
docker compose -f servicio/producto-ms/compose.yml up -d --build
docker compose -f servicio/carrito-ms/compose.yml up -d --build
docker compose -f servicio/orden-ms/compose.yml up -d --build
docker compose -f servicio/pago-ms/compose.yml up -d --build
docker compose -f servicio/inventario-ms/compose.yml up -d --build
docker compose -f servicio/persona-ms/compose.yml up -d --build
docker compose -f servicio/publicacion-ms/compose.yml up -d --build
docker compose -f servicio/categoria-ms/compose.yml up -d --build
docker compose -f servicio/calificacion-ms/compose.yml up -d --build
docker compose -f servicio/chat-ms/compose.yml up -d --build
docker compose -f servicio/notification-ms/compose.yml up -d --build
docker compose -f servicio/media-ms/compose.yml up -d --build
docker compose -f servicio/favoritos-ms/compose.yml up -d --build
docker compose -f servicio/search-ms/compose.yml up -d --build
```

Para cualquier otro microservicio:

```bash
docker compose -f servicio/<nombre-ms>/compose.yml up -d --build
```

Revisa que se registren en Eureka PROD:

```text
http://localhost:28761
```

### 5.4 Probar PROD por Gateway

Siempre llama por Gateway PROD:

```bash
curl http://localhost:28082/actuator/health
curl http://localhost:28082/api/v1/productos
curl http://localhost:28082/api/v1/carritos
curl http://localhost:28082/api/v1/inventarios
curl http://localhost:28082/api/v1/categorias
curl http://localhost:28082/api/v1/publicaciones
```

Login por Gateway PROD:

```http
POST http://localhost:28082/auth/login
Content-Type: application/json

{
  "username": "usuario_prueba",
  "password": "clave_usuario"
}
```

## 6. Comandos Maven utiles

Compilar un servicio:

```bash
mvn -f servicio/auth-ms/pom.xml clean compile
mvn -f servicio/producto-ms/pom.xml clean compile
```

Ejecutar pruebas:

```bash
mvn -f infra/gateway/pom.xml test
mvn -f servicio/auth-ms/pom.xml test
```

Empaquetar:

```bash
mvn -f servicio/auth-ms/pom.xml clean package
```

## 7. Apagar el entorno

Apagar PROD:

```bash
docker compose -f servicio/auth-ms/compose.yml down
docker compose -f servicio/catalogo-ms/compose.yml down
docker compose -f servicio/producto-ms/compose.yml down
docker compose -f servicio/carrito-ms/compose.yml down
docker compose -f servicio/orden-ms/compose.yml down
docker compose -f servicio/pago-ms/compose.yml down
docker compose -f servicio/inventario-ms/compose.yml down
docker compose -f servicio/persona-ms/compose.yml down
docker compose -f servicio/publicacion-ms/compose.yml down
docker compose -f servicio/categoria-ms/compose.yml down
docker compose -f servicio/calificacion-ms/compose.yml down
docker compose -f servicio/chat-ms/compose.yml down
docker compose -f servicio/notification-ms/compose.yml down
docker compose -f servicio/media-ms/compose.yml down
docker compose -f servicio/favoritos-ms/compose.yml down
docker compose -f servicio/search-ms/compose.yml down
docker compose -f infra/compose.yml down
docker compose -f keycloak/compose.yml down
docker compose -f kafka/compose.yml down
docker compose -f obs/compose.yml down
```

Apagar dependencias DEV:

```bash
docker compose -f servicio/auth-ms/compose-dev.yml down
docker compose -f servicio/catalogo-ms/compose-dev.yml down
docker compose -f servicio/producto-ms/compose-dev.yml down
docker compose -f servicio/carrito-ms/compose-dev.yml down
docker compose -f servicio/orden-ms/compose-dev.yml down
docker compose -f servicio/pago-ms/compose-dev.yml down
docker compose -f servicio/inventario-ms/compose-dev.yml down
docker compose -f servicio/persona-ms/compose-dev.yml down
docker compose -f servicio/publicacion-ms/compose-dev.yml down
docker compose -f servicio/categoria-ms/compose-dev.yml down
docker compose -f servicio/calificacion-ms/compose-dev.yml down
docker compose -f servicio/chat-ms/compose-dev.yml down
docker compose -f servicio/notification-ms/compose-dev.yml down
docker compose -f servicio/media-ms/compose-dev.yml down
docker compose -f servicio/favoritos-ms/compose-dev.yml down
docker compose -f servicio/search-ms/compose-dev.yml down
docker compose -f keycloak/compose.yml down
docker compose -f kafka/compose-dev.yml down
docker compose -f obs/compose-dev.yml down
```

Para detener servicios Java levantados con Maven, usa `Ctrl + C` en cada terminal.

## 8. Problemas comunes

- Si Gateway responde `503`, revisa Eureka y confirma que el microservicio este registrado.
- Si Keycloak no inicia, confirma que exista la red `ecom-prod-net`.
- Si un microservicio no conecta a PostgreSQL en DEV, verifica que su `compose-dev.yml` este levantado y que el puerto no este ocupado.
- Si Docker indica que un puerto esta ocupado, deten el contenedor anterior o cambia el servicio que usa ese puerto.
- Si cambias configuracion en `infra/config/config-repo`, reinicia el servicio afectado.
