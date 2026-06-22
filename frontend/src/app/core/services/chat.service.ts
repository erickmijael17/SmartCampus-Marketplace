import { Injectable } from '@angular/core';

export interface ChatMessage {
  author: string;
  text: string;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class ChatService {
  private readonly storagePrefix = 'smartcampus-chat-';

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

  sendMessage(productId: number, author: string, text: string): ChatMessage {
    const message: ChatMessage = {
      author,
      text,
      createdAt: new Date().toISOString()
    };

    const messages = this.getMessages(productId);
    messages.push(message);
    localStorage.setItem(this.storagePrefix + productId, JSON.stringify(messages));
    return message;
  }
}
