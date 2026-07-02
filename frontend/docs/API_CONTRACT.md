# Contrato API Frontend ↔ Backend (SmartCampus Marketplace)

Documento de referencia para integración Angular con Spring Cloud Gateway. Toda petición HTTP del SPA debe salir hacia el Gateway; nunca hacia puertos internos de microservicios ni Keycloak directo (salvo migración OIDC futura).

## Arquitectura

```text
Angular → GatewayService + API_CONFIG → Gateway → lb://MICROSERVICIO → Controller
```

| Entorno | URL Gateway | Detección |
|---------|-------------|-----------|
| Desarrollo | `http://localhost:18080` (prioridad), fallback `http://localhost:28082` | Probe `/actuator/health` |
| Producción | `https://api.smartcampus.upeu.edu.pe` (configurable en `environment.ts`) | URL fija, sin probe |

**Header de autenticación (rutas protegidas):**

```http
Authorization: Bearer <accessToken>
```

Configuración centralizada: `frontend/src/app/core/config/api.config.ts`.

---

## Autenticación (`auth-ms` → `/auth/**`)

Flujo actual: **password grant vía Gateway** (`authMode: gateway-password`).

| Método | Ruta | Auth | Request | Response |
|--------|------|------|---------|----------|
| POST | `/auth/login` | No | `{ username, password }` | `AuthSession` |
| POST | `/auth/register` | No | `AuthRegisterRequest` | `AuthSession` |
| GET | `/auth/me` | Bearer | — | `AuthMeResponse` |

### AuthLoginRequest

```json
{ "username": "string", "password": "string" }
```

### AuthRegisterRequest

```json
{
  "username": "string?",
  "email": "string?",
  "password": "string",
  "fullName": "string?",
  "userType": "string?",
  "career": "string?",
  "cycle": "string?"
}
```

### AuthSession (login/register)

```json
{
  "accessToken": "string",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "username": "string",
  "roles": ["USER"]
}
```

Tras login/register el frontend enriquece la sesión:

1. `GET /auth/me` → roles, email, `userId` (UUID Keycloak en `sub`)
2. `GET /api/v1/personas/me` → `userId` numérico y `personaId`

### AuthMeResponse

```json
{
  "username": "string?",
  "email": "string?",
  "userId": "uuid-keycloak | number?",
  "roles": ["USER"],
  "accessToken": "string?"
}
```

### Errores

| Código | Comportamiento frontend |
|--------|-------------------------|
| 401 | Interceptor limpia sesión y redirige a `/login` (excepto `/auth/login`, `/auth/register`, `/auth/me`) |
| 403 | Mensaje de permisos insuficientes |
| 503 | Gateway o microservicio no disponible |

---

## Personas (Fusionado en `auth-ms` → `/auth/profile`)

| Método | Ruta | Auth | Uso frontend |
|--------|------|------|--------------|
| GET | `/auth/profile` | Bearer | Perfil propio, enriquecimiento post-login |
| PUT | `/auth/profile` | Bearer | Actualizar perfil |
| GET | `/auth/users/{id}`| Bearer | Datos de vendedor/comprador (backend API) |

### PersonaResponse

```json
{
  "id": 1,
  "userId": 42,
  "nombres": "string",
  "apellidos": "string",
  "email": "string",
  "telefono": "string?",
  "codigoUniversitario": "string?",
  "tipoUsuario": "ESTUDIANTE | DOCENTE | ADMINISTRATIVO | EGRESADO",
  "carrera": "string?",
  "facultad": "string?",
  "fotoPerfilUrl": "string?",
  "activo": true
}
```

---

## Catálogo y productos

### Categorías (`categoria-ms`)

| Método | Ruta | Auth | Uso |
|--------|------|------|-----|
| GET | `/api/v1/categorias` | Público* | Filtros home, formulario publicar |

\*Según política del microservicio; el frontend envía Bearer si hay sesión.

### Productos (`producto-ms`)

| Método | Ruta | Auth | Uso |
|--------|------|------|-----|
| GET | `/api/v1/productos` | Público* | Listado home |
| GET | `/api/v1/productos/detalle/{id}` | Público* | Detalle listing |
| POST | `/api/v1/productos` | Bearer | Crear producto al publicar |

### ProductRequest (publicar)

```json
{
  "titulo": "string",
  "descripcion": "string?",
  "precio": 25.5,
  "moneda": "PEN",
  "estado": "DISPONIBLE",
  "idCategoria": 1,
  "idVendedor": 42
}
```

---

## Publicaciones y media (capa social)

### Publicaciones (`publicacion-ms`)

| Método | Ruta | Auth | Uso |
|--------|------|------|-----|
| GET | `/api/v1/publicaciones` | Bearer | Resolver favoritos, imágenes |
| GET | `/api/v1/publicaciones/{id}` | Bearer | Detalle social |
| POST | `/api/v1/publicaciones` | Bearer | Primera capa al publicar |

### PublicacionRequest

```json
{
  "titulo": "string",
  "descripcion": "string?",
  "precio": 25.5,
  "estado": "ACTIVA",
  "idUsuario": 42,
  "idCategoria": 1
}
```

### Media (`media-ms`)

| Método | Ruta | Auth | Uso |
|--------|------|------|-----|
| GET | `/api/v1/media` | Bearer | Listado metadata |
| POST | `/api/v1/media` | Bearer | Registrar URL de imagen (no multipart) |

---

## Comercio

| Método | Ruta | MS | Uso |
|--------|------|-----|-----|
| POST | `/api/v1/ordenes` | orden-ms | Checkout |
| POST | `/api/v1/pagos` | pago-ms | Pago tras orden |

### CheckoutRequest

```json
{
  "idComprador": 10,
  "idProducto": 5,
  "cantidad": 1,
  "precioUnitario": 25.5,
  "metodoPago": "TARJETA",
  "referenciaTransaccion": "string?"
}
```

---

## Social

### Favoritos (`favoritos-ms`)

| Método | Ruta | Auth |
|--------|------|------|
| GET | `/api/v1/favoritos` | Bearer |
| POST | `/api/v1/favoritos` | Bearer |
| DELETE | `/api/v1/favoritos/{id}` | Bearer |

Body POST: `{ "idPublicacion": number, "idUsuario": number }`

### Calificaciones (`calificacion-ms`)

| Método | Ruta | Auth |
|--------|------|------|
| GET | `/api/v1/calificaciones` | Bearer |
| POST | `/api/v1/calificaciones` | Bearer |

---

## Chat (`chat-ms`)

| Método | Ruta | Auth | Estado |
|--------|------|------|--------|
| GET | `/api/v1/chats` | Bearer | Integrado |
| GET | `/api/v1/chats/{id}` | Bearer | Integrado |
| POST | `/api/v1/chats` | Bearer | Crear conversación |
| GET | `/api/v1/chats/{id}/mensajes` | Bearer | Integrado |
| POST | `/api/v1/chats/{id}/mensajes` | Bearer | Integrado |

Body POST conversación: `{ "idUsuario1": number, "idUsuario2": number }`

Body POST mensaje: `{ "idRemitente": number, "contenido": string, "leido": false }`

---

## Flujo de publicación (frontend)

```text
POST /api/v1/publicaciones  → idPublicacion
POST /api/v1/productos      → idProducto (checkout)
POST /api/v1/media          → metadata imagen (URL externa opcional)
```

Requiere `userId` numérico de `persona-ms`.

---

## Identificadores de usuario

| Campo | Origen | Uso |
|-------|--------|-----|
| `keycloakUserId` | JWT / `/auth/me` (`sub` UUID) | Referencia identidad |
| `userId` | `persona-ms.userId` | Microservicios (Long) |
| `personaId` | `persona-ms.id` | Perfil |

Si falta `userId` numérico, publicar/comprar/chat fallan con error explícito en UI.

---

## Migración Keycloak OIDC (preparada, no activa)

Configuración en `environment.keycloak` y `KeycloakOidcService`:

| Aspecto | Estado |
|---------|--------|
| PKCE S256 | Utilidades listas (`pkce.util.ts`) |
| Authorization Code | `startLogin()` genera URL con `code_challenge` |
| Silent refresh | `silent-refresh.html` + `silentRefresh()` stub |
| Token exchange | Pendiente backend/CORS |
| Modo activo | `authMode: gateway-password` |

Para activar OIDC:

1. Registrar cliente público SPA en Keycloak con redirect URIs de producción.
2. Añadir URIs en `keycloak/realm-smartcampus.json` (redirect + webOrigins).
3. Cambiar `authMode` a `keycloak-oidc` y `keycloak.enabled: true`.
4. Implementar ruta `/auth/callback` y proxy de token (recomendado vía Gateway/auth-ms).

---

## Referencias en código

| Recurso | Archivo |
|---------|---------|
| Endpoints | `core/config/api.config.ts` |
| Gateway | `core/services/gateway.service.ts` |
| Auth | `core/services/auth-api.service.ts` |
| Sesión | `core/services/session.service.ts` |
| Interceptor JWT | `core/interceptors/auth-token.interceptor.ts` |
| Entornos | `src/environments/environment*.ts` |
| Keycloak OIDC | `core/services/keycloak-oidc.service.ts` |
