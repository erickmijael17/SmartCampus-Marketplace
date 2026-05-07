# 📦 Microservicio Producto

Este proyecto implementa el **Microservicio Producto**, responsable de gestionar productos dentro de una arquitectura de microservicios en evolución.

---

## 🧱 Estado del proyecto

Actualmente incluye:

- API REST funcional para productos
- Persistencia con MySQL
- Configuración por perfiles (`dev`, `prod`)
- Migraciones versionadas con Flyway en `prod`
- Contenerización con Docker
- Documentación OpenAPI/Swagger en `dev`
- Integración operativa con Config Server
- Integración operativa con Registry Server (Eureka)
- Integración operativa con API Gateway
- Enrutamiento dinámico con `lb://producto`
- Comunicación con `catalogo` mediante OpenFeign

---

## 🏗️ Arquitectura (visión)

```text
Client → Gateway → Microservicios → Eureka → Config Server
```

Este repositorio implementa únicamente el microservicio **Producto**.

Ubicación recomendada para clases/equipos:

- Cada microservicio (`catalogo`, `producto`, `[otro-ms]`) vive en su propio repositorio Git.
- Clonar cada repositorio de microservicio dentro de la carpeta `services` para trabajo integrado local.
- Mantener la infraestructura en un único repositorio `infra` (Config Server, Registry, Gateway, etc.).
- Estructura sugerida:

```text
ProyectosMS2026/
  infra/
    config-server/
    registry-server/
    gateway/
  services/
    catalogo/
    producto/
    [otro-ms]/
```

---

## ⚙️ Stack tecnológico base 2026

- Java 17
- Spring Boot 3.5.12
- Maven 3.9+
- MySQL 8.4
- Docker
- Docker Compose
- Flyway
- SpringDoc OpenAPI

Antes de ejecutar el proyecto, asegúrate de tener instalado:

```bash
java -version
mvn -v
docker -v
docker compose version
```

## Dependencias

- Spring Web
- Spring Data JPA
- Validation
- Lombok
- MySQL Driver
- Flyway
- Spring Boot Actuator
- Spring Boot DevTools
- SpringDoc OpenAPI WebMVC UI

---

## 📌 Dominio gestionado

La entidad principal es `Producto` y actualmente contiene:

- `id`
- `nombre`
- `descripcion`
- `idCategoria`

Tabla actual:

```sql
productos
```

Migración base:

```text
src/main/resources/db/migration/V1__create_productos_table.sql
```

---

## 🔌 Puertos utilizados

| Servicio | Puerto expuesto |
|----------|------------------|
| Aplicación (dev) | 9091 |
| Aplicación (prod) | 9092 |
| MySQL (dev) | 3391 |
| MySQL (prod) | 3392 |

---

## 🔄 Diferencia entre DEV y PROD

| Modo | Ejecución | Base de datos | Puerto app | Swagger | Flyway |
|------|-----------|---------------|------------|---------|--------|
| DEV | Maven | MySQL local o Docker | 9091 | habilitado | deshabilitado |
| PROD | Docker Compose | Docker | 9092 | deshabilitado | habilitado |

---

## 🧪 Endpoints principales

Base path:

```text
/api/v1/productos
```

Operaciones disponibles:

- `POST /api/v1/productos`
- `GET /api/v1/productos`
- `GET /api/v1/productos/{id}`
- `GET /api/v1/productos/detalle/{id}`
- `PUT /api/v1/productos/{id}`
- `DELETE /api/v1/productos/{id}`

Ejemplo de payload de creación:

```json
{
  "nombre": "Laptop Lenovo",
  "descripcion": "Equipo para laboratorio",
  "idCategoria": 1
}
```

Endpoint auxiliar para pruebas de gateway y balanceo:

- `GET /api/v1/producto/instancia`

```text
/api/v1/producto/instancia
```

Respuesta esperada:

```json
{
  "app": "producto",
  "port": "9092",
  "host": "nombre-del-host"
}
```

Endpoint de detalle con integración Feign:

- `GET /api/v1/productos/detalle/{id}`

Ejemplo de respuesta:

```json
{
  "id": 1,
  "nombre": "Laptop Lenovo",
  "descripcion": "Equipo para laboratorio",
  "idCategoria": 1,
  "categoria": {
    "id": 1,
    "nombre": "Tecnologia",
    "descripcion": "Productos tecnologicos"
  }
}
```

---

## Base de datos y migraciones

Convención actual:

- Los cambios de esquema deben quedar en SQL versionado.
- Flyway ejecuta automáticamente scripts en `src/main/resources/db/migration` cuando arranca `prod`.
- Ejemplo actual: `V1__create_productos_table.sql`.
- En `prod`, Hibernate no crea tablas; solo valida el esquema existente.
- En `dev`, Hibernate usa `ddl-auto: update` y Flyway está deshabilitado.

Flujo recomendado del equipo:

1. Diseñar o ajustar la tabla en SQL.
2. Probar el cambio en `dev`.
3. Crear una nueva versión SQL si corresponde (`V2`, `V3`, etc.).
4. Aplicar el cambio en `prod`.
5. Arrancar la app en `prod` y validar.

No modificar scripts ya ejecutados; crear siempre una nueva versión.

---

# 🚀 Ejecución en modo desarrollo (dev)

## 🔹 1. Clonar repositorio

Ejemplo:

```bash
git clone https://github.com/261dist/producto.git

cd producto
```

---

## 🔹 2. Levantar base de datos dev

```bash
docker compose -f docker-compose-dev.yml up -d
```

Esto levanta MySQL dev en el puerto `3391` con la base `db_producto`.

Si no usas Docker, también puedes apuntar a un MySQL local siempre que coincida con la configuración externa definida en `infra/config-repo/producto-dev.yml`.

---

## 🔹 3. Ejecutar aplicación

```bash
mvn spring-boot:run
```

Perfil activo por defecto:

```text
dev
```

---

## 🌐 Acceso DEV

API Productos:

```text
http://localhost:9091/api/v1/productos
```

Swagger:

```text
http://localhost:9091/swagger-ui/index.html
```

Health:

```text
http://localhost:9091/actuator/health
```

Instancia:

```text
http://localhost:9091/api/v1/producto/instancia
```

Acceso vía Gateway DEV:

```text
http://localhost:7091/api/v1/producto/instancia
```

---

# 🐳 Ejecución en modo producción (prod)

## 🔹 1. Crear archivo `.env`

```env
PRODUCTO_MYSQL_ROOT_PASSWORD=root
PRODUCTO_MYSQL_DATABASE=db_producto

SPRING_PROFILES_ACTIVE=prod
CONFIG_SERVER_URL=http://config-server:7071

PRODUCTO_DB_HOST=mysql-producto
PRODUCTO_DB_PORT=3306
PRODUCTO_DB_NAME=db_producto
PRODUCTO_DB_USERNAME=root
PRODUCTO_DB_PASSWORD=root
```

---

## 🔹 2. Levantar servicios

```bash
docker compose -f docker-compose.yml up -d
```

Esto levanta:

- MySQL prod en el puerto `3392`
- La aplicación `producto` en el puerto `9092`

---

## 🌐 Acceso PROD

API Productos:

```text
http://localhost:9092/api/v1/productos
```

Health:

```text
http://localhost:9092/actuator/health
```

Instancia:

```text
http://localhost:9092/api/v1/producto/instancia
```

Acceso vía Gateway PROD:

```text
http://localhost:7092/api/v1/producto/instancia
```

Swagger:

```text
deshabilitado en prod
```

---

# 📈 Escalado de la aplicación (múltiples instancias)

## 🔹 Opción rápida. No detener el entorno previo

Busca el contenedor de la aplicación con `docker ps -a`:

```bash
docker create --name producto22 --network ms-net --env-file .env -p 9099:9092 producto-prod-producto
docker network connect producto-int producto22
docker start producto22
```

## 🔹 Verificar

```bash
docker ps
```

---

## 🔹 Probar

- http://localhost:9092/api/v1/productos

Si quieres validar balanceo por Gateway, repite varias veces:

```text
http://localhost:7092/api/v1/producto/instancia
```

---

## 🔹 Finalizar

```bash
docker stop producto22
docker rm producto22
docker rmi producto-prod-producto
```

O limpiar el entorno completo:

```bash
docker rm -f producto22 producto33
docker compose -f docker-compose.yml down
```

---

## 🔹 Ejecución sin `.env` (opcional)

### PowerShell

```powershell
docker create --name producto33 --network ms-net -p 9098:9092 `
  -e SPRING_PROFILES_ACTIVE=prod `
  -e CONFIG_SERVER_URL=http://config-server:7071 `
  -e PRODUCTO_DB_HOST=mysql-producto `
  -e PRODUCTO_DB_PORT=3306 `
  -e PRODUCTO_DB_NAME=db_producto `
  -e PRODUCTO_DB_USERNAME=root `
  -e PRODUCTO_DB_PASSWORD=root `
  producto-prod-producto

docker network connect producto-int producto33
docker start producto33
```

---

# 🔗 Integración actual

## Config Server dev

```properties
SPRING_CONFIG_IMPORT=optional:configserver:http://localhost:7071
```

## Eureka dev

```properties
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:7081/eureka
```

## Gateway

```yaml
uri: lb://producto
```

Ruta de verificación de instancia por gateway:

```text
http://localhost:7091/api/v1/producto/instancia
http://localhost:7092/api/v1/producto/instancia
```

## Feign

La comunicación entre microservicios ya está operativa desde `producto` hacia `catalogo`:

- `producto` habilita clientes Feign con `@EnableFeignClients`
- `CatalogoClient` consume `GET /api/v1/categorias/{id}`
- `findDetalleById` arma una respuesta enriquecida con los datos de categoría
- el cliente Feign resuelve el servicio por nombre usando Eureka: `@FeignClient(name = "catalogo")`

Rutas funcionales para probar la integración:

```text
http://localhost:9091/api/v1/productos/detalle/{id}
http://localhost:9092/api/v1/productos/detalle/{id}
http://localhost:7091/api/v1/productos/detalle/{id}
http://localhost:7092/api/v1/productos/detalle/{id}
```

---

# Estado de avance

- [x] Config Server
- [x] Registry Server (Eureka)
- [x] API Gateway
- [x] Enrutamiento `lb://producto`
- [x] Feign
- [ ] Circuit Breaker
- [ ] Seguridad
- [ ] Gestion del trafico (filtros, politicas y control de peticiones)
- [ ] Observabilidad y trazabilidad
- [ ] Integracion con frontend

---

# Siguiente paso

Continuar con atributos de calidad sobre la base actual:

- agregar resiliencia con Circuit Breaker
- integrar seguridad con autenticacion y autorizacion
- aplicar gestion del trafico en Gateway
- fortalecer observabilidad y trazabilidad entre servicios
- habilitar integracion con frontend

---

# ⚠️ Alcance actual

Este proyecto no incluye aún:

- Circuit Breaker
- Seguridad
- Gestion del trafico en Gateway
- Observabilidad y trazabilidad
- Integracion con frontend

---

# 🧠 Concepto clave

Este proyecto es un microservicio que:

- es independiente
- puede escalar
- expone su propia API REST
- se integrará progresivamente al ecosistema completo

---

# 📌 Nota final

Este repositorio forma parte de una arquitectura de microservicios en evolución.

# 🔧 Anexo PR (flujo de trabajo con Git)

Este flujo permite trabajar con ramas, enviar cambios y versionar el proyecto de forma ordenada.

---

## 🔹 1. Actualizar repositorio

```bash
git branch
git pull origin main
```

---

## 🔹 2. Crear rama de trabajo

```bash
git checkout -b tarea/avance
```

Atención: no trabajes directamente sobre `main`.

---

## 🔹 3. Realizar cambios

```bash
git add .
git commit -m "feat: avance"
git push -u origin tarea/avance
```

---

## 🔹 4. Volver a `main` y limpiar rama

```bash
git checkout main
git pull origin main

git branch -d tarea/avance
git push origin --delete tarea/avance
```

---

## 🔹 5. Crear tag (versión estable)

```bash
git tag -a vs05-feign-r1 -m "Producto integrado con Feign para consultar categorias desde catalogo y documentacion actualizada"
git push origin vs05-feign-r1
```

---

## 🔹 6. Eliminar tag (si es necesario)

```bash
git tag -d vs05-feign-r1
git push origin --delete vs05-feign-r1
```

---

## 📚 Documentación adicional

Ver documentación operativa en:

https://upeuoficial.github.io/carrera-sistemas-docs-operativos/
