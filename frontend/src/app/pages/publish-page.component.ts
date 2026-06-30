import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { switchMap, tap } from 'rxjs';
import { MarketplaceService } from '../core/services/marketplace.service';
import { SessionService } from '../core/services/session.service';
import { CategoriaDto } from '../core/models/product.model';
import { describeHttpError } from '../core/utils/http-error.util';
import { getFieldError } from '../core/utils/form-error.util';
import { LoadingComponent } from '../shared/components/loading/loading.component';

type ImageSource = 'url' | 'local';

const ACCEPTED_IMAGE_TYPES = ['image/png', 'image/jpeg', 'image/webp', 'image/gif'];
const ACCEPTED_IMAGE_ERROR = 'Usa una imagen PNG, JPG, JPEG, WEBP o GIF.';

@Component({
  selector: 'app-publish-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LoadingComponent],
  templateUrl: './publish-page.component.html',
  styleUrl: './publish-page.component.css'
})
export class PublishPageComponent implements OnInit, OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly marketplaceService = inject(MarketplaceService);
  private readonly sessionService = inject(SessionService);
  private readonly router = inject(Router);

  submitting = false;
  loadingCategories = true;
  errorMessage = '';
  imageErrorMessage = '';
  imagePreviewUrl = '';
  imageSource: ImageSource | null = null;
  selectedImageFile: File | null = null;
  categories: CategoriaDto[] = [];
  private localImageObjectUrl = '';

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

  ngOnDestroy(): void {
    this.revokeLocalImageObjectUrl();
  }

  fieldError(controlName: 'titulo' | 'descripcion' | 'precio' | 'categoriaId' | 'imageUrl'): string | null {
    const labels: Record<string, string> = {
      titulo: 'Titulo',
      descripcion: 'Descripcion',
      precio: 'Precio',
      categoriaId: 'Categoria',
      imageUrl: 'Imagen'
    };

    const control = this.form.controls[controlName];
    if (controlName === 'imageUrl' && !control.value) {
      return null;
    }

    return getFieldError(control, labels[controlName]);
  }

  onImageUrlInput(): void {
    const imageUrl = this.form.controls.imageUrl.value.trim();
    this.imageErrorMessage = '';

    if (!imageUrl) {
      if (this.imageSource === 'url') {
        this.imagePreviewUrl = '';
        this.imageSource = null;
      }
      return;
    }

    this.revokeLocalImageObjectUrl();

    if (!/^https?:\/\/.+/i.test(imageUrl)) {
      this.imagePreviewUrl = '';
      this.imageSource = null;
      this.imageErrorMessage = 'La URL debe comenzar con http:// o https://.';
      return;
    }

    this.selectedImageFile = null;
    this.imagePreviewUrl = imageUrl;
    this.imageSource = 'url';
  }

  onLocalImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.imageErrorMessage = '';

    if (!file) {
      return;
    }

    if (!ACCEPTED_IMAGE_TYPES.includes(file.type)) {
      this.clearSelectedImage();
      this.imageErrorMessage = ACCEPTED_IMAGE_ERROR;
      input.value = '';
      return;
    }

    this.revokeLocalImageObjectUrl();
    this.form.controls.imageUrl.setValue('');
    this.selectedImageFile = file;
    this.localImageObjectUrl = URL.createObjectURL(file);
    this.imagePreviewUrl = this.localImageObjectUrl;
    this.imageSource = 'local';
  }

  clearSelectedImage(): void {
    this.revokeLocalImageObjectUrl();
    this.form.controls.imageUrl.setValue('');
    this.imagePreviewUrl = '';
    this.imageSource = null;
    this.selectedImageFile = null;
    this.imageErrorMessage = '';
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
    this.onImageUrlInput();
    if (this.imageErrorMessage) {
      this.submitting = false;
      return;
    }

    this.marketplaceService
      .createListing({
        titulo: raw.titulo,
        descripcion: raw.descripcion,
        precio: raw.precio,
        categoriaId: raw.categoriaId,
        imageUrl: this.imageSource === 'url' ? raw.imageUrl.trim() : undefined
      })
      .pipe(
        switchMap((listing) => {
          if (this.imageSource !== 'local' || !this.selectedImageFile || !listing.publicacionId) {
            return [listing];
          }

          return this.marketplaceService.uploadListingImage(listing.publicacionId, this.selectedImageFile).pipe(
            tap((media) => {
              listing.imageUrl = media.url;
            }),
            switchMap(() => [listing])
          );
        })
      )
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

  private revokeLocalImageObjectUrl(): void {
    if (!this.localImageObjectUrl) {
      return;
    }

    URL.revokeObjectURL(this.localImageObjectUrl);
    this.localImageObjectUrl = '';
  }
}
