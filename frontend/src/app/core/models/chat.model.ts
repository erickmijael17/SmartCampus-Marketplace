export interface MensajeResponse {
  id: number;
  idConversacion: number;
  idRemitente: number | null;
  receptorId?: number | null;
  contenido: string;
  tipoRemitente?: string | null;
  tipoMensaje?: string | null;
  idOrden?: number | null;
  pagoId?: number | null;
  mpPaymentId?: string | null;
  leido?: boolean | null;
  creadoEn?: string | null;
  actualizadoEn?: string | null;
  nombreRemitente?: string | null;
}

export interface MensajeRequest {
  idRemitente: number;
  contenido: string;
  leido?: boolean;
}

export interface ConversacionRequest {
  idUsuario1: number;
  idUsuario2: number;
  publicacionId?: number | null;
  idOrden?: number | null;
  tipoChat?: string | null;
}

export interface ConversacionResponse {
  id: number;
  idUsuario1: number;
  idUsuario2: number;
  publicacionId?: number | null;
  idOrden?: number | null;
  tipoChat?: string | null;
  creadoEn?: string | null;
  actualizadoEn?: string | null;
  
  nombreUsuario1?: string | null;
  nombreUsuario2?: string | null;
  ultimoMensaje?: string | null;
  tipoUltimoMensaje?: string | null;
  ultimoMensajeFecha?: string | null;
}

export interface ChatMessage {
  from: 'me' | 'them' | 'system';
  text: string;
  time: string;
  author?: string;
  createdAt?: string;
}

export interface ChatThread {
  id: number;
  idUsuario1: number;
  idUsuario2: number;
  name: string;
  avatar: string;
  subject: string;
  lastMsg: string;
  time: string;
  unread: number;
  messages: ChatMessage[];
  publicacionId?: number | null;
  idOrden?: number | null;
}
