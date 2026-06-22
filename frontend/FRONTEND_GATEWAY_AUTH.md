# Frontend Angular: autenticacion via Gateway

Esta documentacion describe solo la preparacion del frontend Angular para probar autenticacion contra `auth-ms` usando Spring Cloud Gateway como unico punto de entrada HTTP.

## Objetivo

El flujo esperado de autenticacion es:

```text
Angular -> Gateway -> auth-ms -> Keycloak
```

El frontend no debe consumir directamente Keycloak ni puertos internos de microservicios. Toda llamada HTTP debe salir hacia el Gateway.

## Gateway usado

La URL base del Gateway esta centralizada en:

```text
frontend/src/app/core/config/api.config.ts
```

Configuracion actual:

```ts
export const API_CONFIG = {
  gatewayBaseUrl: 'http://localhost:28082',
  endpoints: {
    auth: {
      login: '/auth/login',
      register: '/auth/register',
      me: '/auth/me'
    }
  }
};
```

Endpoint usado para login:

```text
POST http://localhost:28082/auth/login
```

En Gateway, la ruta `/auth/**` apunta internamente a:

```text
lb://auth-ms
```

## Servicios principales

### AuthApiService

Archivo:

```text
frontend/src/app/core/services/auth-api.service.ts
```

Responsabilidades:

- Ejecutar login contra Gateway.
- Preparar registro si el backend expone `/auth/register`.
- Preparar consulta de sesion si el backend expone `/auth/me`.
- No llamar directo a Keycloak.
- No llamar directo al puerto interno de `auth-ms`.

### SessionService

Archivo:

```text
frontend/src/app/core/services/session.service.ts
```

Responsabilidades:

- Guardar la sesion en `localStorage`.
- Leer el token actual.
- Limpiar la sesion.
- Exponer estado de autenticacion.
- Construir el valor `Bearer <token>`.

Clave usada en navegador:

```text
smartcampus-session
```

## Interceptor de token

Archivo:

```text
frontend/src/app/core/interceptors/auth-token.interceptor.ts
```

El interceptor agrega este header cuando existe token:

```text
Authorization: Bearer <token>
```

Esta registrado en:

```text
frontend/src/app/app.config.ts
```

con:

```ts
provideHttpClient(withInterceptors([authTokenInterceptor]))
```

## Rutas del frontend

Archivo:

```text
frontend/src/app/app.routes.ts
```

Rutas principales:

```text
/
/login
/register
/publish
/listing/:id
```

La ruta `/publish` esta protegida con `auth.guard.ts`.

Tambien existen alias de compatibilidad:

```text
/registro -> /register
/publicar -> /publish
/publicacion/:id -> /listing/:id
```

## Paginas relacionadas

Login:

```text
frontend/src/app/pages/login-page.component.ts
frontend/src/app/pages/login-page.component.html
frontend/src/app/pages/login-page.component.css
```

Register:

```text
frontend/src/app/pages/register-page.component.ts
frontend/src/app/pages/register-page.component.html
frontend/src/app/pages/register-page.component.css
```

Home:

```text
frontend/src/app/pages/home-page.component.ts
frontend/src/app/pages/home-page.component.html
frontend/src/app/pages/home-page.component.css
```

## Como probar

Desde `frontend/`:

```powershell
npm.cmd install
npm.cmd run build
npm.cmd start -- --host 127.0.0.1 --port 4200
```

Abrir:

```text
http://127.0.0.1:4200/login
```

Verificar en DevTools que el login llama a:

```text
POST http://localhost:28082/auth/login
```

Si el login responde correctamente, el frontend guarda la sesion en `localStorage` y vuelve a Home.

## Pendientes conocidos

- `auth-ms` expone actualmente `POST /auth/login`.
- No se encontro endpoint activo para `POST /auth/register`.
- No se encontro endpoint activo para `GET /auth/me`.
- La respuesta actual de login no incluye `userId`; algunas acciones como publicar pueden necesitar ese dato desde backend o desde un endpoint de perfil.

