import { Component, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { SessionService } from './core/services/session.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  private readonly sessionService = inject(SessionService);

  protected readonly title = 'SmartCampus Marketplace';

  protected readonly isAuthenticated = this.sessionService.isAuthenticated;
  protected readonly username = this.sessionService.username;

  logout(): void {
    this.sessionService.clear();
  }
}
