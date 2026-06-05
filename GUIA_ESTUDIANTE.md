# Guía del Estudiante: SmartCampus Marketplace

Esta guía está diseñada para que puedas inicializar, ejecutar y probar el proyecto SmartCampus Marketplace desde cero en cualquier computadora.

## 1. Preparación del Entorno

Antes de empezar, verifica que tienes instalado:
- **Java JDK 17** (Configura la variable `JAVA_HOME`).
- **Maven** (Verifica con `mvn -version`).
- **Docker Desktop** (Asegúrate de que esté iniciado).
- **Git**.

---

## 2. Inicialización en una Nueva Computadora

### Paso 1: Clonar el repositorio
```bash
git clone <url-del-proyecto>
cd SmartCampus-Marketplace
```

### Paso 2: Configurar Variables de Entorno
Copia los archivos de ejemplo `.env.example` a `.env` en las siguientes rutas:
- `infra/.env`
- `servicio/auth-ms/.env`
- `servicio/producto-ms/.env`
- (Y en cada microservicio que tenga un `.env.example`)

**Nota:** Asegúrate de que el `JWT_SECRET` sea el mismo en `infra/.env` y `servicio/auth-ms/.env`.

---

## 3. Modo Desarrollo (DEV) - Paso a Paso

En modo desarrollo, ejecutaremos las bases de datos y Kafka en Docker, pero los microservicios de Spring Boot directamente desde la terminal o tu IDE.

### A. Iniciar Bases de Datos, Kafka y Observabilidad (Docker)
Ejecuta estos comandos para preparar el entorno de soporte:

```bash
# 1. Iniciar Kafka (Mensajería)
cd kafka && docker compose -f compose-dev.yml up -d

# 2. Iniciar Observabilidad (Grafana, Prometheus, Loki)
cd ../obs && docker compose -f compose-dev.yml up -d

# 3. Iniciar Bases de Datos de los microservicios
# Debes entrar a cada carpeta en 'servicio/' y ejecutar su compose-dev.yml
cd ../servicio/auth-ms && docker compose -f compose-dev.yml up -d
cd ../producto-ms && docker compose -f compose-dev.yml up -d
cd ../catalogo-ms && docker compose -f compose-dev.yml up -d
cd ../carrito-ms && docker compose -f compose-dev.yml up -d
cd ../orden-ms && docker compose -f compose-dev.yml up -d
cd ../pago-ms && docker compose -f compose-dev.yml up -d
cd ../inventario-ms && docker compose -f compose-dev.yml up -d
```

### B. Iniciar Servicios de Infraestructura (Spring Boot - Maven)
Abre una terminal para cada uno y ejecuta en este orden:

1. **Config Server** (Puerto 18888):
   ```bash
   cd infra/config
   ./mvnw spring-boot:run
   ```
2. **Eureka Server** (Puerto 18761):
   ```bash
   cd ../eureka
   ./mvnw spring-boot:run
   ```
3. **API Gateway** (Puerto 18080):
   ```bash
   cd ../gateway
   ./mvnw spring-boot:run
   ```

### C. Iniciar Microservicios de Negocio (Spring Boot - Maven)
Para cada microservicio dentro de la carpeta `servicio/`, abre una terminal y ejecuta:

```bash
# Ejemplo para Auth MS
cd servicio/auth-ms
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Ejemplo para Producto MS
cd ../producto-ms
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Repetir para: catalogo-ms, carrito-ms, orden-ms, pago-ms, inventario-ms
```

---

## 4. De Desarrollo (DEV) a Producción (PROD)

En el entorno de **PROD**, simplificamos todo el despliegue usando contenedores. No necesitas ejecutar Maven manualmente, ya que Docker se encarga de compilar y empaquetar los servicios.

### Paso 1: Preparar el Entorno
Asegúrate de haber cerrado todas las aplicaciones Java que iniciaste en el modo DEV y haber detenido los contenedores de desarrollo:
```bash
# Detener contenedores de desarrollo
cd kafka && docker compose -f compose-dev.yml down
cd ../obs && docker compose -f compose-dev.yml down
# (Opcional) Detener BDs de servicios
cd ../servicio/auth-ms && docker compose -f compose-dev.yml down
```

### Paso 2: Despliegue Total con Docker Compose
Desde la raíz del proyecto, usaremos el archivo `compose.yml` de la carpeta `infra` que está configurado para orquestar todo el sistema (incluyendo Kafka, Observabilidad y Servicios).

```bash
cd infra
# Levantar todo el ecosistema (compila imágenes y levanta contenedores)
docker compose up -d --build
```

*Nota: Este proceso puede tardar varios minutos la primera vez mientras se descargan las imágenes base y se compilan los microservicios.*

### Paso 3: Verificación del Despliegue
Una vez que Docker termine, verifica que todos los contenedores estén corriendo:
```bash
docker compose ps
```

### Diferencia de Puertos y URLs en PROD:
- **API Gateway (Entrada única)**: `http://localhost:28082`
- **Eureka Dashboard**: `http://localhost:28761`
- **Config Server**: `http://localhost:28888/catalogo-ms/prod`
- **Grafana**: `http://localhost:23000`
- **Kafka UI**: `http://localhost:28085`

### Verificación en PROD:
1. Accede al Eureka de PROD: `http://localhost:28761`. Deberías ver todos los microservicios registrados.
2. Revisa el estado del Gateway: `http://localhost:28082/actuator/health`.
3. Los logs centralizados estarán en Grafana PROD: `http://localhost:23000`.

---

## 5. ¿Cómo Probar el Proyecto?

### A. Flujo de Prueba Sugerido
1. **Registro/Login**:
   - Envía un `POST` a `http://localhost:18080/auth/register` para crear un usuario.
   - Envía un `POST` a `http://localhost:18080/auth/login` para obtener tu **Token JWT**.
2. **Uso del Token**:
   - Para los demás servicios, debes incluir el encabezado: `Authorization: Bearer <TU_TOKEN_JWT>`.
3. **Explorar Productos**:
   - `GET http://localhost:18080/producto`
4. **Comprar**:
   - Agrega al carrito -> Crea una orden -> El servicio de pagos procesará la orden automáticamente vía Kafka.

### B. Dashboards de Control y Documentación
- **Eureka (Ver servicios activos)**: `http://localhost:18761` (DEV) o `http://localhost:28761` (PROD).
- **Kafka UI (Ver mensajes)**: `http://localhost:41085` (DEV).
- **Grafana (Métricas y Logs)**: `http://localhost:13000` (DEV) - Usuario: `admin` / Clave: `admin`.
- **Swagger (Documentación de API)**:
  Cada microservicio tiene su propia documentación Swagger accesible a través del Gateway en modo DEV:
  - Auth: `http://localhost:18080/auth-ms/swagger-ui/index.html`
  - Producto: `http://localhost:18080/producto-ms/swagger-ui/index.html`
  - Carrito: `http://localhost:18080/carrito-ms/swagger-ui/index.html`
  - (Y así sucesivamente para cada microservicio)

---

## 6. Resolución de Problemas Comunes

- **Error de conexión a Config Server**: Asegúrate de que `config-server` sea el primero en subir y que el puerto 18888 esté libre.
- **Microservicio no aparece en Eureka**: Verifica que tenga el perfil `-Dspring-boot.run.profiles=dev` activo.
- **Docker no inicia**: Verifica que tengas suficiente memoria RAM asignada a Docker Desktop (mínimo 4GB para este proyecto).
