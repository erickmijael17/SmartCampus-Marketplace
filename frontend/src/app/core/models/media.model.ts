export interface MediaFileRequest {
  url: string;
  tipoMime?: string;
  tamanoBytes?: number;
  idUploader: number;
  idPublicacion: number;
}

export interface MediaFileResponse {
  id: number;
  url: string;
  originalUrl?: string | null;
  urlOriginal?: string | null;
  enlace?: string | null;
  ruta?: string | null;
  uri?: string | null;
  tipoMime?: string | null;
  tamanoBytes?: number | null;
  idUploader: number;
  idPublicacion: number;
  creadoEn?: string | null;
  actualizadoEn?: string | null;
}
