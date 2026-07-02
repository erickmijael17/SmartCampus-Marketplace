import { Routes } from '@angular/router';
import { PublishPageComponent } from '../pages/publish-page.component';
import { PaymentResultComponent } from '../pages/payment-result/payment-result.component';
import { authGuard } from '../guards/auth.guard';

export const publishPaymentCircuitRoutes: Routes = [
  {
    path: 'publish',
    canActivate: [authGuard],
    component: PublishPageComponent
  },
  {
    path: 'payment-result',
    component: PaymentResultComponent
  },
  {
    path: 'pago/exito',
    component: PaymentResultComponent
  },
  {
    path: 'pago/fallo',
    component: PaymentResultComponent
  },
  {
    path: 'pago/error',
    component: PaymentResultComponent
  },
  {
    path: 'pago/pendiente',
    component: PaymentResultComponent
  },
  {
    path: 'publicar',
    redirectTo: 'publish'
  }
];
