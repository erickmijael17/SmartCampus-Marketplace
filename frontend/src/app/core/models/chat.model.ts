export interface ChatMessage {
  from: 'me' | 'them';
  text: string;
  time: string;
  author?: string;
  createdAt?: string;
}

export interface ChatThread {
  id: number;
  name: string;
  avatar: string;
  subject: string;
  lastMsg: string;
  time: string;
  unread: number;
  messages: ChatMessage[];
}
