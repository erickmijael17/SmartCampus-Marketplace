# Guía del Estudiante: Despliegue de SmartCampus Marketplace en Docker Compose

Esta guía está diseñada para que puedas desplegar, configurar y probar el proyecto **SmartCampus Marketplace** en un entorno de Docker desde cero en cualquier computadora.

---

## 1. Requisitos del Entorno

Antes de iniciar, instala y verifica los siguientes componentes:
- **Java JDK 17** (asegúrate de configurar `JAVA_HOME`).
- **Maven** (verifica con `mvn -version`).

---



## 2. ¿Cómo Probar la Autenticación y Autorización?


El microservicio `auth-ms` actúa como un proxy transparente de transición hacia Keycloak. Para obtener un Token JWT firmado por Keycloak:

1. Realiza una petición `POST` a la ruta antigua:
   ```http
   POST http://localhost:28082/auth/login
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

