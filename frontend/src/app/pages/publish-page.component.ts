import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MarketplaceService } from '../core/services/marketplace.service';
import { SessionService } from '../core/services/session.service';
import { CategoriaDto } from '../core/models/product.model';
import { describeHttpError } from '../core/utils/http-error.util';
import { getFieldError } from '../core/utils/form-error.util';
import { LoadingComponent } from '../shared/components/loading/loading.component';

@Component({
  selector: 'app-publish-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LoadingComponent],
  templateUrl: './publish-page.component.html',
  styleUrl: './publish-page.component.css'
})
export class PublishPageComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly marketplaceService = inject(MarketplaceService);
  private readonly sessionService = inject(SessionService);
  private readonly router = inject(Router);

  submitting = false;
  loadingCategories = true;
  errorMessage = '';
  categories: CategoriaDto[] = [];

  protected readonly sellerId = this.sessionService.userId;

  readonly form = this.fb.nonNullable.group({
    titulo: ['', [Validators.required, Validators.minLength(5)]],
    descripcion: ['', [Validators.required, Validators.minLength(20)]],
    precio: [20, [Validators.required, Validators.min(1)]],
    categoriaId: [1, [Validators.required, Validators.min(1)]],
    imageUrl: ['']
  });

  ngOnInit(): void {
    this.marketplaceService.getCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
        if (categories.length > 0) {
          this.form.controls.categoriaId.setValue(categories[0].id);
        }
        this.loadingCategories = false;
      },
      error: (error) => {
        this.loadingCategories = false;
        this.errorMessage = describeHttpError(error, 'la carga de categorias');
      }
    });
  }

  fieldError(controlName: 'titulo' | 'descripcion' | 'precio' | 'categoriaId' | 'imageUrl'): string | null {
    const labels: Record<string, string> = {
      titulo: 'Titulo',
      descripcion: 'Descripcion',
      precio: 'Precio',
      categoriaId: 'Categoria',
      imageUrl: 'URL de imagen'
    };

    const control = this.form.controls[controlName];
    if (controlName === 'imageUrl' && !control.value) {
      return null;
    }

    return getFieldError(control, labels[controlName]);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    if (!this.sessionService.isAuthenticated()) {
      void this.router.navigate(['/login'], { queryParams: { returnUrl: '/publish' } });
      return;
    }

    this.submitting = true;
    this.errorMessage = '';

    const raw = this.form.getRawValue();
    if (raw.imageUrl && !/^https?:\/\/.+/i.test(raw.imageUrl)) {
      this.errorMessage = 'La URL de imagen debe comenzar con http:// o https://';
      return;
    }

    this.marketplaceService
      .createListing({
        titulo: raw.titulo,
        descripcion: raw.descripcion,
        precio: raw.precio,
        categoriaId: raw.categoriaId,
        imageUrl: raw.imageUrl || undefined
      })
      .subscribe({
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
