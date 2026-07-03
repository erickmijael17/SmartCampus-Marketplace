import { Injectable } from '@angular/core';
import { KEYCLOAK_CONFIG, isKeycloakOidcEnabled } from '../config/keycloak.config';
import { generateCodeChallenge, generateCodeVerifier } from '../utils/pkce.util';

const PKCE_STATE_KEY = 'smartcampus-oidc-state';
const PKCE_VERIFIER_KEY = 'smartcampus-oidc-verifier';

export interface KeycloakTokenResponse {
  access_token: string;
  refresh_token?: string;
  expires_in: number;
  token_type: string;
  id_token?: string;
}

/**
 * Preparacion para migracion futura a Keycloak OIDC (Authorization Code + PKCE).
 * Flujo actual: auth-ms password grant via Gateway (`authMode: gateway-password`).
 */
@Injectable({ providedIn: 'root' })
export class KeycloakOidcService {
  isEnabled(): boolean {
    return isKeycloakOidcEnabled();
  }

  /** Inicia login OIDC con PKCE S256 (requiere ruta /auth/callback en el SPA). */
  async startLogin(returnUrl = '/'): Promise<void> {
    if (!this.isEnabled()) {
      throw new Error('Keycloak OIDC no esta habilitado. Use authMode gateway-password.');
    }

    const state = crypto.randomUUID();
    const codeVerifier = generateCodeVerifier();
    const codeChallenge = await generateCodeChallenge(codeVerifier);

    sessionStorage.setItem(PKCE_STATE_KEY, JSON.stringify({ state, returnUrl }));
    sessionStorage.setItem(PKCE_VERIFIER_KEY, codeVerifier);

    const params = new URLSearchParams({
      client_id: KEYCLOAK_CONFIG.clientId,
      redirect_uri: KEYCLOAK_CONFIG.redirectUri,
      response_type: 'code',
      scope: KEYCLOAK_CONFIG.scope,
      state,
      code_challenge: codeChallenge,
      code_challenge_method: KEYCLOAK_CONFIG.pkceMethod
    });

    window.location.assign(`${this.authorizationEndpoint()}?${params.toString()}`);
  }

  /**
   * Intercambia el authorization code por tokens.
   * Pendiente: conectar cuando auth-ms exponga proxy de token o el SPA tenga CORS directo a Keycloak.
   */
  async exchangeCodeForTokens(_code: string, _state: string): Promise<KeycloakTokenResponse> {
    throw new Error('exchangeCodeForTokens pendiente de integracion backend.');
  }

  /**
   * Silent refresh via iframe oculto (requiere silent-refresh.html y sesion SSO en Keycloak).
   * Pendiente: activar cuando authMode sea keycloak-oidc.
   */
  silentRefresh(): Promise<KeycloakTokenResponse | null> {
    if (!this.isEnabled()) {
      return Promise.resolve(null);
    }

    return new Promise((resolve) => {
      const iframe = document.createElement('iframe');
      iframe.style.display = 'none';
      iframe.src = `${KEYCLOAK_CONFIG.silentRefreshRedirectUri}?prompt=none`;

      const timeoutId = window.setTimeout(() => {
        cleanup();
        resolve(null);
      }, 10_000);

      const onMessage = (event: MessageEvent) => {
        if (event.origin !== window.location.origin) {
          return;
        }

        if (event.data?.type === 'oidc-silent-refresh') {
          cleanup();
          resolve(event.data.tokens ?? null);
        }
      };

      const cleanup = () => {
        window.clearTimeout(timeoutId);
        window.removeEventListener('message', onMessage);
        iframe.remove();
      };

      window.addEventListener('message', onMessage);
      document.body.appendChild(iframe);
    });
  }

  private authorizationEndpoint(): string {
    return `${KEYCLOAK_CONFIG.issuer}/protocol/openid-connect/auth`;
  }
}
