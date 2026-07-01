import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ChatMessage } from '../core/models/chat.model';
import { FavoritoResponse } from '../core/models/favorito.model';
import { MarketplaceListing } from '../core/models/product.model';
import { ChatService } from '../core/services/chat.service';
import { MarketplaceService } from '../core/services/marketplace.service';
import { PagoApiService } from '../core/services/pago-api.service';
import { SessionService } from '../core/services/session.service';
import { describeHttpError } from '../core/utils/http-error.util';

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

  listing: MarketplaceListing | null = null;
  loading = true;
  quantity = 1;
  paymentRedirecting = false;
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

    this.paymentRedirecting = true;
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
          const checkoutUrl =
            response.sandboxInitPoint ||
            response.sandbox_init_point ||
            response.initPoint ||
            response.init_point ||
            response.urlPago;

          if (checkoutUrl) {
            this.redirectToPayment(checkoutUrl);
            return;
          }

          this.paymentRedirecting = false;
          this.statusMessage = 'No se pudo iniciar el pago. Intentalo nuevamente.';
        },
        error: (error) => {
          console.error('No se pudo iniciar el pago con Mercado Pago.', error);
          this.statusMessage = 'No se pudo iniciar el pago. Intentalo nuevamente.';
          this.paymentRedirecting = false;
        }
      });
  }

  redirectToPayment(url: string): void {
    window.location.href = url;
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
    return this.sessionService.isAuthenticated();
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

    this.chatService.getOrCreateConversation(listing.sellerId).subscribe({
      next: (thread) => {
        this.conversationId = thread.id;
        this.messages = thread.messages;
      },
      error: (error) => {
        this.statusMessage = describeHttpError(error, 'iniciar conversacion');
      }
    });
  }
}
