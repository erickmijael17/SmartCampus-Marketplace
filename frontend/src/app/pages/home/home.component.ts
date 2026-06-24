import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Category, Product } from '../../core/models/product.model';
import { MarketplaceService } from '../../core/services/marketplace.service';
import { CategoryChipComponent } from '../../shared/components/category-chip/category-chip.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { ListingCardComponent } from '../../shared/components/listing-card/listing-card.component';
import { SearchBarComponent } from '../../shared/components/search-bar/search-bar.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, CategoryChipComponent, EmptyStateComponent, ListingCardComponent, SearchBarComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  private readonly marketplace = inject(MarketplaceService);

  categories: Category[] = [];
  products: Product[] = [];
  selectedCategory = 'Todos';
  query = '';

  ngOnInit(): void {
    this.marketplace.getCategories().subscribe((categories) => this.categories = categories);
    this.marketplace.getProducts().subscribe((products) => this.products = products);
  }

  get filteredProducts(): Product[] {
    const query = this.query.trim().toLowerCase();
    return this.products.filter((product) => {
      const matchesCategory = this.selectedCategory === 'Todos' || product.category === this.selectedCategory;
      const matchesQuery = !query || `${product.title} ${product.description} ${product.category}`.toLowerCase().includes(query);
      return matchesCategory && matchesQuery;
    });
  }
}
