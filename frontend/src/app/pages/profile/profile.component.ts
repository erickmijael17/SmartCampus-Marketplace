import { Component, OnInit, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { CalificacionResponse } from '../../core/models/calificacion.model';
import { Product } from '../../core/models/product.model';
import { MarketplaceUser } from '../../core/models/user.model';
import { MarketplaceService } from '../../core/services/marketplace.service';
import { describeHttpError } from '../../core/utils/http-error.util';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { ListingCardComponent } from '../../shared/components/listing-card/listing-card.component';
import { LoadingComponent } from '../../shared/components/loading/loading.component';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [ListingCardComponent, LoadingComponent, EmptyStateComponent, DatePipe],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
  private readonly marketplace = inject(MarketplaceService);

  user?: MarketplaceUser;
  products: Product[] = [];
  favoriteProducts: Product[] = [];
  reviews: CalificacionResponse[] = [];
  activeTab: 'publicaciones' | 'favoritos' | 'resenas' = 'publicaciones';
  loading = true;
  errorMessage = '';

  ngOnInit(): void {
    this.marketplace.getCurrentUser().subscribe({
      next: (user) => {
        this.user = user;
        this.loading = false;
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = describeHttpError(error, 'la carga del perfil');
      }
    });

    this.marketplace.getUserProducts().subscribe((products) => (this.products = products));
    this.marketplace.getFavoriteListings().subscribe((products) => (this.favoriteProducts = products));
    this.marketplace.getMyCalificaciones().subscribe((reviews) => (this.reviews = reviews));
  }
}
