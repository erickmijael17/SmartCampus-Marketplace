# Frontend Angular: Gateway y autenticacion

> Documentacion ampliada del contrato API: [`docs/API_CONTRACT.md`](docs/API_CONTRACT.md)

## Flujo obligatorio

```text
Angular → GatewayService + API_CONFIG → Gateway → auth-ms / microservicios
```

El frontend **no** llama Keycloak ni microservicios directamente (excepto migracion OIDC futura documentada en el contrato API).

## URLs del Gateway

| Entorno | Archivo | URL |
|---------|---------|-----|
| Produccion | `src/environments/environment.ts` | `https://api.smartcampus.upeu.edu.pe` (sustituir antes del deploy) |
| Desarrollo | `src/environments/environment.development.ts` | `http://localhost:18080` (+ probe fallback `28082`) |

Produccion usa URL fija (`gatewayProbeEnabled: false`). Desarrollo sondea candidatos con `/actuator/health`.

## Servicios principales

- **GatewayService** — resuelve base URL al arrancar
- **AuthApiService** — login/register/me + enriquecimiento persona-ms
- **SessionService** — token, expiracion, storage (`sessionStorage` en prod)
- **authTokenInterceptor** — header `Authorization: Bearer ...`, logout en 401

## Rutas activas

| Ruta | Componente | Guard |
|------|------------|-------|
| `/` | `pages/home/home.component` | — |
| `/login` | `pages/login-page.component` | guest |
| `/register` | `pages/register/register.component` | guest |
| `/publish` | `pages/publish-page.component` | auth |
| `/listing/:id` | `pages/listing-detail-page.component` | — |
| `/profile` | `pages/profile/profile.component` | auth |
| `/chat` | `pages/chat/chat.component` | auth |

## Build produccion

```powershell
cd frontend
npm run build:prod
```

Salida: `dist/frontend/`. Budgets configurados en `angular.json` (initial < 1 MB, styles component < 8 kB).

## Keycloak OIDC (preparacion)

Modo actual: `authMode: gateway-password`.

Preparado para PKCE + silent refresh en `KeycloakOidcService` cuando se active `authMode: keycloak-oidc`. Ver contrato API seccion "Migracion Keycloak OIDC".
