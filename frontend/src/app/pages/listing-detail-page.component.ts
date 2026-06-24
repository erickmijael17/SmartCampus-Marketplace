import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ChatService } from '../core/services/chat.service';
import { ChatMessage } from '../core/models/chat.model';
import { MarketplaceService } from '../core/services/marketplace.service';
import { SessionService } from '../core/services/session.service';
import { MarketplaceListing } from '../core/models/product.model';

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

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.marketplaceService.getListingById(id).subscribe({
      next: (listing) => {
        this.listing = listing;
        this.messages = this.chatService.getMessages(id);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.statusMessage = 'No se pudo cargar la publicacion.';
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
    console.log('[ListingDetailPageComponent] Iniciando compra...', {
      listingId: this.listing.id,
      quantity: this.quantity,
      paymentMethod: this.paymentMethod
    });

    this.marketplaceService.checkout(this.listing, this.quantity, this.paymentMethod).subscribe({
      next: (summary) => {
        console.log('[ListingDetailPageComponent] Compra exitosa:', summary);
        this.statusMessage = `Compra completada. Orden ${summary.orderId} y pago ${summary.paymentId} registrados.`;
        this.loading = false;
      },
      error: (err) => {
        console.error('[ListingDetailPageComponent] Error en compra:', err);
        this.statusMessage = 'No se pudo completar la compra. Revisa la consola para más detalles.';
        this.loading = false;
      }
    });
  }

  sendMessage(): void {
    if (!this.listing) {
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

    console.log('[ListingDetailPageComponent] Enviando mensaje...', {
      listingId: this.listing.id,
      text: text
    });

    try {
      this.chatService.sendMessage(this.listing.id, this.sessionService.username() || 'Comprador', text);
      this.chatMessage = '';
      this.messages = this.chatService.getMessages(this.listing.id);
      this.statusMessage = 'Mensaje enviado al vendedor.';
    } catch (err) {
      console.error('[ListingDetailPageComponent] Error enviando mensaje:', err);
      this.statusMessage = 'Error al enviar mensaje.';
    }
  }

  get canBuy(): boolean {
    return this.sessionService.isAuthenticated();
  }
}
