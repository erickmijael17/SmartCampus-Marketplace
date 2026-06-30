import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { MarketplaceService } from './marketplace.service';
import { GatewayService } from './gateway.service';
import { SessionService } from './session.service';
import { PublicacionApiService } from './publicacion-api.service';
import { MediaApiService } from './media-api.service';
import { FavoritosApiService } from './favoritos-api.service';
import { CalificacionApiService } from './calificacion-api.service';
import { MarketplaceListing } from '../models/product.model';

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
