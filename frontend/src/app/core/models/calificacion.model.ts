export interface CalificacionRequest {
  puntuacion: number;
  comentario?: string;
  idUsuario: number;
  idPublicacion: number;
}

export interface CalificacionResponse {
  id: number;
  puntuacion: number;
  comentario?: string | null;
  idUsuario: number;
  idPublicacion: number;
  creadoEn?: string | null;
  actualizadoEn?: string | null;
}
