import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { PublishPageComponent } from './publish-page.component';
import { MarketplaceService } from '../core/services/marketplace.service';
import { SessionService } from '../core/services/session.service';

describe('PublishPageComponent image source', () => {
  let fixture: ComponentFixture<PublishPageComponent>;
  let component: PublishPageComponent;
  let marketplaceService: jasmine.SpyObj<MarketplaceService>;
  let sessionService: { isAuthenticated: jasmine.Spy; userId: () => number };

  beforeEach(async () => {
    marketplaceService = jasmine.createSpyObj<MarketplaceService>('MarketplaceService', [
      'getCategories',
      'createListing',
      'uploadListingImage'
    ]);
    marketplaceService.getCategories.and.returnValue(of([{ id: 7, codigo: 'ELEC', nombre: 'Electronica' }]));
    marketplaceService.createListing.and.returnValue(
      of({
        id: 99,
        publicacionId: 77,
        title: 'Audifonos inalambricos',
        description: 'Audifonos en buen estado para clases y llamadas',
        price: 80,
        currency: 'PEN',
        status: 'PUBLICADO',
        categoryId: 7,
        categoryLabel: 'Electronica',
        sellerId: 12,
        sellerLabel: 'Vendedor #12',
        imageUrl: 'https://cdn.example.com/audifonos.webp',
        publishedAt: null
      })
    );
    marketplaceService.uploadListingImage.and.returnValue(
      of({
        id: 1,
        url: 'http://localhost:18080/api/v1/media/files/producto.jpg',
        tipoMime: 'image/jpeg',
        tamanoBytes: 10,
        idUploader: 12,
        idPublicacion: 77
      })
    );

    sessionService = {
      isAuthenticated: jasmine.createSpy('isAuthenticated'),
      userId: () => 12
    };
    sessionService.isAuthenticated.and.returnValue(true);

    await TestBed.configureTestingModule({
      imports: [PublishPageComponent],
      providers: [
        provideRouter([]),
        { provide: MarketplaceService, useValue: marketplaceService },
        { provide: SessionService, useValue: sessionService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PublishPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('shows a centered preview when a valid web image URL is entered', () => {
    component.form.controls.imageUrl.setValue('https://images.example.com/render?id=abc123');

    component.onImageUrlInput();

    expect(component.imagePreviewUrl).toBe('https://images.example.com/render?id=abc123');
    expect(component.imageSource).toBe('url');
    expect(component.imageErrorMessage).toBe('');
  });

  it('rejects web image URLs without http or https', () => {
    component.form.controls.imageUrl.setValue('ftp://cdn.example.com/producto.jpg');

    component.onImageUrlInput();

    expect(component.imagePreviewUrl).toBe('');
    expect(component.imageSource).toBeNull();
    expect(component.imageErrorMessage).toBe('La URL debe comenzar con http:// o https://.');
  });

  it('persists the web image URL when publishing', () => {
    fillValidForm();
    component.form.controls.imageUrl.setValue('https://cdn.example.com/audifonos.webp');
    component.onImageUrlInput();

    component.submit();

    expect(marketplaceService.createListing).toHaveBeenCalledWith({
      titulo: 'Audifonos inalambricos',
      descripcion: 'Audifonos en buen estado para clases y llamadas',
      precio: 80,
      categoriaId: 7,
      imageUrl: 'https://cdn.example.com/audifonos.webp'
    });
  });

  it('uploads a local image after creating the listing', () => {
    fillValidForm();
    const file = new File(['fake-image'], 'producto.jpg', { type: 'image/jpeg' });
    component.selectedImageFile = file;
    component.imageSource = 'local';
    component.imagePreviewUrl = 'blob:http://localhost/local-preview';
    component.form.controls.imageUrl.setValue('');

    component.submit();

    expect(marketplaceService.createListing).toHaveBeenCalled();
    expect(marketplaceService.uploadListingImage).toHaveBeenCalledWith(77, file);
  });

  function fillValidForm(): void {
    component.form.setValue({
      titulo: 'Audifonos inalambricos',
      descripcion: 'Audifonos en buen estado para clases y llamadas',
      precio: 80,
      categoriaId: 7,
      imageUrl: ''
    });
  }
});
