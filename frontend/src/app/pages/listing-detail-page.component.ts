import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ChatMessage } from '../core/models/chat.model';
import { FavoritoResponse } from '../core/models/favorito.model';
import { MarketplaceListing } from '../core/models/product.model';
import { ChatService } from '../core/services/chat.service';
import { MarketplaceService } from '../core/services/marketplace.service';
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
  private readonly sessionService = inject(SessionService);
  private readonly chatService = inject(ChatService);

  listing: MarketplaceListing | null = null;
  loading = true;
  quantity = 1;
  paymentMethod = 'TARJETA';
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

    this.loading = true;

    this.marketplaceService.checkout(this.listing, this.quantity, this.paymentMethod).subscribe({
      next: (summary) => {
        this.statusMessage = `Compra completada. Orden ${summary.orderId} y pago ${summary.paymentId} registrados.`;
        this.loading = false;
      },
      error: (error) => {
        this.statusMessage = describeHttpError(error, 'la compra');
        this.loading = false;
      }
    });
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

  private loadFavoriteState(listing: MarketplaceListing): void {
    if (!this.sessionService.isAuthenticated()) {
      return;
    }

    this.marketplaceService.findFavoriteForListing(listing).subscribe((favorito) => {
      this.favoriteRecord = favorito;
      this.isFavorite = favorito !== null;
    });
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
