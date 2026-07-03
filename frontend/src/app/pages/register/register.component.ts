import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthApiService } from '../../core/services/auth-api.service';
import { describeHttpError } from '../../core/utils/http-error.util';
import { getFieldError } from '../../core/utils/form-error.util';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthApiService);
  private readonly router = inject(Router);

  loading = false;
  errorMessage = '';

  readonly form = this.fb.nonNullable.group({
    fullName: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    userType: ['Estudiante', Validators.required],
    career: ['Ingenieria de Sistemas', Validators.required],
    cycle: ['3er ciclo', Validators.required]
  });

  fieldError(
    controlName: 'fullName' | 'email' | 'password' | 'userType' | 'career' | 'cycle'
  ): string | null {
    const labels: Record<string, string> = {
      fullName: 'Nombre completo',
      email: 'Correo institucional',
      password: 'Contraseña',
      userType: 'Tipo de usuario',
      career: 'Carrera',
      cycle: 'Ciclo'
    };

    return getFieldError(this.form.controls[controlName], labels[controlName]);
  }

  submit(): void {
    if (this.loading) {
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.auth.register(this.form.getRawValue()).subscribe({
      next: () => {
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
