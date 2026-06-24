import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthApiService } from '../../core/services/auth-api.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  private readonly auth = inject(AuthApiService);
  private readonly router = inject(Router);

  fullName = '';
  email = '';
  password = '';
  userType = 'Estudiante';
  career = 'Ingeniería de Sistemas';
  cycle = '3er ciclo';

  loading = false;
  errorMessage = '';

  submit(): void {
    if (!this.fullName || !this.email || !this.password) {
      this.errorMessage = 'Los campos obligatorios no pueden estar vacíos';
      return;
    }
    
    this.loading = true;
    this.errorMessage = '';
    console.log('[RegisterComponent] Intentando registro con:', this.email);

    this.auth.register({
      fullName: this.fullName,
      email: this.email,
      password: this.password,
      userType: this.userType,
      career: this.career,
      cycle: this.cycle
    }).subscribe({
      next: (res) => {
        console.log('[RegisterComponent] Registro exitoso:', res);
        this.router.navigateByUrl('/');
      },
      error: (err) => {
        console.error('[RegisterComponent] Registro fallido:', err);
        this.loading = false;
        this.errorMessage = 'No se pudo completar el registro. Verifica los datos o la conexión.';
      },
      complete: () => this.loading = false
    });
  }
}
