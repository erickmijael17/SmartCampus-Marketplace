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
    const paymentId = params['payment_id'] ?? params['collection_id'];
    const mercadoPagoStatus = this.normalizeStatus(
      params['status'] ?? params['collection_status'] ?? this.statusFromPath()
    );

    if (mercadoPagoStatus !== 'approved') {
      this.status = mercadoPagoStatus === 'failure' || mercadoPagoStatus === 'rejected' ? 'failure' : 'pending';
      this.title = this.status === 'failure' ? 'Pago no completado' : 'Pago pendiente';
      this.message = this.status === 'failure'
        ? 'La operacion fue rechazada, cancelada o no pudo completarse.'
        : 'Mercado Pago todavia esta procesando la operacion. Revisa el estado mas tarde.';
      return;
    }

    if (!paymentId) {
      this.status = 'success';
      this.title = 'Pago aprobado';
      this.message = 'Pago aprobado, pero no recibimos el identificador de Mercado Pago para confirmarlo.';
      return;
    }

    const confirmationParams: Record<string, string> = {
      payment_id: String(paymentId),
      status: mercadoPagoStatus
    };
    if (params['external_reference']) {
      confirmationParams['external_reference'] = String(params['external_reference']);
    }
    if (params['preference_id']) {
      confirmationParams['preference_id'] = String(params['preference_id']);
    }

    this.pagoApiService.confirmarPago(confirmationParams).subscribe({
      next: (response) => {
        const estado = response.estadoPago ?? response.estado;
        if (estado === 'APROBADO') {
          this.status = 'success';
          this.title = 'Pago aprobado';
          this.message = response.mensaje ?? 'Tu pago ha sido confirmado exitosamente.';
          
          const chatId = response.chatId ?? response.conversacionId;
          if (chatId) {
            this.message += ' Redirigiendo al chat con el comprobante...';
            setTimeout(() => {
              void this.router.navigate(['/chat'], { queryParams: { chatId } });
            }, 2000);
          }
        } else if (estado === 'PENDIENTE') {
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
    const normalized = (status || 'pending').toLowerCase();
    return normalized === 'success' ? 'approved' : normalized;
  }

  private statusFromPath(): string {
    const path = this.route.snapshot.routeConfig?.path ?? '';
    if (path.endsWith('/exito')) {
      return 'approved';
    }
    if (path.endsWith('/fallo') || path.endsWith('/error')) {
      return 'failure';
    }
    return 'pending';
  }
}
