import { DatePipe } from '@angular/common';
import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
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
export class ChatComponent implements OnInit, OnDestroy {
  private readonly chatService = inject(ChatService);
  private readonly sessionService = inject(SessionService);
  private readonly route = inject(ActivatedRoute);

  threads: ChatThread[] = [];
  activeThread?: ChatThread;
  messages: ChatMessage[] = [];
  message = '';
  showList = true;
  loading = true;
  sending = false;
  errorMessage = '';
  private pollInterval: any;

  ngOnInit(): void {
    if (!this.sessionService.isAuthenticated()) {
      this.loading = false;
      this.errorMessage = 'Debes iniciar sesion para ver tus conversaciones.';
      return;
    }

    this.loadThreads();
    this.pollInterval = setInterval(() => {
      this.loadThreads(true);
    }, 5000);
  }

  ngOnDestroy(): void {
    if (this.pollInterval) {
      clearInterval(this.pollInterval);
    }
  }

  private loadThreads(silent = false): void {
    if (!silent) this.loading = true;
    
    this.chatService.getThreads().subscribe({
      next: (threads) => {
        this.threads = threads;
        if (!this.activeThread && threads.length > 0) {
          this.selectThread(this.threadFromQueryParam(threads) ?? threads[0]);
        } else if (this.activeThread) {
          const updated = threads.find(t => t.id === this.activeThread!.id);
          if (updated) {
            this.activeThread = updated;
            this.messages = updated.messages;
          }
        }
        if (!silent) this.loading = false;
      },
      error: (error) => {
        if (!silent) {
          this.loading = false;
          this.errorMessage = describeHttpError(error, 'la carga de conversaciones');
        }
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

  private threadFromQueryParam(threads: ChatThread[]): ChatThread | undefined {
    const chatId = Number(this.route.snapshot.queryParamMap.get('chatId'));
    if (!Number.isFinite(chatId) || chatId <= 0) {
      return undefined;
    }
    return threads.find((thread) => thread.id === chatId);
  }
}
