export interface PublicacionRequest {
  titulo: string;
  descripcion?: string | null;
  precio: number;
  estado: string;
  idUsuario: number;
  idCategoria: number;
}

export interface PublicacionResponse {
  id: number;
  titulo: string;
  descripcion?: string | null;
  precio: number | string;
  estado: string;
  idUsuario: number;
  idCategoria: number;
  creadoEn?: string | null;
  actualizadoEn?: string | null;
}
