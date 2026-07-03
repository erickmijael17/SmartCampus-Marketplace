import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { MarketplaceService } from './marketplace.service';
import { GatewayService } from './gateway.service';
import { SessionService } from './session.service';
import { PublicacionApiService } from './publicacion-api.service';
import { MediaApiService } from './media-api.service';
import { FavoritosApiService } from './favoritos-api.service';
import { CalificacionApiService } from './calificacion-api.service';
import { MarketplaceListing } from '../models/product.model';
import { PublicacionResponse } from '../models/publicacion.model';
import { MediaFileResponse } from '../models/media.model';

describe('MarketplaceService Mercado Pago checkout', () => {
  let service: MarketplaceService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        MarketplaceService,
        { provide: GatewayService, useValue: { baseUrl: () => 'http://localhost:18080' } },
        {
          provide: SessionService,
          useValue: {
            isAuthenticated: () => true,
            personaId: () => 8,
            session: () => ({ accessToken: 'token', personaId: 8 }),
            setSession: jasmine.createSpy('setSession'),
            clear: jasmine.createSpy('clear'),
            username: () => 'mija'
          }
        },
        { provide: PublicacionApiService, useValue: {} },
        { provide: MediaApiService, useValue: {} },
        { provide: FavoritosApiService, useValue: {} },
        { provide: CalificacionApiService, useValue: {} }
      ]
    });

    service = TestBed.inject(MarketplaceService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('creates an order and then requests a Mercado Pago preference', () => {
    const listing: MarketplaceListing = {
      id: 99,
      publicacionId: 77,
      title: 'Audifonos',
      description: 'Audifonos para clases',
      price: 35,
      currency: 'PEN',
      status: 'PUBLICADO',
      categoryId: 1,
      categoryLabel: 'Electronica',
      sellerId: 3,
      sellerLabel: 'Vendedor #3',
      imageUrl: '/assets/placeholder-listing.svg'
    };

    service.checkout(listing, 2, 'MERCADO_PAGO').subscribe((summary) => {
      expect(summary.orderId).toBe(12);
      expect(summary.paymentId).toBe(44);
      expect(summary.status).toBe('PENDIENTE');
      expect(summary.checkoutUrl).toBe('https://mercadopago.test/checkout');
    });

    const orderRequest = http.expectOne('http://localhost:18080/api/v1/ordenes');
    expect(orderRequest.request.method).toBe('POST');
    expect(orderRequest.request.body).toEqual({
      idComprador: 8,
      idProducto: 99,
      cantidad: 2,
      precioUnitario: 35,
      estado: 'PENDIENTE'
    });
    orderRequest.flush({ id: 12, estado: 'PENDIENTE' });

    const preferenceRequest = http.expectOne('http://localhost:18080/api/v1/pagos/mercadopago/preference');
    expect(preferenceRequest.request.method).toBe('POST');
    expect(preferenceRequest.request.body).toEqual({
      idOrden: 12,
      idComprador: 8,
      idProducto: 99,
      titulo: 'Audifonos',
      descripcion: 'Audifonos para clases',
      cantidad: 2,
      precioUnitario: 35,
      metodoPago: 'MERCADO_PAGO'
    });
    preferenceRequest.flush({
      pagoId: 44,
      idOrden: 12,
      estado: 'PENDIENTE',
      preferenceId: 'pref_44',
      initPoint: 'https://mercadopago.test/checkout',
      sandboxInitPoint: 'https://sandbox.mercadopago.test/checkout'
    });
  });
});

describe('MarketplaceService public catalog images', () => {
  let service: MarketplaceService;
  let http: HttpTestingController;
  let publicacionApi: jasmine.SpyObj<PublicacionApiService>;
  let mediaApi: jasmine.SpyObj<MediaApiService>;

  beforeEach(() => {
    publicacionApi = jasmine.createSpyObj<PublicacionApiService>('PublicacionApiService', ['findAll']);
    mediaApi = jasmine.createSpyObj<MediaApiService>('MediaApiService', ['findAll']);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        MarketplaceService,
        { provide: GatewayService, useValue: { baseUrl: () => 'http://localhost:18080' } },
        {
          provide: SessionService,
          useValue: {
            isAuthenticated: () => false,
            personaId: () => null,
            session: () => null,
            setSession: jasmine.createSpy('setSession'),
            clear: jasmine.createSpy('clear'),
            username: () => 'invitado'
          }
        },
        { provide: PublicacionApiService, useValue: publicacionApi },
        { provide: MediaApiService, useValue: mediaApi },
        { provide: FavoritosApiService, useValue: {} },
        { provide: CalificacionApiService, useValue: {} }
      ]
    });

    service = TestBed.inject(MarketplaceService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('shows media images in the public catalog without an authenticated session', () => {
    const publicaciones: PublicacionResponse[] = [
      {
        id: 77,
        titulo: 'Polera UPeU',
        descripcion: 'Talla L en buen estado',
        precio: 20,
        estado: 'ACTIVO',
        idUsuario: 2,
        idCategoria: 5
      }
    ];
    const mediaFiles: MediaFileResponse[] = [
      {
        id: 8,
        url: 'http://localhost:18080/api/v1/media/files/polera.jpg',
        tipoMime: 'image/jpeg',
        tamanoBytes: 123,
        idUploader: 2,
        idPublicacion: 77
      }
    ];
    publicacionApi.findAll.and.returnValue(of(publicaciones));
    mediaApi.findAll.and.returnValue(of(mediaFiles));

    service.getListings().subscribe((listings) => {
      expect(listings[0].publicacionId).toBe(77);
      expect(listings[0].imageUrl).toBe('http://localhost:18080/api/v1/media/files/polera.jpg');
    });

    const productsRequest = http.expectOne('http://localhost:18080/api/v1/productos');
    expect(productsRequest.request.method).toBe('GET');
    productsRequest.flush([
      {
        id: 99,
        titulo: 'Polera UPeU',
        descripcion: 'Talla L en buen estado',
        precio: 20,
        moneda: 'PEN',
        estado: 'PUBLICADO',
        idCategoria: 5,
        idVendedor: 2
      }
    ]);
  });
});
