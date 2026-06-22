export interface AuthLoginRequest {
  username: string;
  password: string;
}

export interface AuthRegisterRequest {
  username: string;
  password: string;
}

export interface AuthSession {
  userId?: number | null;
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  username: string;
  roles: string[];
}
