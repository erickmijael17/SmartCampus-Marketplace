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
  userId?: string;
  roles?: string[];
  accessToken?: string;
  persona?: {
    id: number;
    userId: string;
    nombres: string;
    apellidos: string;
    email: string;
    telefono?: string | null;
    codigoUniversitario?: string | null;
    tipoUsuario: string;
    carrera?: string | null;
    facultad?: string | null;
    fotoPerfilUrl?: string | null;
    activo: boolean;
  } | null;
}

export interface AuthSession {
  userId?: string | null;
  personaId?: number | null;
  email?: string | null;
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  expiresAt?: number;
  username: string;
  roles: string[];
}
