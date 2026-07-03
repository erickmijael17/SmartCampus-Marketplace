# Reporte frontend Gateway Auth

Fecha: 2026-06-22

## Objetivo

Preparar el frontend Angular para una primera prueba real de autenticacion contra `auth-ms` pasando por Spring Cloud Gateway.

Flujo esperado:

```text
Angular -> Gateway -> auth-ms -> Keycloak
```

## Archivos revisados

- `frontend/package.json`
- `frontend/src/app/app.routes.ts`
- `frontend/src/app/app.config.ts`
- `frontend/src/app/core/config/api.config.ts`
- `frontend/src/app/core/services/auth-api.service.ts`
- `frontend/src/app/core/services/session.service.ts`
- `frontend/src/app/core/models/auth.model.ts`
- `frontend/src/app/pages/login-page.component.ts`
- `frontend/src/app/pages/register-page.component.ts`
- `frontend/src/app/pages/home-page.component.ts`
- `frontend/src/app/pages/publish-page.component.ts`
- `frontend/src/app/pages/listing-detail-page.component.ts`
- `infra/config/config-repo/gateway-dev.yml`
- `infra/config/config-repo/gateway-prod.yml`
- `.agents/test-auth-ms-report.md`
- `.agents/fix-auth-ms-keycloak-gateway-report.md`
- `AGENTS.md`

## Archivos creados

- `frontend/src/app/core/interceptors/auth-token.interceptor.ts`
- `frontend/src/app/core/interceptors/README.md`
- `frontend/src/app/shared/components/navbar/README.md`
- `frontend/src/app/shared/components/loading/README.md`
- `frontend/src/app/shared/components/empty-state/README.md`
- `frontend/src/app/shared/layout/main-layout/README.md`
- `.agents/frontend-gateway-auth-report.md`

## Archivos modificados

- `.gitignore`
- `AGENTS.md`
- `estructura_proyecto.txt`
- `frontend/src/app/app.config.ts`
- `frontend/src/app/app.html`
- `frontend/src/app/app.routes.ts`
- `frontend/src/app/core/config/api.config.ts`
- `frontend/src/app/core/models/auth.model.ts`
- `frontend/src/app/core/services/auth-api.service.ts`
- `frontend/src/app/core/services/marketplace.service.ts`
- `frontend/src/app/core/services/session.service.ts`
- `frontend/src/app/pages/home-page.component.ts`
- `frontend/src/app/pages/home-page.component.html`
- `frontend/src/app/pages/listing-detail-page.component.ts`
- `frontend/src/app/pages/listing-detail-page.component.html`
- `frontend/src/app/pages/login-page.component.ts`
- `frontend/src/app/pages/login-page.component.html`
- `frontend/src/app/pages/publish-page.component.ts`
- `frontend/src/app/pages/register-page.component.ts`
- `frontend/src/app/pages/register-page.component.html`

## Gateway y endpoints

- URL base usada para Gateway: `http://localhost:28082`
- Endpoint final de login usado por Angular: `POST http://localhost:28082/auth/login`
- Ruta verificada en `gateway-dev.yml`: `Path=/auth/**` hacia `lb://auth-ms`
- Ruta verificada en `gateway-prod.yml`: `Path=/auth/**` hacia `lb://auth-ms`

## Cambios aplicados

- Centralizada la configuracion HTTP en `API_CONFIG`.
- `AuthApiService.login()` usa `API_CONFIG.endpoints.auth.login`.
- `AuthApiService.register()` apunta a `API_CONFIG.endpoints.auth.register`, pero `auth-ms` actualmente solo expone `/auth/login`.
- Agregado `AuthApiService.me()` para dejar preparado `/auth/me` si el backend lo implementa.
- `SessionService` normaliza la sesion, usa `localStorage`, expone `getToken()` y arma `Bearer <token>`.
- Agregado `authTokenInterceptor` con `HttpInterceptorFn`.
- Registrado el interceptor con `provideHttpClient(withInterceptors(...))`, compatible con Angular 20.
- Rutas minimas conectadas: `/`, `/login`, `/register`, `/publish`, `/listing/:id`.
- `/publish` queda protegido por `auth.guard.ts`.
- Se conservaron alias de compatibilidad desde `/registro`, `/publicar` y `/publicacion/:id`.
- Login sin credenciales hardcodeadas en el formulario.
- Register muestra aviso claro si el backend todavia no soporta registro.
- Home muestra estado de sesion, boton de cierre de sesion y boton de publicar condicionado por autenticacion.
- `.gitignore` ya no ignora todo `frontend/`; solo ignora dependencias, build y cache.
- `AGENTS.md` incluye la seccion `Frontend Angular Agent Guidelines`.

## Como levantar frontend

Desde `frontend/`:

```powershell
npm.cmd install
npm.cmd run build
npm.cmd start -- --host 127.0.0.1 --port 4200
```

URL local verificada:

```text
http://127.0.0.1:4200
```

## Como probar login

1. Levantar Config Server, Eureka, Gateway, Keycloak y `auth-ms`.
2. Abrir `http://127.0.0.1:4200/login`.
3. Ingresar credenciales existentes en Keycloak.
4. Verificar en DevTools que Angular llama a `POST http://localhost:28082/auth/login`.
5. Verificar que la respuesta se guarda en `localStorage` bajo `smartcampus-session`.
6. Volver a Home y confirmar que aparece el estado de sesion activa.

## Validaciones ejecutadas

`npm install`:

- `npm install` fallo en PowerShell porque `npm.ps1` esta bloqueado por execution policy.
- Se ejecuto `npm.cmd install`.
- Resultado: `up to date in 3s`.

`npm run build`:

- Se ejecuto `npm.cmd run build`.
- Resultado: build exitoso.
- Output: `Application bundle generation complete`.
- Carpeta generada: `frontend/dist/frontend` (ignorada por Git).

`npm start`:

- Se ejecuto `npm.cmd start -- --host 127.0.0.1 --port 4200` en proceso local.
- PID iniciado: `8716`.
- Verificacion HTTP: `http://127.0.0.1:4200` respondio `200`.

## Pendientes

- `auth-ms` actualmente expone `POST /auth/login`; no se encontro endpoint activo para `POST /auth/register` ni `GET /auth/me`.
- La respuesta actual de `auth-ms` no incluye `userId`; publicar productos puede requerir que backend agregue ese dato o que frontend lo obtenga desde un endpoint de perfil.
- La prueba end-to-end de login requiere que Keycloak tenga usuarios reales y que Config Server, Eureka, Gateway y `auth-ms` esten levantados.
- Revisar si conviene reemplazar los alias en espanol por redirecciones permanentes solo despues de estabilizar navegacion del frontend.
