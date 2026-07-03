import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map, switchMap, throwError } from 'rxjs';
import { API_CONFIG } from '../config/api.config';
import { GatewayService } from './gateway.service';
import { SessionService } from './session.service';

export interface CrearPreferenciaPagoPayload {
  publicacionId: number | null;
  cantidad: number;
  titulo: string;
  precio: number;
  vendedorId?: number | null;
  idProducto?: number | null;
  descripcion?: string | null;
}

export interface PreferenciaPagoResponse {
  pagoId?: number;
  idOrden?: number;
  estado?: string;
  preferenceId?: string;
  checkoutUrl?: string;
  initPoint?: string;
  init_point?: string;
  sandboxInitPoint?: string;
  sandbox_init_point?: string;
  urlPago?: string;
}

export interface ConfirmacionPagoResponse {
  pagoId?: number;
  ordenId: number;
  estado?: string;
  estadoPago: string;
  estadoOrden: string;
  chatId?: number;
  conversacionId?: number;
  mensaje?: string;
  mensajeComprobanteEnviado: boolean;
}

export interface ValidarTransaccionMercadoPagoResponse {
  pagoId: number;
  idOrden: number;
  estado: string;
  mercadoPagoPaymentId: string;
  mpPaymentId?: string;
  tituloProducto?: string;
  preferenceId?: string;
  externalReference?: string;
  monto?: number;
  moneda?: string;
  mensaje?: string;
  chatMessageCreated?: boolean;
}

interface OrdenResponse {
  id: number;
  estado: string;
}

@Injectable({ providedIn: 'root' })
export class PagoApiService {
  private readonly http = inject(HttpClient);
  private readonly gateway = inject(GatewayService);
  private readonly sessionService = inject(SessionService);

  crearPreferencia(payload: CrearPreferenciaPagoPayload): Observable<PreferenciaPagoResponse> {
    const idComprador = this.sessionService.personaId();

    if (idComprador === null || idComprador <= 0) {
      return throwError(() => new Error('Sesion sin comprador valido.'));
    }

    const cantidad = Math.max(1, Number(payload.cantidad) || 1);
    const idProducto = payload.idProducto ?? payload.publicacionId;

    if (idProducto === null || idProducto === undefined) {
      return throwError(() => new Error('Producto no disponible para pago.'));
    }

    const orderRequest = {
      idComprador,
      idProducto,
      cantidad,
      precioUnitario: payload.precio,
      estado: 'PENDIENTE',
      metodoPago: 'MERCADO_PAGO',
      idVendedor: payload.vendedorId
    };

    return this.http
      .post<OrdenResponse>(this.url(API_CONFIG.endpoints.marketplace.orders), orderRequest)
      .pipe(
        switchMap((orden) => {
          const preferenceRequest = {
            ordenId: orden.id,
            idComprador,
            publicacionId: idProducto,
            titulo: payload.titulo,
            descripcion: payload.descripcion ?? '',
            cantidad,
            precio: payload.precio,
            metodoPago: 'MERCADO_PAGO',
            idVendedor: payload.vendedorId
          };

          return this.http.post<PreferenciaPagoResponse>(
            this.url(API_CONFIG.endpoints.marketplace.mercadoPagoPreference),
            preferenceRequest
          );
        }),
        map((response) => ({
          ...response,
          urlPago:
            response.checkoutUrl ||
            response.sandboxInitPoint ||
            response.sandbox_init_point ||
            response.initPoint ||
            response.init_point ||
            response.urlPago
        }))
      );
  }

  confirmarPago(params: any): Observable<ConfirmacionPagoResponse> {
    return this.http.get<ConfirmacionPagoResponse>(
      this.url(API_CONFIG.endpoints.marketplace.mercadoPagoConfirm),
      { params }
    );
  }

  validarTransaccion(pagoId: number, paymentId: string): Observable<ValidarTransaccionMercadoPagoResponse> {
    return this.http.post<ValidarTransaccionMercadoPagoResponse>(
      this.url(API_CONFIG.endpoints.marketplace.mercadoPagoValidateTransaction(pagoId)),
      { transactionId: paymentId.trim() }
    );
  }

  private url(path: string): string {
    return path.startsWith('http://') || path.startsWith('https://') ? path : this.gateway.baseUrl() + path;
  }
}
