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

export interface AuthSession {
  userId?: number | null;
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  username: string;
  roles: string[];
}
