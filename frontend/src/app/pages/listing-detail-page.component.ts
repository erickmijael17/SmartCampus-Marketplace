import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ChatMessage } from '../core/models/chat.model';
import { FavoritoResponse } from '../core/models/favorito.model';
import { MarketplaceListing } from '../core/models/product.model';
import { ChatService } from '../core/services/chat.service';
import { MarketplaceService } from '../core/services/marketplace.service';
import { PagoApiService, ValidarTransaccionMercadoPagoResponse } from '../core/services/pago-api.service';
import { SessionService } from '../core/services/session.service';
import { describeHttpError } from '../core/utils/http-error.util';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-listing-detail-page',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, DatePipe, FormsModule, RouterLink],
  templateUrl: './listing-detail-page.component.html',
  styleUrl: './listing-detail-page.component.css'
})
export class ListingDetailPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly marketplaceService = inject(MarketplaceService);
  private readonly pagoApiService = inject(PagoApiService);
  private readonly sessionService = inject(SessionService);
  private readonly chatService = inject(ChatService);
  private lastPreference: { pagoId?: number; preferenceId?: string } | null = null;

  listing: MarketplaceListing | null = null;
  loading = true;
  quantity = 1;
  paymentRedirecting = false;
  validatingPayment = false;
  showManualPaymentValidation = false;
  numeroTransaccionMercadoPago = '';
  paymentValidationResult: ValidarTransaccionMercadoPagoResponse | null = null;
  manualCheckoutUrl: string | null = null;
  statusMessage = '';
  chatMessage = '';
  messages: ChatMessage[] = [];
  conversationId: number | null = null;
  isFavorite = false;
  favoriteBusy = false;
  favoriteRecord: FavoritoResponse | null = null;
  reviewScore = 5;
  reviewComment = '';
  reviewBusy = false;

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.marketplaceService.getListingById(id).subscribe({
      next: (listing) => {
        this.listing = listing;
        this.loading = false;
        this.loadFavoriteState(listing);
        this.prepareConversation(listing);
      },
      error: (error) => {
        this.loading = false;
        this.statusMessage = describeHttpError(error, 'la carga de la publicacion');
      }
    });
  }

  buyNow(): void {
    if (!this.listing) {
      this.statusMessage = 'La publicacion no existe.';
      return;
    }

    if (!this.sessionService.isAuthenticated()) {
      void this.router.navigate(['/login'], {
        queryParams: { returnUrl: `/listing/${this.listing.id}` }
      });
      return;
    }

    if (this.isOwnListing) {
      this.statusMessage = 'No puedes comprar tu propio producto.';
      return;
    }

    this.paymentRedirecting = true;
    this.validatingPayment = false;
    this.showManualPaymentValidation = false;
    this.numeroTransaccionMercadoPago = '';
    this.paymentValidationResult = null;
    this.manualCheckoutUrl = null;
    this.statusMessage = '';

    console.log('Listing completo:', this.listing);

    this.pagoApiService
      .crearPreferencia({
        publicacionId: this.listing.publicacionId ?? null,
        cantidad: this.quantity,
        titulo: this.listing.title,
        precio: this.listing.price,
        vendedorId: this.listing.sellerId,
        idProducto: this.listing.id,
        descripcion: this.listing.description
      })
      .subscribe({
        next: (response) => {
          this.lastPreference = {
            pagoId: response.pagoId,
            preferenceId: response.preferenceId
          };
          this.showManualPaymentValidation = Boolean(response.pagoId);
          console.info('[Pago] Preferencia creada', {
            preferenceId: response.preferenceId,
            pagoId: response.pagoId
          });
          const checkoutUrl = this.resolveCheckoutUrl(response);

          if (checkoutUrl) {
            this.redirectToPayment(checkoutUrl);
            return;
          }

          this.paymentRedirecting = false;
          this.manualCheckoutUrl = null;
          this.showManualPaymentValidation = false;
          this.statusMessage = 'La preferencia fue creada, pero no se recibio la URL de checkout.';
          console.error('[Pago] Respuesta sin URL de checkout', response);
        },
        error: (error) => {
          console.error('[Pago] Error creando preferencia', this.paymentErrorLog(error));
          this.statusMessage = this.describePaymentPreferenceError(error);
          this.paymentRedirecting = false;
          this.showManualPaymentValidation = false;
        }
      });
  }

  redirectToPayment(url: string): void {
    if (environment.mercadoPagoOpenMode === 'same-tab') {
      window.location.href = url;
      return;
    }

    this.manualCheckoutUrl = url;
    const paymentWindow = window.open(url, '_blank', 'noopener,noreferrer');

    if (!paymentWindow) {
      this.paymentRedirecting = false;
      this.statusMessage = 'El navegador bloqueo la ventana de Mercado Pago. Habilita ventanas emergentes o abre el enlace manualmente.';
      return;
    }

    this.paymentRedirecting = false;
    this.statusMessage = 'Se abrio Mercado Pago en una nueva pestaña. Completa el pago alli y vuelve a esta ventana cuando termines.';
    console.info('[Pago] Checkout abierto en nueva pestaña', {
      pagoId: this.lastPreference?.pagoId,
      preferenceId: this.lastPreference?.preferenceId
    });
  }

  validateManualPayment(): void {
    const paymentId = this.numeroTransaccionMercadoPago.trim();
    if (!paymentId) {
      this.statusMessage = 'Ingresa el numero de transaccion de Mercado Pago.';
      return;
    }
    if (/\s/.test(paymentId)) {
      this.statusMessage = 'El numero de transaccion no debe contener espacios.';
      return;
    }
    const pagoId = this.lastPreference?.pagoId;
    if (!pagoId) {
      this.statusMessage = 'Primero crea la preferencia de pago.';
      return;
    }

    this.validatingPayment = true;
    this.statusMessage = 'Validando pago con Mercado Pago...';
    this.pagoApiService.validarTransaccion(pagoId, paymentId).subscribe({
      next: (response) => {
        this.validatingPayment = false;
        if ((response.estado ?? '').toUpperCase() === 'APROBADO') {
          this.paymentValidationResult = response;
          this.showManualPaymentValidation = false;
          this.statusMessage = this.approvedPaymentMessage(response);
          this.refreshCurrentConversationWithRetries();
          return;
        }
        this.paymentValidationResult = null;
        this.statusMessage = this.describeManualPaymentResponse(response.estado);
      },
      error: (error) => {
        this.validatingPayment = false;
        this.paymentValidationResult = null;
        this.statusMessage = this.describeManualPaymentError(error);
      }
    });
  }

  get paymentSuccessTransactionId(): string {
    return this.paymentValidationResult?.mpPaymentId ?? this.paymentValidationResult?.mercadoPagoPaymentId ?? this.numeroTransaccionMercadoPago.trim();
  }

  get paymentSuccessMonto(): string {
    const monto = Number(this.paymentValidationResult?.monto ?? 0);
    return monto.toFixed(2);
  }

  private resolveCheckoutUrl(response: {
    checkoutUrl?: string;
    initPoint?: string;
    init_point?: string;
    sandboxInitPoint?: string;
    sandbox_init_point?: string;
    urlPago?: string;
  }): string | null {
    return (
      response.checkoutUrl ??
      response.initPoint ??
      response.init_point ??
      response.sandboxInitPoint ??
      response.sandbox_init_point ??
      response.urlPago ??
      null
    );
  }

  private describePaymentPreferenceError(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 401) {
        return 'Tu sesion expiro. Vuelve a iniciar sesion.';
      }
      if (error.status === 503) {
        return 'El servicio de pagos no esta disponible o rechazo la creacion de preferencia.';
      }
      if (error.status === 409 || error.status === 400) {
        const message = typeof error.error?.message === 'string' ? error.error.message : '';
        if (message.includes('propio producto')) {
          return 'No puedes comprar tu propio producto.';
        }
      }
    }
    return 'No se pudo iniciar el pago. Intentalo nuevamente.';
  }

  private describeManualPaymentResponse(estado?: string): string {
    const normalized = (estado ?? '').toUpperCase();
    if (normalized === 'APROBADO') {
      return 'Pago aprobado correctamente';
    }
    if (normalized === 'PENDIENTE') {
      return 'El pago existe, pero todavia esta pendiente';
    }
    if (normalized === 'RECHAZADO') {
      return 'El pago fue rechazado';
    }
    if (normalized === 'CANCELADO') {
      return 'El pago fue cancelado';
    }
    return 'Pago validado. Revisa el estado de la orden.';
  }

  private approvedPaymentMessage(response: ValidarTransaccionMercadoPagoResponse): string {
    if (response.mensaje?.trim()) {
      return response.mensaje.trim();
    }
    const titulo = response.tituloProducto || this.listing?.title || 'producto';
    const monto = Number(response.monto ?? this.orderTotal).toFixed(2);
    return `Su pago del producto ${titulo} con numero de venta #${response.idOrden} por el precio de S/ ${monto} ha sido validado correctamente. En seguida el vendedor se contactara con usted para coordinar la entrega.`;
  }

  private describeManualPaymentError(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 409) {
        return 'El numero de transaccion no corresponde a esta orden';
      }
      if (error.status === 404) {
        return 'No se encontro esa transaccion';
      }
      if (error.status === 502 || error.status === 503) {
        return 'No se pudo consultar Mercado Pago. Intenta nuevamente.';
      }
      if (error.status === 400) {
        return 'El numero de transaccion no es valido para este pago.';
      }
    }
    return 'No se pudo validar el pago. Intenta nuevamente.';
  }

  private paymentErrorLog(error: unknown): unknown {
    if (error instanceof HttpErrorResponse) {
      return {
        status: error.status,
        url: error.url,
        error: error.error
      };
    }
    return error;
  }

  toggleFavorite(): void {
    if (!this.listing) {
      return;
    }

    if (!this.sessionService.isAuthenticated()) {
      void this.router.navigate(['/login'], {
        queryParams: { returnUrl: `/listing/${this.listing.id}` }
      });
      return;
    }

    if (!this.listing.publicacionId) {
      this.statusMessage = 'Esta publicacion aun no tiene idPublicacion asociado.';
      return;
    }

    this.favoriteBusy = true;

    if (this.isFavorite && this.favoriteRecord) {
      this.marketplaceService.removeFavorite(this.favoriteRecord.id).subscribe({
        next: () => {
          this.isFavorite = false;
          this.favoriteRecord = null;
          this.favoriteBusy = false;
          this.statusMessage = 'Publicacion eliminada de favoritos.';
        },
        error: (error) => {
          this.favoriteBusy = false;
          this.statusMessage = describeHttpError(error, 'eliminar favorito');
        }
      });
      return;
    }

    this.marketplaceService.addFavorite(this.listing).subscribe({
      next: (favorito) => {
        this.isFavorite = true;
        this.favoriteRecord = favorito;
        this.favoriteBusy = false;
        this.statusMessage = 'Publicacion agregada a favoritos.';
      },
      error: (error) => {
        this.favoriteBusy = false;
        this.statusMessage = describeHttpError(error, 'guardar favorito');
      }
    });
  }

  submitReview(): void {
    if (!this.listing) {
      return;
    }

    if (!this.sessionService.isAuthenticated()) {
      void this.router.navigate(['/login'], {
        queryParams: { returnUrl: `/listing/${this.listing.id}` }
      });
      return;
    }

    this.reviewBusy = true;
    this.marketplaceService.submitCalificacion(this.listing, this.reviewScore, this.reviewComment).subscribe({
      next: () => {
        this.reviewBusy = false;
        this.reviewComment = '';
        this.statusMessage = 'Calificacion registrada correctamente.';
      },
      error: (error) => {
        this.reviewBusy = false;
        this.statusMessage = describeHttpError(error, 'registrar calificacion');
      }
    });
  }

  sendMessage(): void {
    if (!this.listing || this.conversationId === null) {
      return;
    }

    if (!this.sessionService.isAuthenticated()) {
      void this.router.navigate(['/login'], {
        queryParams: { returnUrl: `/listing/${this.listing.id}` }
      });
      return;
    }

    const text = this.chatMessage.trim();
    if (!text) {
      return;
    }

    this.chatService.sendMessage(this.conversationId, text).subscribe({
      next: (message) => {
        this.chatMessage = '';
        this.messages = [...this.messages, message];
        this.statusMessage = 'Mensaje enviado al vendedor.';
      },
      error: (error) => {
        this.statusMessage = describeHttpError(error, 'el envio del mensaje');
      }
    });
  }

  get canBuy(): boolean {
    return this.sessionService.isAuthenticated() && !this.isOwnListing;
  }

  get isOwnListing(): boolean {
    const personaId = this.sessionService.personaId();
    return personaId !== null && this.listing?.sellerId === personaId;
  }

  get buyButtonLabel(): string {
    if (this.paymentRedirecting) {
      return 'Redirigiendo a Mercado Pago...';
    }
    if (!this.sessionService.isAuthenticated()) {
      return 'Inicia sesion para comprar';
    }
    if (this.isOwnListing) {
      return 'No puedes comprar tu propio producto';
    }
    return 'Comprar ahora';
  }

  get favoriteLabel(): string {
    return this.isFavorite ? 'Quitar de favoritos' : 'Agregar a favoritos';
  }

  get orderSubtotal(): number {
    return (this.listing?.price ?? 0) * Math.max(1, Number(this.quantity) || 1);
  }

  get orderTotal(): number {
    return this.orderSubtotal;
  }

  private loadFavoriteState(listing: MarketplaceListing): void {
    if (!this.sessionService.isAuthenticated()) {
      return;
    }

    this.marketplaceService.findFavoriteForListing(listing).subscribe((favorito) => {
      this.favoriteRecord = favorito;
      this.isFavorite = favorito !== null;
    });
  }

  onImgError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.src = '/assets/placeholder-listing.svg';
  }

  private prepareConversation(listing: MarketplaceListing): void {
    if (!this.sessionService.isAuthenticated()) {
      return;
    }

    const currentPersonaId = this.sessionService.personaId();
    if (currentPersonaId === null || currentPersonaId === listing.sellerId) {
      return;
    }

    this.chatService.getOrCreateConversation(listing.sellerId, listing.publicacionId).subscribe({
      next: (thread) => {
        this.conversationId = thread.id;
        this.messages = thread.messages;
      },
      error: (error) => {
        this.statusMessage = describeHttpError(error, 'iniciar conversacion');
      }
    });
  }

  private refreshCurrentConversationWithRetries(): void {
    this.refreshCurrentConversation();
    window.setTimeout(() => this.refreshCurrentConversation(), 1000);
    window.setTimeout(() => this.refreshCurrentConversation(), 3000);
  }

  private refreshCurrentConversation(): void {
    if (this.conversationId === null) {
      return;
    }

    this.chatService.getMessages(this.conversationId).subscribe({
      next: (messages) => {
        this.messages = messages;
      },
      error: () => {
        // El evento Kafka puede tardar unos segundos; el chat se refrescara al volver a consultar.
      }
    });
  }
}
