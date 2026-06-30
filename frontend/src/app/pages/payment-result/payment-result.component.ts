import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

@Component({
  selector: 'app-payment-result',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './payment-result.component.html',
  styleUrl: './payment-result.component.css'
})
export class PaymentResultComponent {
  private readonly route = inject(ActivatedRoute);

  readonly status = this.normalizeStatus(this.route.snapshot.queryParamMap.get('status'));
  readonly paymentId = this.route.snapshot.queryParamMap.get('payment_id');

  get title(): string {
    if (this.status === 'success' || this.status === 'approved') {
      return 'Pago aprobado';
    }
    if (this.status === 'pending' || this.status === 'in_process') {
      return 'Pago pendiente';
    }
    return 'Pago no completado';
  }

  get message(): string {
    if (this.status === 'success' || this.status === 'approved') {
      return 'Mercado Pago confirmo la operacion. El pago quedara registrado cuando pago-ms sincronice la notificacion.';
    }
    if (this.status === 'pending' || this.status === 'in_process') {
      return 'Mercado Pago todavia esta procesando la operacion. Revisa el estado mas tarde.';
    }
    return 'La operacion fue rechazada, cancelada o no pudo completarse.';
  }

  private normalizeStatus(status: string | null): string {
    return (status || 'pending').toLowerCase();
  }
}
