import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { ChatMessage, ChatThread } from '../models/chat.model';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private readonly storagePrefix = 'smartcampus-chat-';

  getThreads(): Observable<ChatThread[]> {
    const defaultThreads: ChatThread[] = [
      {
        id: 1,
        name: 'Vendedor 1',
        avatar: 'https://i.pravatar.cc/150?u=v1',
        subject: 'Interesado en Libro de Matemáticas',
        lastMsg: 'Claro, podemos encontrarnos en la biblioteca',
        time: '10:00 AM',
        unread: 2,
        messages: []
      }
    ];
    return of(defaultThreads);
  }

  getMessages(productId: number): ChatMessage[] {
    const rawMessages = localStorage.getItem(this.storagePrefix + productId);
    if (!rawMessages) {
      return [];
    }

    try {
      return JSON.parse(rawMessages) as ChatMessage[];
    } catch {
      localStorage.removeItem(this.storagePrefix + productId);
      return [];
    }
  }

  sendMessage(threadOrId: ChatThread | number, textOrAuthor: string, text?: string): any {
    if (typeof threadOrId === 'number') {
      const author = textOrAuthor;
      const messageText = text || '';
      const message: ChatMessage = {
        from: 'me',
        text: messageText,
        time: new Date().toISOString(),
        author: author,
        createdAt: new Date().toISOString()
      };
      const messages = this.getMessages(threadOrId);
      messages.push(message);
      localStorage.setItem(this.storagePrefix + threadOrId, JSON.stringify(messages));
      return message;
    } else {
      const message: ChatMessage = {
        from: 'me',
        text: textOrAuthor,
        time: new Date().toISOString(),
        author: 'Me',
        createdAt: new Date().toISOString()
      };
      return of(message);
    }
  }
}
