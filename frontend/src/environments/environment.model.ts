export type AuthMode = 'gateway-password' | 'keycloak-oidc';

export type GatewayLabel = 'PROD' | 'DEV' | 'NONE';

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
  sessionStorageMode: 'localStorage' | 'sessionStorage';
  keycloak: KeycloakEnvironmentConfig;
}
