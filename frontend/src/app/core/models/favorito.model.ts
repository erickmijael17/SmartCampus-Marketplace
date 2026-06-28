export interface FavoritoRequest {
  idUsuario: number;
  idPublicacion: number;
}

export interface FavoritoResponse {
  id: number;
  idUsuario: number;
  idPublicacion: number;
  creadoEn?: string | null;
  actualizadoEn?: string | null;
}
