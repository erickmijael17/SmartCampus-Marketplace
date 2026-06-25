import { CommonModule, CurrencyPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { catchError, finalize, of } from 'rxjs';
import { MarketplaceService } from '../core/services/marketplace.service';
import { SessionService } from '../core/services/session.service';
import { describeHttpError } from '../core/utils/http-error.util';

@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [CommonModule, RouterLink, CurrencyPipe],
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.css'
})
export class HomePageComponent {
  private readonly marketplaceService = inject(MarketplaceService);
  private readonly sessionService = inject(SessionService);
  private readonly router = inject(Router);

  protected readonly isAuthenticated = this.sessionService.isAuthenticated;
  protected readonly username = this.sessionService.username;
  loadingListings = true;
  listingsErrorMessage = '';

  readonly listings$ = this.marketplaceService.getListings().pipe(
    catchError((error) => {
      this.listingsErrorMessage = describeHttpError(error, 'la carga de publicaciones');
      return of([]);
    }),
    finalize(() => {
      this.loadingListings = false;
    })
  );

  goToPublish(): void {
    if (this.sessionService.isAuthenticated()) {
      void this.router.navigate(['/publish']);
      return;
    }

    void this.router.navigate(['/login'], { queryParams: { returnUrl: '/publish' } });
  }

  logout(): void {
    this.sessionService.clear();
  }
}
