import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, forkJoin, map, of, switchMap } from 'rxjs';
import { API_CONFIG } from '../config/api.config';
import {
  ChatMessage,
  ChatThread,
  ConversacionRequest,
  ConversacionResponse,
  MensajeRequest,
  MensajeResponse
} from '../models/chat.model';
import { GatewayService } from './gateway.service';
import { SessionService } from './session.service';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private readonly http = inject(HttpClient);
  private readonly gateway = inject(GatewayService);
  private readonly sessionService = inject(SessionService);

  getThreads(): Observable<ChatThread[]> {
    if (!this.sessionService.isAuthenticated()) {
      return of([]);
    }

    const userId = this.sessionService.personaId();
    return this.http.get<ConversacionResponse[]>(this.url(API_CONFIG.endpoints.chats.base)).pipe(
      switchMap((conversations) => {
        if (conversations.length === 0) {
          return of([] as ChatThread[]);
        }

        return forkJoin(
          conversations.map((conversation) =>
            this.getMessages(conversation.id).pipe(map((messages) => this.toThread(conversation, messages)))
          )
        );
      }),
      map(threads => threads.sort((a, b) => new Date(b.time).getTime() - new Date(a.time).getTime()))
    );
  }

  getConversation(id: number): Observable<ChatThread> {
    return this.http.get<ConversacionResponse>(this.url(API_CONFIG.endpoints.chats.detail(id))).pipe(
      switchMap((conversation) =>
        this.getMessages(id).pipe(map((messages) => this.toThread(conversation, messages)))
      )
    );
  }

  getOrCreateConversation(otherUserId: number, publicacionId?: number | null): Observable<ChatThread> {
    const currentUserId = this.requireUserId();

    return this.getThreads().pipe(
      switchMap((threads) => {
        const sameUsers = (thread: ChatThread) =>
          (thread.idUsuario1 === currentUserId && thread.idUsuario2 === otherUserId) ||
          (thread.idUsuario1 === otherUserId && thread.idUsuario2 === currentUserId);
        const existing = threads.find(
          (thread) => sameUsers(thread) && (publicacionId ? thread.publicacionId === publicacionId : true)
        ) ?? threads.find((thread) => sameUsers(thread) && !thread.publicacionId);

        if (existing) {
          return this.getConversation(existing.id);
        }

        const request: ConversacionRequest = {
          idUsuario1: currentUserId,
          idUsuario2: otherUserId,
          publicacionId: publicacionId ?? null
        };

        return this.http.post<ConversacionResponse>(this.url(API_CONFIG.endpoints.chats.base), request).pipe(
          switchMap((item) => this.getMessages(item.id).pipe(map((messages) => this.toThread(item, messages))))
        );
      })
    );
  }

  getMessages(conversationId: number): Observable<ChatMessage[]> {
    return this.http.get<MensajeResponse[]>(this.url(API_CONFIG.endpoints.chats.messages(conversationId))).pipe(
      map((items) => items.map((item) => this.toChatMessage(item)))
    );
  }

  sendMessage(conversationId: number, text: string): Observable<ChatMessage> {
    const currentUserId = this.requireUserId();
    const request: MensajeRequest = {
      idRemitente: currentUserId,
      contenido: text.trim(),
      leido: false
    };

    return this.http.post<MensajeResponse>(this.url(API_CONFIG.endpoints.chats.messages(conversationId)), request).pipe(
      map((item) => this.toChatMessage(item))
    );
  }

  private toThreadSummary(conversation: ConversacionResponse): ChatThread {
    const currentUserId = this.sessionService.personaId();
    const otherUserId =
      currentUserId !== null && conversation.idUsuario1 === currentUserId ? conversation.idUsuario2 : conversation.idUsuario1;
    
    let otherUserName = 'Sistema';
    if (otherUserId) {
      if (currentUserId !== null && conversation.idUsuario1 === currentUserId) {
        otherUserName = conversation.nombreUsuario2 || `Usuario #${otherUserId}`;
      } else {
        otherUserName = conversation.nombreUsuario1 || `Usuario #${otherUserId}`;
      }
    }

    return {
      id: conversation.id,
      idUsuario1: conversation.idUsuario1,
      idUsuario2: conversation.idUsuario2,
      name: otherUserName,
      avatar: `/assets/avatar-placeholder.svg`,
      subject: conversation.idOrden ? `Venta #${conversation.idOrden}` : `Conversacion #${conversation.id}`,
      lastMsg: conversation.ultimoMensaje || 'Sin mensajes aun',
      time: conversation.ultimoMensajeFecha ?? conversation.actualizadoEn ?? conversation.creadoEn ?? '',
      unread: 0,
      messages: [],
      publicacionId: conversation.publicacionId,
      idOrden: conversation.idOrden
    };
  }

  private toThread(conversation: ConversacionResponse, messages: ChatMessage[]): ChatThread {
    const summary = this.toThreadSummary(conversation);
    const lastMessage = messages.at(-1);

    return {
      ...summary,
      lastMsg: lastMessage?.text ?? summary.lastMsg,
      time: lastMessage?.createdAt ?? summary.time,
      messages
    };
  }

  private toChatMessage(message: MensajeResponse): ChatMessage {
    const currentUserId = this.sessionService.personaId();
    const isSystem =
      message.tipoRemitente === 'SISTEMA' ||
      message.tipoMensaje?.startsWith('SISTEMA') ||
      message.tipoMensaje?.startsWith('VENTA_CONFIRMADA');
    const fromMe = currentUserId !== null && message.idRemitente === currentUserId;

    let authorName = message.nombreRemitente || 'Sistema';
    if (!isSystem && !message.nombreRemitente) {
      if (fromMe) {
        authorName = this.sessionService.username() || 'Yo';
      } else if (message.idRemitente) {
        authorName = `Usuario #${message.idRemitente}`;
      } else {
        authorName = 'Usuario Desconocido';
      }
    }

    return {
      from: isSystem ? 'system' : fromMe ? 'me' : 'them',
      text: message.contenido,
      time: message.creadoEn ?? new Date().toISOString(),
      author: authorName,
      createdAt: message.creadoEn ?? new Date().toISOString()
    };
  }

  private requireUserId(): number {
    const personaId = this.sessionService.personaId();
    if (personaId === null || personaId <= 0) {
      throw new Error('Debes iniciar sesion para usar el chat.');
    }

    return personaId;
  }

  private url(path: string): string {
    return path.startsWith('http://') || path.startsWith('https://') ? path : this.gateway.baseUrl() + path;
  }
}
