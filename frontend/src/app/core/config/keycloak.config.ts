import { environment } from '../../../environments/environment';
import { KeycloakEnvironmentConfig } from '../../../environments/environment.model';

export const KEYCLOAK_CONFIG: KeycloakEnvironmentConfig = environment.keycloak;

export function isKeycloakOidcEnabled(): boolean {
  return environment.authMode === 'keycloak-oidc' && environment.keycloak.enabled;
}
