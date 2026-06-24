import { Component, OnInit, inject } from '@angular/core';
import { Product } from '../../core/models/product.model';
import { MarketplaceUser } from '../../core/models/user.model';
import { MarketplaceService } from '../../core/services/marketplace.service';
import { ListingCardComponent } from '../../shared/components/listing-card/listing-card.component';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [ListingCardComponent],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
  private readonly marketplace = inject(MarketplaceService);

  user?: MarketplaceUser;
  products: Product[] = [];
  activeTab: 'publicaciones' | 'favoritos' | 'resenas' = 'publicaciones';

  ngOnInit(): void {
    this.marketplace.getCurrentUser().subscribe((user) => this.user = user);
    this.marketplace.getUserProducts().subscribe((products) => this.products = products);
  }
}
