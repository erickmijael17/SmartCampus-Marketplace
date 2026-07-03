import { Component, OnInit, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { catchError, finalize, forkJoin, of } from 'rxjs';
import { Category, Product } from '../../core/models/product.model';
import { MarketplaceService } from '../../core/services/marketplace.service';
import { SessionService } from '../../core/services/session.service';
import { describeHttpError } from '../../core/utils/http-error.util';
import { CategoryChipComponent } from '../../shared/components/category-chip/category-chip.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { ListingCardComponent } from '../../shared/components/listing-card/listing-card.component';
import { LoadingComponent } from '../../shared/components/loading/loading.component';
import { SearchBarComponent } from '../../shared/components/search-bar/search-bar.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    RouterLink,
    CategoryChipComponent,
    EmptyStateComponent,
    ListingCardComponent,
    LoadingComponent,
    SearchBarComponent
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  private readonly marketplace = inject(MarketplaceService);
  private readonly sessionService = inject(SessionService);
  private readonly router = inject(Router);

  protected readonly isAuthenticated = this.sessionService.isAuthenticated;

  categories: Category[] = [];
  products: Product[] = [];
  selectedCategory = 'Todos';
  query = '';
  loading = true;
  errorMessage = '';

  ngOnInit(): void {
    forkJoin({
      categories: this.marketplace.getCategories().pipe(catchError(() => of([] as Category[]))),
      products: this.marketplace.getProducts().pipe(
        catchError((error) => {
          this.errorMessage = describeHttpError(error, 'la carga de publicaciones');
          return of([] as Product[]);
        })
      )
    })
      .pipe(finalize(() => {
        this.loading = false;
      }))
      .subscribe(({ categories, products }) => {
        this.categories = categories;
        this.products = products;
      });
  }

  get filteredProducts(): Product[] {
    const query = this.query.trim().toLowerCase();

    return this.products.filter((product) => {
      const categoryName = product.categoryLabel || product.category || '';
      const matchesCategory = this.selectedCategory === 'Todos' || categoryName === this.selectedCategory;
      const haystack = `${product.title} ${product.description} ${categoryName}`.toLowerCase();
      const matchesQuery = !query || haystack.includes(query);
      return matchesCategory && matchesQuery;
    });
  }

  selectCategory(category: string): void {
    this.selectedCategory = category;
  }

  goToPublish(): void {
    if (this.sessionService.isAuthenticated()) {
      void this.router.navigate(['/publish']);
      return;
    }

    void this.router.navigate(['/login'], { queryParams: { returnUrl: '/publish' } });
  }
}
