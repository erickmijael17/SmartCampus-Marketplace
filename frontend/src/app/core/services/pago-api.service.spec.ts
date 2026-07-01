import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { PagoApiService } from './pago-api.service';
import { GatewayService } from './gateway.service';
import { SessionService } from './session.service';

describe('PagoApiService', () => {
  let service: PagoApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        PagoApiService,
        { provide: GatewayService, useValue: { baseUrl: () => 'http://localhost:18080' } },
        { provide: SessionService, useValue: { personaId: () => 8 } }
      ]
    });

    service = TestBed.inject(PagoApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('creates an order and then a Mercado Pago Checkout Pro preference through the Gateway', () => {
    const payload = {
      idProducto: 99,
      publicacionId: 77,
      cantidad: 1,
      titulo: 'Polera UPeU',
      precio: 20,
      vendedorId: 2,
      descripcion: 'Talla L en buen estado'
    };

    service.crearPreferencia(payload).subscribe((response) => {
      expect(response.initPoint).toBe('https://mercadopago.test/checkout');
    });

    const orderRequest = http.expectOne('http://localhost:18080/api/v1/ordenes');
    expect(orderRequest.request.method).toBe('POST');
    expect(orderRequest.request.body).toEqual({
      idComprador: 8,
      idProducto: 99,
      cantidad: 1,
      precioUnitario: 20,
      estado: 'PENDIENTE'
    });
    orderRequest.flush({ id: 12, estado: 'PENDIENTE' });

    const preferenceRequest = http.expectOne('http://localhost:18080/api/v1/pagos/mercadopago/preference');
    expect(preferenceRequest.request.method).toBe('POST');
    expect(preferenceRequest.request.body).toEqual({
      idOrden: 12,
      idComprador: 8,
      idProducto: 99,
      titulo: 'Polera UPeU',
      descripcion: 'Talla L en buen estado',
      cantidad: 1,
      precioUnitario: 20,
      metodoPago: 'MERCADO_PAGO'
    });
    preferenceRequest.flush({ initPoint: 'https://mercadopago.test/checkout' });
  });
});
