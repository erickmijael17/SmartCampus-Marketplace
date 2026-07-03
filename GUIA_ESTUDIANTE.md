# Guia del estudiante - SmartCampus Marketplace MVP

Esta guia describe el entorno actual despues de la refactorizacion a **11 microservicios activos**. El frontend Angular siempre consume el backend por medio del Gateway.

## Microservicios activos

| Servicio | Ruta Gateway |
|----------|--------------|
| auth-ms | `/auth/**` |
| persona-ms | `/api/v1/personas/**` |
| producto-ms | `/api/v1/productos/**` |
| categoria-ms | `/api/v1/categorias/**` |
| publicacion-ms | `/api/v1/publicaciones/**` |
| media-ms | `/api/v1/media/**` |
| favoritos-ms | `/api/v1/favoritos/**` |
| calificacion-ms | `/api/v1/calificaciones/**` |
| chat-ms | `/api/v1/chats/**` |
| orden-ms | `/api/v1/ordenes/**` |
| pago-ms | `/api/v1/pagos/**` |

Servicios eliminados del MVP: `carrito-ms`, `inventario-ms`, `notification-ms`, `search-ms` y `catalogo-ms`.

## Modo DEV

Usa este modo para desarrollar localmente. La infraestructura base corre con Maven en puertos DEV y las bases de datos de microservicios se levantan con `compose-dev.yml`.

### 1. Crear redes Docker

Keycloak usa `ecom-prod-net` aunque se use en desarrollo. Kafka DEV y observabilidad DEV usan `ecom-dev-net`.

```bash
docker network create ecom-prod-net
docker network create ecom-dev-net
```

Si Docker indica que la red ya existe, puedes continuar.

### 2. Levantar Keycloak

Luego levantalo con el nombre nuevo (`keycloak`):

```bash
docker compose -f keycloak/compose.yml up -d
```

### 3. Levantar Kafka DEV

Luego levantalo con el nombre nuevo (`kafka-dev`):

```bash
docker compose -f kafka/compose-dev.yml up -d
```

### 4. Levantar bases de datos DEV

Luego levantarlas:

```bash
docker compose -f servicio/auth-ms/compose-dev.yml up -d
docker compose -f servicio/categoria-ms/compose-dev.yml up -d
docker compose -f servicio/producto-ms/compose-dev.yml up -d
docker compose -f servicio/publicacion-ms/compose-dev.yml up -d
docker compose -f servicio/media-ms/compose-dev.yml up -d
docker compose -f servicio/favoritos-ms/compose-dev.yml up -d
docker compose -f servicio/calificacion-ms/compose-dev.yml up -d
docker compose -f servicio/chat-ms/compose-dev.yml up -d
docker compose -f servicio/orden-ms/compose-dev.yml up -d
docker compose -f servicio/pago-ms/compose-dev.yml up -d
```

### 5. Levantar infraestructura DEV con Maven

Ejecutar cada comando en una terminal distinta:

```bash
mvn -f infra/config/pom.xml spring-boot:run
mvn -f infra/eureka/pom.xml spring-boot:run
mvn -f infra/gateway/pom.xml spring-boot:run
```

Puertos DEV:

| Componente | URL |
|------------|-----|
| Config Server | `http://localhost:18888` |
| Eureka | `http://localhost:18761` |
| Gateway | `http://localhost:18080` |

### 6. Levantar microservicios DEV con Maven

Ejecutar cada microservicio necesario en una terminal distinta:

```bash
mvn -f servicio/auth-ms/pom.xml spring-boot:run
mvn -f servicio/categoria-ms/pom.xml spring-boot:run
mvn -f servicio/producto-ms/pom.xml spring-boot:run
mvn -f servicio/publicacion-ms/pom.xml spring-boot:run
mvn -f servicio/media-ms/pom.xml spring-boot:run
mvn -f servicio/favoritos-ms/pom.xml spring-boot:run
mvn -f servicio/calificacion-ms/pom.xml spring-boot:run
mvn -f servicio/chat-ms/pom.xml spring-boot:run
mvn -f servicio/orden-ms/pom.xml spring-boot:run
mvn -f servicio/pago-ms/pom.xml spring-boot:run
```

Para probar solo login y catalogo publico, normalmente basta con:

```bash
mvn -f servicio/auth-ms/pom.xml spring-boot:run
mvn -f servicio/categoria-ms/pom.xml spring-boot:run
mvn -f servicio/producto-ms/pom.xml spring-boot:run
```

### 7. Levantar frontend DEV

```bash
cd frontend
npm install
npm start
```

## Modo PROD / Docker

Usa este modo para levantar contenedores completos en la red `ecom-prod-net`.

### Levantar infraestructura PROD

```bash
docker network create ecom-prod-net
docker compose -f keycloak/compose.yml up -d
docker compose -f infra/compose.yml up -d --build
docker compose -f kafka/compose.yml up -d
docker compose -f obs/compose.yml up -d
```

### Levantar microservicios MVP en PROD

```bash
docker compose -f servicio/auth-ms/compose.yml up -d --build
docker compose -f servicio/persona-ms/compose.yml up -d --build
docker compose -f servicio/categoria-ms/compose.yml up -d --build
docker compose -f servicio/producto-ms/compose.yml up -d --build
docker compose -f servicio/publicacion-ms/compose.yml up -d --build
docker compose -f servicio/media-ms/compose.yml up -d --build
docker compose -f servicio/favoritos-ms/compose.yml up -d --build
docker compose -f servicio/calificacion-ms/compose.yml up -d --build
docker compose -f servicio/chat-ms/compose.yml up -d --build
docker compose -f servicio/orden-ms/compose.yml up -d --build
docker compose -f servicio/pago-ms/compose.yml up -d --build
```

## Health checks

DEV:

```bash
curl http://localhost:18888/actuator/health
curl http://localhost:18761/eureka/apps
curl http://localhost:18080/actuator/health
```

PROD:

```bash
curl http://localhost:28082/actuator/health
```

## Probar por Gateway

DEV:

```bash
curl http://localhost:18080/api/v1/categorias
curl http://localhost:18080/api/v1/productos
curl http://localhost:18080/api/v1/productos/detalle/1
```

PROD:

```bash
curl http://localhost:28082/api/v1/categorias
curl http://localhost:28082/api/v1/productos
curl http://localhost:28082/api/v1/productos/detalle/1
```

No consumir microservicios directamente desde Angular ni desde pruebas de frontend. La ruta correcta siempre es:

```txt
Angular -> Gateway -> lb://SERVICIO -> microservicio
```
