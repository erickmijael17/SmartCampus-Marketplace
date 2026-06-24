import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthApiService } from '../../core/services/auth-api.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private readonly auth = inject(AuthApiService);
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
    console.log('[LoginComponent] Intentando login con:', this.usernameOrEmail);
    
    this.auth.login({ username: this.usernameOrEmail, password: this.password }).subscribe({
      next: (res) => {
        console.log('[LoginComponent] Login exitoso:', res);
        this.router.navigateByUrl('/');
      },
      error: (err) => {
        console.error('[LoginComponent] Login fallido:', err);
        this.loading = false;
        this.errorMessage = 'Usuario o contraseña incorrectos, o error de conexión.';
      },
      complete: () => this.loading = false
    });
  }
}
