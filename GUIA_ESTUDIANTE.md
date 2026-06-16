# Guía del Estudiante: Despliegue de SmartCampus Marketplace en Kubernetes

Esta guía está diseñada para que puedas desplegar, configurar y probar el proyecto **SmartCampus Marketplace** en un entorno de **Kubernetes** desde cero en cualquier computadora.

---

## 1. Requisitos del Entorno

Antes de iniciar, instala y verifica los siguientes componentes:
- **Java JDK 17** (asegúrate de configurar `JAVA_HOME`).
- **Maven** (verifica con `mvn -version`).
- **Docker Desktop** (con Kubernetes local habilitado en la configuración).
- **Skaffold** (herramienta de compilación y despliegue rápido).
- **Kubectl** (CLI de Kubernetes).
- **Helm** (para instalar complementos del clúster).

---

## 2. Configuración de DNS Locales

Dado que el clúster utiliza **Ingress** con nombres de dominio locales para enrutar el tráfico (y cert-manager para HTTPS), debemos mapear estos dominios a tu dirección IP local.

Abre tu archivo de hosts (en Windows: `C:\Windows\System32\drivers\etc\hosts` con permisos de Administrador; en Linux/macOS: `/etc/hosts`) y agrega las siguientes líneas:

```text
127.0.0.1   api.smartcampus.local
127.0.0.1   keycloak.smartcampus.local
127.0.0.1   grafana.smartcampus.local
```

---

## 3. Guía de Despliegue Paso a Paso (Desde Cero)

Sigue estos pasos en orden secuencial para aprovisionar tu clúster de Kubernetes:

### Paso 1: Instalar Ingress-Nginx Controller
El controlador Ingress se encarga de recibir el tráfico HTTP/HTTPS externo y enviarlo a los servicios correctos.
Instálalo en tu clúster ejecutando:
```bash
helm upgrade --install ingress-nginx ingress-nginx \
  --repo https://kubernetes.github.io/ingress-nginx \
  --namespace ingress-nginx --create-namespace
```
*Espera a que los pods de Ingress estén corriendo (`kubectl get pods -n ingress-nginx`).*

### Paso 2: Instalar Cert-Manager
Cert-Manager gestiona de forma automatizada los certificados TLS/HTTPS dentro de tu clúster.
Instálalo ejecutando:
```bash
helm upgrade --install cert-manager cert-manager \
  --repo https://charts.jetstack.io \
  --namespace cert-manager --create-namespace \
  --set installCRDs=true
```
*Espera a que cert-manager esté completamente listo (`kubectl get pods -n cert-manager`).*

### Paso 3: Inicializar la Base de Datos de Keycloak y Keycloak
Keycloak se encarga de centralizar la identidad de tus usuarios y los roles (**`ADMIN`**, **`ESTUDIANTE`**, **`VENDEDOR`**, **`MODERADOR`**).

Despliega el motor PostgreSQL y el servidor Keycloak aplicando la Kustomización:
```bash
kubectl apply -k infra/keycloak/k8s/
```
*Esto creará la base de datos persistente, importará el realm `smartcampus` y arrancará Keycloak.*

### Paso 4: Desplegar el Config Server & Secreto Global
Para propagar las propiedades OAuth2 a todos los microservicios, primero levantamos las propiedades comunes y los secretos compartidos:
```bash
# Crear secretos globales (Credenciales Grafana y JWT)
kubectl apply -f k8s/base/ecom-global-secret.yaml

# Crear el emisor de certificados cert-manager (SSL)
kubectl apply -f k8s/base/cert-manager-issuer.yaml

# Desplegar Config Server
kubectl apply -k infra/config/k8s/
```

### Paso 5: Desplegar Eureka, Gateway y Kafka
```bash
# 1. Desplegar Eureka Server (Service Discovery)
kubectl apply -f infra/eureka/k8s/

# 2. Desplegar API Gateway con Ingress TLS
kubectl apply -f infra/gateway/k8s/

# 3. Desplegar Clúster Apache Kafka
kubectl apply -f kafka/k8s/
```

### Paso 6: Desplegar Microservicios de Negocio
Despliega todos los microservicios Java pre-configurados como Resource Servers:
```bash
kubectl apply -f servicio/auth-ms/k8s/
kubectl apply -f servicio/calificacion-ms/k8s/
kubectl apply -f servicio/carrito-ms/k8s/
kubectl apply -f servicio/catalogo-ms/k8s/
kubectl apply -f servicio/categoria-ms/k8s/
kubectl apply -f servicio/chat-ms/k8s/
kubectl apply -f servicio/favoritos-ms/k8s/
kubectl apply -f servicio/inventario-ms/k8s/
kubectl apply -f servicio/media-ms/k8s/
kubectl apply -f servicio/notification-ms/k8s/
kubectl apply -f servicio/orden-ms/k8s/
kubectl apply -f servicio/pago-ms/k8s/
kubectl apply -f servicio/persona-ms/k8s/
kubectl apply -f servicio/producto-ms/k8s/
kubectl apply -f servicio/publicacion-ms/k8s/
kubectl apply -f servicio/search-ms/k8s/
```

### Paso 7: Desplegar el Stack de Observabilidad
Instala el monitoreo centralizado (Loki, Promtail, Prometheus y Grafana con Ingress TLS):
```bash
kubectl apply -f obs/k8s/
```

---

## 4. Despliegue Rápido en un Solo Comando (Skaffold)

Si estás desarrollando activamente y quieres compilar imágenes de código local, sincronizar cambios en caliente y desplegar todo de una sola vez, utiliza **Skaffold** desde la raíz del proyecto:

```bash
skaffold dev --load-restrictor=LoadRestrictionsNone
```
*Skaffold se encargará de compilar los microservicios mediante Maven y cargarlos en Kubernetes por ti.*

---

## 5. Accesos y Credenciales

Una vez completado el despliegue, puedes acceder a las plataformas web:

| Plataforma | URL | Credenciales |
| :--- | :--- | :--- |
| **API Gateway** | `https://api.smartcampus.local` | Endpoint público unificado |
| **Keycloak Admin** | `https://keycloak.smartcampus.local` | Usuario: `admin` / Clave: `admin_secure_pass_2026` |
| **Grafana** | `https://grafana.smartcampus.local` | Usuario: `admin` / Clave: `grafana_secure_pass_2026` |
| **Eureka Discovery** | `http://localhost:8761` (port-forward) | Panel de monitoreo de servicios Java |

*Nota: Para habilitar el puerto de Eureka localmente, ejecuta:*
```bash
kubectl port-forward svc/eureka 8761:8761 -n ecom
```

---

## 6. ¿Cómo Probar la Autenticación y Autorización?

### Flujo de Autenticación de Retrocompatibilidad
El microservicio `auth-ms` actúa como un proxy transparente de transición hacia Keycloak. Para obtener un Token JWT firmado por Keycloak:

1. Realiza una petición `POST` a la ruta antigua:
   ```http
   POST https://api.smartcampus.local/auth/login
   Content-Type: application/json

   {
     "username": "usuario_prueba",
     "password": "clave_usuario"
   }
   ```
2. La respuesta contendrá el Token JWT emitido por Keycloak, sus roles y datos de expiración.

### Consumir Recursos Protegidos por Rol
Los microservicios validan dinámicamente la firma del JWT usando JWKS y extraen las autoridades del claim `realm_access.roles`.

* **Prueba 1 (Acceso Concedido)**: Realiza un `POST` a `/api/v1/productos/` (que requiere rol `ADMIN`) incluyendo el header `Authorization: Bearer <TU_TOKEN_JWT_ADMIN>`. Obtendrás una respuesta exitosa (HTTP 201/200).
* **Prueba 2 (Acceso Denegado)**: Realiza la misma petición con un token de rol normal (ej. `ESTUDIANTE`). Obtendrás un error HTTP 403 Forbidden.

---

## 7. Resolución de Problemas

- **El Ingress arroja error de certificado no seguro**: Esto ocurre porque estamos usando un emisor ACME local/Let's Encrypt sobre dominios `.local` que no son públicos. Para saltar la advertencia en tu navegador, haz clic en "Avanzado" -> "Continuar". Para pruebas programáticas o Curl, agrega el flag `-k` o `--insecure`.
- **Los microservicios tardan en arrancar**: Todos los pods tienen contenedores de inicialización (`initContainers`) que esperan a que el Config Server, Eureka y la base de datos respectiva estén listos. Si un pod no inicia, revisa su estado con `kubectl describe pod <nombre_pod> -n ecom`.
