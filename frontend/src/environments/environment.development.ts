import { AppEnvironment } from './environment.model';

export const environment: AppEnvironment = {
  production: false,
  gatewayUrl: 'http://localhost:18080',
  gatewayCandidates: [
    { label: 'DEV', url: 'http://localhost:18080' },
    { label: 'PROD', url: 'http://localhost:28082' }
  ],
  gatewayProbeEnabled: true,
  authMode: 'gateway-password',
  sessionStorageMode: 'localStorage',
  keycloak: {
    enabled: false,
    issuer: 'http://127.0.0.1:8080/realms/smartcampus',
    clientId: 'marketplace-spa',
    redirectUri: 'http://localhost:4200/auth/callback',
    silentRefreshRedirectUri: 'http://localhost:4200/assets/silent-refresh.html',
    scope: 'openid profile email offline_access',
    pkceMethod: 'S256'
  }
};
