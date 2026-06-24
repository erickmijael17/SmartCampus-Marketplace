import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ChatMessage, ChatThread } from '../../core/models/chat.model';
import { ChatService } from '../../core/services/chat.service';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css'
})
export class ChatComponent implements OnInit {
  private readonly chatService = inject(ChatService);

  threads: ChatThread[] = [];
  activeThread?: ChatThread;
  messages: ChatMessage[] = [];
  message = '';
  showList = true;

  ngOnInit(): void {
    this.chatService.getThreads().subscribe((threads) => {
      this.threads = threads;
      this.selectThread(threads[0]);
      this.showList = true;
    });
  }

  selectThread(thread: ChatThread): void {
    this.activeThread = thread;
    this.messages = [...thread.messages];
    this.showList = false;
  }

  send(): void {
    const text = this.message.trim();
    if (!text || !this.activeThread) {
      return;
    }
    this.chatService.sendMessage(this.activeThread, text).subscribe((msg: ChatMessage) => {
      this.messages = [...this.messages, msg];
      this.message = '';
    });
  }
}
