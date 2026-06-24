import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { PublishListingRequest } from '../../core/models/listing.model';
import { MarketplaceService } from '../../core/services/marketplace.service';

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
      this.errorMessage = 'El título y el precio son obligatorios';
      return;
    }
    
    this.loading = true;
    this.errorMessage = '';
    console.log('[PublishComponent] Enviando publicación:', this.model);

    this.marketplace.publishListing(this.model).subscribe({
      next: (product) => {
        console.log('[PublishComponent] Publicación exitosa, producto id:', product?.id);
        this.router.navigate(['/listing', product.id]);
      },
      error: (err) => {
        console.error('[PublishComponent] Error publicando:', err);
        this.loading = false;
        this.errorMessage = 'No se pudo crear la publicación. Intenta nuevamente.';
      },
      complete: () => this.loading = false
    });
  }
}
