export type AuthMode = 'gateway-password' | 'keycloak-oidc';

export type GatewayLabel = 'PROD' | 'DEV' | 'NONE';

export type MercadoPagoOpenMode = 'new-tab' | 'same-tab';

export interface GatewayCandidate {
  label: GatewayLabel;
  url: string;
}

export interface KeycloakEnvironmentConfig {
  enabled: boolean;
  issuer: string;
  clientId: string;
  redirectUri: string;
  silentRefreshRedirectUri: string;
  scope: string;
  pkceMethod: 'S256';
}

export interface AppEnvironment {
  production: boolean;
  gatewayUrl: string;
  gatewayCandidates: GatewayCandidate[];
  gatewayProbeEnabled: boolean;
  authMode: AuthMode;
  mercadoPagoOpenMode: MercadoPagoOpenMode;
  sessionStorageMode: 'localStorage' | 'sessionStorage';
  keycloak: KeycloakEnvironmentConfig;
}
