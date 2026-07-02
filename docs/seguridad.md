# Seguridad Keycloak y JWT

## Modelo de seguridad

SmartCampus usa Keycloak como autoridad de identidad. El realm principal es `smartcampus`.

```mermaid
flowchart LR
    UI["Cliente"]
    AUTH["auth-ms"]
    KC["Keycloak<br/>realm smartcampus"]
    GW["Gateway<br/>Resource Server"]
    MS["Microservicios<br/>Resource Server"]
    JWKS["JWKS<br/>/protocol/openid-connect/certs"]

    UI -->|"POST /auth/login"| AUTH
    AUTH --> KC
    KC -->|"JWT RS256"| AUTH
    AUTH --> UI
    UI -->|"Bearer token"| GW
    GW --> JWKS
    GW --> MS
    MS --> JWKS
```

---

## Configuración base

La configuración se hereda desde `infra/config/config-repo/application-dev.yml` y `application-prod.yml`.

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_URL:http://localhost:8080}/realms/smartcampus
          jwk-set-uri: ${KEYCLOAK_URL:http://localhost:8080}/realms/smartcampus/protocol/openid-connect/certs
```

---

## Roles de dominio

| Rol | Usuario demo | Permisos esperados |
|---|---|---|
| `ESTUDIANTE` | `estudiante@upeu.edu.pe` | Comprar, favoritos, chat |
| `VENDEDOR` | `vendedor@upeu.edu.pe` | Publicar y administrar sus productos |
| `ADMIN` | `admin@upeu.edu.pe` | CRUD completo y configuración |

---

## Rutas públicas y privadas

| Ruta | Estado esperado |
|---|---|
| `/auth/login` | Pública |
| `/actuator/health` | Pública |
| Swagger | Pública o restringida según ambiente |
| Escritura, borrado, pagos y administración | Protegidas por rol |

---

## Prueba manual

```http
POST http://localhost:28082/auth/login
Content-Type: application/json

{
  "username": "usuario-demo",
  "password": "clave-demo"
}
```

Respuesta esperada:

```json
{
  "accessToken": "<jwt>",
  "tokenType": "Bearer"
}
```

Uso del token:

```powershell
curl -H "Authorization: Bearer <jwt>" http://localhost:28082/api/v1/productos
```

```bash
curl -H "Authorization: Bearer <jwt>" http://localhost:28082/api/v1/productos
```

---

## Archivos clave

| Archivo | Función |
|---|---|
| `keycloak/compose.yml` | Levanta Keycloak |
| `infra/gateway/src/main/java/com/upeu/gateway/config/SecurityConfig.java` | Seguridad del Gateway |
| `servicio/*/config/SecurityConfig.java` | Seguridad por microservicio |
| `infra/config/config-repo/application-*.yml` | Issuer y JWKS compartidos |
| `servicio/auth-ms/src/main/java/com/upeu/auth/controller/AuthController.java` | Login |
