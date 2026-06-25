import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MarketplaceService } from '../core/services/marketplace.service';
import { SessionService } from '../core/services/session.service';
import { CategoriaDto } from '../core/models/product.model';
import { describeHttpError } from '../core/utils/http-error.util';

@Component({
  selector: 'app-publish-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './publish-page.component.html',
  styleUrl: './publish-page.component.css'
})
export class PublishPageComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly marketplaceService = inject(MarketplaceService);
  private readonly sessionService = inject(SessionService);
  private readonly router = inject(Router);

  submitting = false;
  errorMessage = '';
  categories: CategoriaDto[] = [];

  protected readonly sellerId = this.sessionService.userId;

  readonly form = this.fb.nonNullable.group({
    titulo: ['', [Validators.required, Validators.minLength(5)]],
    descripcion: ['', [Validators.required, Validators.minLength(20)]],
    precio: [20, [Validators.required, Validators.min(1)]],
    categoriaId: [1, [Validators.required, Validators.min(1)]]
  });

  ngOnInit(): void {
    this.marketplaceService.getCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
        if (categories.length > 0) {
          this.form.controls.categoriaId.setValue(categories[0].id);
        }
      },
      error: (error) => {
        this.errorMessage = describeHttpError(error, 'la carga de categorias');
      }
    });
  }

  get invalid() {
    return this.form.invalid && this.form.touched;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;
    this.errorMessage = '';

    this.marketplaceService.createListing(this.form.getRawValue()).subscribe({
      next: (listing) => {
        void this.router.navigate(['/listing', listing.id]);
      },
      error: (error) => {
        this.submitting = false;
        this.errorMessage = describeHttpError(error, 'la publicacion del producto');
      }
    });
  }
}
