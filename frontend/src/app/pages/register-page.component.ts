import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthApiService } from '../core/services/auth-api.service';
import { SessionService } from '../core/services/session.service';
import { describeHttpError } from '../core/utils/http-error.util';

@Component({
  selector: 'app-register-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register-page.component.html',
  styleUrl: './register-page.component.css'
})
export class RegisterPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authApi = inject(AuthApiService);
  private readonly sessionService = inject(SessionService);
  private readonly router = inject(Router);

  submitting = false;
  errorMessage = '';

  readonly form = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;
    this.errorMessage = '';

    this.authApi.register(this.form.getRawValue()).subscribe({
      next: (session) => {
        this.sessionService.setSession(session);
        void this.router.navigateByUrl('/');
      },
      error: (error) => {
        this.submitting = false;
        this.errorMessage = describeHttpError(error, 'el registro');
      }
    });
  }
}
