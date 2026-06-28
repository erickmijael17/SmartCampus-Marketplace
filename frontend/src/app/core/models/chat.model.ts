export interface MensajeResponse {
  id: number;
  idConversacion: number;
  idRemitente: number;
  contenido: string;
  leido?: boolean | null;
  creadoEn?: string | null;
  actualizadoEn?: string | null;
}

export interface MensajeRequest {
  idRemitente: number;
  contenido: string;
  leido?: boolean;
}

export interface ConversacionRequest {
  idUsuario1: number;
  idUsuario2: number;
}

export interface ConversacionResponse {
  id: number;
  idUsuario1: number;
  idUsuario2: number;
  creadoEn?: string | null;
  actualizadoEn?: string | null;
}

export interface ChatMessage {
  from: 'me' | 'them';
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
}
