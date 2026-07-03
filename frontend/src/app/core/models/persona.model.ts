export type TipoUsuario = 'ESTUDIANTE' | 'DOCENTE' | 'ADMINISTRATIVO' | 'EGRESADO';

export interface PersonaRequest {
  nombres: string;
  apellidos: string;
  email: string;
  telefono?: string;
  codigoUniversitario?: string;
  tipoUsuario: TipoUsuario;
  carrera?: string;
  facultad?: string;
  fotoPerfilUrl?: string;
}

export interface PersonaResponse {
  id: number;
  userId: string;
  nombres: string;
  apellidos: string;
  email: string;
  telefono?: string | null;
  codigoUniversitario?: string | null;
  tipoUsuario: TipoUsuario;
  carrera?: string | null;
  facultad?: string | null;
  fotoPerfilUrl?: string | null;
  activo: boolean;
}
