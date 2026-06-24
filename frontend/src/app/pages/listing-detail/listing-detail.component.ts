import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Product } from '../../core/models/product.model';
import { MarketplaceService } from '../../core/services/marketplace.service';
import { ListingCardComponent } from '../../shared/components/listing-card/listing-card.component';

@Component({
  selector: 'app-listing-detail',
  standalone: true,
  imports: [RouterLink, ListingCardComponent],
  templateUrl: './listing-detail.component.html',
  styleUrl: './listing-detail.component.css'
})
export class ListingDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly marketplace = inject(MarketplaceService);

  product?: Product;
  similar: Product[] = [];

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.marketplace.getProductById(id).subscribe((product) => {
      this.product = product;
      if (product) {
        this.marketplace.getSimilarProducts(product).subscribe((items) => this.similar = items);
      }
    });
  }
}
