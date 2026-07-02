import { AppEnvironment } from './environment.model';

/**
 * Configuracion de PRODUCCION.
 * Sustituir `gatewayUrl` por la URL publica real del Gateway antes del despliegue.
 */
export const environment: AppEnvironment = {
  production: true,
  gatewayUrl: 'https://api.smartcampus.upeu.edu.pe',
  gatewayCandidates: [{ label: 'PROD', url: 'https://api.smartcampus.upeu.edu.pe' }],
  /** En produccion se usa URL fija; no se sondea localhost. */
  gatewayProbeEnabled: false,
  authMode: 'gateway-password',
  mercadoPagoOpenMode: 'same-tab',
  sessionStorageMode: 'sessionStorage',
  keycloak: {
    enabled: false,
    issuer: 'https://auth.smartcampus.upeu.edu.pe/realms/smartcampus',
    clientId: 'marketplace-spa',
    redirectUri: 'https://marketplace.smartcampus.upeu.edu.pe/auth/callback',
    silentRefreshRedirectUri: 'https://marketplace.smartcampus.upeu.edu.pe/assets/silent-refresh.html',
    scope: 'openid profile email offline_access',
    pkceMethod: 'S256'
  }
};
