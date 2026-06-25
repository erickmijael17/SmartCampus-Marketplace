import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthApiService } from '../../core/services/auth-api.service';
import { SessionService } from '../../core/services/session.service';
import { describeHttpError } from '../../core/utils/http-error.util';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  private readonly auth = inject(AuthApiService);
  private readonly sessionService = inject(SessionService);
  private readonly router = inject(Router);

  fullName = '';
  email = '';
  password = '';
  userType = 'Estudiante';
  career = 'Ingenieria de Sistemas';
  cycle = '3er ciclo';
  loading = false;
  errorMessage = '';

  submit(): void {
    if (!this.fullName || !this.email || !this.password) {
      this.errorMessage = 'Los campos obligatorios no pueden estar vacios';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.auth.register({
      fullName: this.fullName,
      email: this.email,
      password: this.password,
      userType: this.userType,
      career: this.career,
      cycle: this.cycle
    }).subscribe({
      next: (session) => {
        this.sessionService.setSession(session);
        void this.router.navigateByUrl('/');
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = describeHttpError(error, 'el registro');
      },
      complete: () => {
        this.loading = false;
      }
    });
  }
}
