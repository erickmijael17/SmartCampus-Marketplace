import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthApiService } from '../../core/services/auth-api.service';
import { SessionService } from '../../core/services/session.service';
import { describeHttpError } from '../../core/utils/http-error.util';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private readonly auth = inject(AuthApiService);
  private readonly sessionService = inject(SessionService);
  private readonly router = inject(Router);

  usernameOrEmail = '';
  password = '';
  loading = false;
  errorMessage = '';

  submit(): void {
    if (!this.usernameOrEmail || !this.password) {
      this.errorMessage = 'Todos los campos son obligatorios';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.auth.login({ username: this.usernameOrEmail, password: this.password }).subscribe({
      next: (session) => {
        this.sessionService.setSession(session);
        void this.router.navigateByUrl('/');
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = describeHttpError(error, 'el inicio de sesion');
      },
      complete: () => {
        this.loading = false;
      }
    });
  }
}
