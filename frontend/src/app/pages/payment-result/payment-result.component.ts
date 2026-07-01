import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PagoApiService } from '../../core/services/pago-api.service';

@Component({
  selector: 'app-payment-result',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './payment-result.component.html',
  styleUrl: './payment-result.component.css'
})
export class PaymentResultComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly pagoApiService = inject(PagoApiService);

  status = 'verifying';
  title = 'Verificando pago...';
  message = 'Por favor espera mientras confirmamos tu pago con Mercado Pago.';
  
  ngOnInit(): void {
    const params = this.route.snapshot.queryParams;
    const paymentId = params['payment_id'];

    if (!paymentId) {
      this.status = this.normalizeStatus(params['status'] ?? this.statusFromPath());
      this.title = this.status === 'success' ? 'Pago aprobado' : (this.status === 'failure' ? 'Pago no completado' : 'Pago pendiente');
      this.message = this.status === 'success' ? 'Pago completado.' : 'El pago no se completo o esta pendiente.';
      return;
    }

    this.pagoApiService.confirmarPago(params).subscribe({
      next: (response) => {
        if (response.estadoPago === 'APROBADO') {
          this.status = 'success';
          this.title = 'Pago aprobado';
          this.message = 'Tu pago ha sido confirmado exitosamente.';
          
          if (response.chatId) {
            this.message += ' Redirigiendo al chat con el comprobante...';
            setTimeout(() => {
              void this.router.navigate(['/chat'], { queryParams: { chatId: response.chatId } });
            }, 2000);
          }
        } else if (response.estadoPago === 'PENDIENTE') {
          this.status = 'pending';
          this.title = 'Pago pendiente';
          this.message = 'Mercado Pago todavia esta procesando la operacion. Revisa el estado mas tarde.';
        } else {
          this.status = 'failure';
          this.title = 'Pago no completado';
          this.message = 'La operacion fue rechazada, cancelada o no pudo completarse.';
        }
      },
      error: (err) => {
        console.error('Error verificando el pago', err);
        this.status = 'error';
        this.title = 'Error de verificacion';
        this.message = 'Ocurrio un problema al verificar el estado de tu pago. Por favor, revisa tus ordenes mas tarde.';
      }
    });
  }

  private normalizeStatus(status: string | null): string {
    return (status || 'pending').toLowerCase();
  }

  private statusFromPath(): string {
    const path = this.route.snapshot.routeConfig?.path ?? '';
    if (path.endsWith('/exito')) {
      return 'success';
    }
    if (path.endsWith('/error')) {
      return 'failure';
    }
    return 'pending';
  }
}
