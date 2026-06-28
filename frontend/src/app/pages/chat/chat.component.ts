import { DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ChatMessage, ChatThread } from '../../core/models/chat.model';
import { ChatService } from '../../core/services/chat.service';
import { SessionService } from '../../core/services/session.service';
import { describeHttpError } from '../../core/utils/http-error.util';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { LoadingComponent } from '../../shared/components/loading/loading.component';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [FormsModule, EmptyStateComponent, LoadingComponent, DatePipe],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css'
})
export class ChatComponent implements OnInit {
  private readonly chatService = inject(ChatService);
  private readonly sessionService = inject(SessionService);

  threads: ChatThread[] = [];
  activeThread?: ChatThread;
  messages: ChatMessage[] = [];
  message = '';
  showList = true;
  loading = true;
  sending = false;
  errorMessage = '';

  ngOnInit(): void {
    if (!this.sessionService.isAuthenticated()) {
      this.loading = false;
      this.errorMessage = 'Debes iniciar sesion para ver tus conversaciones.';
      return;
    }

    this.chatService.getThreads().subscribe({
      next: (threads) => {
        this.threads = threads;
        if (threads.length > 0) {
          this.selectThread(threads[0]);
        }
        this.loading = false;
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = describeHttpError(error, 'la carga de conversaciones');
      }
    });
  }

  selectThread(thread: ChatThread): void {
    this.activeThread = thread;
    this.messages = thread.messages;
    this.showList = false;
    this.errorMessage = '';
  }

  send(): void {
    const text = this.message.trim();
    if (!text || !this.activeThread) {
      return;
    }

    this.sending = true;
    this.chatService.sendMessage(this.activeThread.id, text).subscribe({
      next: (msg) => {
        this.messages = [...this.messages, msg];
        this.message = '';
        this.sending = false;
      },
      error: (error) => {
        this.sending = false;
        this.errorMessage = describeHttpError(error, 'el envio del mensaje');
      }
    });
  }
}
