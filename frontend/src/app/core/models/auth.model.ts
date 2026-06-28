export interface AuthLoginRequest {
  username: string;
  password: string;
}

export interface AuthRegisterRequest {
  username?: string;
  email?: string;
  password?: string;
  fullName?: string;
  userType?: string;
  career?: string;
  cycle?: string;
}

export interface AuthMeResponse {
  username?: string;
  email?: string;
  /** Keycloak `sub` (UUID) when returned by /auth/me */
  userId?: string | number | null;
  roles?: string[];
  accessToken?: string;
}

export interface AuthSession {
  /** Numeric ID for microservices (persona-ms) when available */
  userId?: number | null;
  /** Persona record id from persona-ms */
  personaId?: number | null;
  /** Keycloak subject from JWT (/auth/me) */
  keycloakUserId?: string | null;
  email?: string | null;
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  /** Epoch ms when the access token expires */
  expiresAt?: number;
  username: string;
  roles: string[];
}
