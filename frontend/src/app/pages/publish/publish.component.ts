import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { PublishListingRequest } from '../../core/models/listing.model';
import { MarketplaceService } from '../../core/services/marketplace.service';
import { describeHttpError } from '../../core/utils/http-error.util';

@Component({
  selector: 'app-publish',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './publish.component.html',
  styleUrl: './publish.component.css'
})
export class PublishComponent {
  private readonly marketplace = inject(MarketplaceService);
  private readonly router = inject(Router);

  model: PublishListingRequest = {
    type: 'Producto',
    title: '',
    category: 'Libros',
    price: '',
    description: '',
    condition: 'Usado',
    location: '',
    imageUrl: ''
  };

  loading = false;
  errorMessage = '';

  submit(): void {
    if (!this.model.title || !this.model.price) {
      this.errorMessage = 'El titulo y el precio son obligatorios';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.marketplace.publishListing(this.model).subscribe({
      next: (product) => {
        void this.router.navigate(['/listing', product.id]);
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = describeHttpError(error, 'la publicacion del producto');
      },
      complete: () => {
        this.loading = false;
      }
    });
  }
}
