import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { ListingDetailPageComponent } from './listing-detail-page.component';
import { MarketplaceListing } from '../core/models/product.model';
import { MarketplaceService } from '../core/services/marketplace.service';
import { SessionService } from '../core/services/session.service';
import { ChatService } from '../core/services/chat.service';
import { PagoApiService } from '../core/services/pago-api.service';

describe('ListingDetailPageComponent Checkout Pro flow', () => {
  let fixture: ComponentFixture<ListingDetailPageComponent>;
  let component: ListingDetailPageComponent;
  let marketplaceService: jasmine.SpyObj<MarketplaceService>;
  let pagoApiService: jasmine.SpyObj<PagoApiService>;
  let chatService: jasmine.SpyObj<ChatService>;
  let sessionService: jasmine.SpyObj<SessionService>;
  let router: jasmine.SpyObj<Router>;

  const listing: MarketplaceListing = {
    id: 99,
    publicacionId: 77,
    title: 'Polera UPeU',
    description: 'Talla L en buen estado',
    price: 20,
    currency: 'PEN',
    status: 'PUBLICADO',
    categoryId: 5,
    categoryLabel: 'Ropas',
    sellerId: 2,
    sellerLabel: 'Vendedor #2',
    imageUrl: '/assets/placeholder-listing.svg'
  };

  beforeEach(async () => {
    marketplaceService = jasmine.createSpyObj<MarketplaceService>('MarketplaceService', [
      'getListingById',
      'findFavoriteForListing'
    ]);
    marketplaceService.getListingById.and.returnValue(of(listing));
    marketplaceService.findFavoriteForListing.and.returnValue(of(null));

    pagoApiService = jasmine.createSpyObj<PagoApiService>('PagoApiService', ['crearPreferencia', 'validarTransaccion']);
    pagoApiService.crearPreferencia.and.returnValue(of({ initPoint: 'https://mercadopago.test/checkout' }));
    pagoApiService.validarTransaccion.and.returnValue(of({
      pagoId: 24,
      idOrden: 83,
      estado: 'APROBADO',
      mercadoPagoPaymentId: '166617913516',
      preferenceId: 'pref_83',
      externalReference: 'ORDEN-83',
      monto: 100,
      moneda: 'PEN',
      mensaje: 'Pago validado correctamente'
    }));

    chatService = jasmine.createSpyObj<ChatService>('ChatService', ['getOrCreateConversation', 'getMessages']);
    chatService.getOrCreateConversation.and.returnValue(
      of({
        id: 1,
        idUsuario1: 8,
        idUsuario2: 2,
        name: 'Vendedor #2',
        avatar: 'V2',
        subject: 'Polera UPeU',
        lastMsg: '',
        time: '',
        unread: 0,
        messages: []
      })
    );
    chatService.getMessages.and.returnValue(of([]));

    sessionService = jasmine.createSpyObj<SessionService>('SessionService', ['isAuthenticated', 'personaId']);
    sessionService.isAuthenticated.and.returnValue(true);
    sessionService.personaId.and.returnValue(8);

    router = jasmine.createSpyObj<Router>('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [ListingDetailPageComponent],
      providers: [
        { provide: MarketplaceService, useValue: marketplaceService },
        { provide: PagoApiService, useValue: pagoApiService },
        { provide: SessionService, useValue: sessionService },
        { provide: ChatService, useValue: chatService },
        { provide: Router, useValue: router },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '99' } } } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ListingDetailPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('does not render the internal checkout when the listing loads', () => {
    expect(fixture.nativeElement.querySelector('.checkout-panel')).toBeNull();
    expect(fixture.nativeElement.textContent).toContain('Comprar ahora');
    expect(fixture.nativeElement.textContent).not.toContain('Finalizar compra');
    expect(fixture.nativeElement.textContent).not.toContain('Detalles de facturacion');
    expect(fixture.nativeElement.textContent).not.toContain('Metodo de pago');
  });

  it('creates a Mercado Pago preference and opens Checkout Pro in a new tab', () => {
    const openSpy = spyOn(window, 'open').and.returnValue({ closed: false } as Window);
    const button = fixture.nativeElement.querySelector('[data-testid="buy-now-button"]') as HTMLButtonElement;

    button.click();
    fixture.detectChanges();

    expect(pagoApiService.crearPreferencia).toHaveBeenCalledOnceWith({
      publicacionId: 77,
      cantidad: 1,
      titulo: 'Polera UPeU',
      precio: 20,
      vendedorId: 2,
      idProducto: 99,
      descripcion: 'Talla L en buen estado'
    });
    expect(openSpy).toHaveBeenCalledOnceWith('https://mercadopago.test/checkout', '_blank', 'noopener,noreferrer');
    expect(component.statusMessage).toBe('Se abrio Mercado Pago en una nueva pestaña. Completa el pago alli y vuelve a esta ventana cuando termines.');
    expect(fixture.nativeElement.querySelector('.checkout-panel')).toBeNull();
  });

  it('redirects using checkoutUrl when pago-ms returns it', () => {
    pagoApiService.crearPreferencia.and.returnValue(
      of({
        pagoId: 24,
        preferenceId: 'pref_24',
        initPoint: 'https://mercadopago.test/init',
        checkoutUrl: 'https://mercadopago.test/checkout-url'
      })
    );
    const openSpy = spyOn(window, 'open').and.returnValue({ closed: false } as Window);
    spyOn(console, 'info');

    component.buyNow();

    expect(console.info).toHaveBeenCalledWith('[Pago] Preferencia creada', {
      preferenceId: 'pref_24',
      pagoId: 24
    });
    expect(openSpy).toHaveBeenCalledOnceWith('https://mercadopago.test/checkout-url', '_blank', 'noopener,noreferrer');
    expect(console.info).toHaveBeenCalledWith('[Pago] Checkout abierto en nueva pestaña', {
      pagoId: 24,
      preferenceId: 'pref_24'
    });
  });

  it('shows a manual checkout link when the browser blocks the Mercado Pago popup', () => {
    pagoApiService.crearPreferencia.and.returnValue(
      of({
        pagoId: 24,
        preferenceId: 'pref_24',
        checkoutUrl: 'https://mercadopago.test/checkout-url'
      })
    );
    spyOn(window, 'open').and.returnValue(null);

    component.buyNow();
    fixture.detectChanges();

    const manualLink = fixture.nativeElement.querySelector('[data-testid="manual-checkout-link"]') as HTMLAnchorElement;
    expect(component.paymentRedirecting).toBeFalse();
    expect(component.statusMessage).toBe('El navegador bloqueo la ventana de Mercado Pago. Habilita ventanas emergentes o abre el enlace manualmente.');
    expect(component.manualCheckoutUrl).toBe('https://mercadopago.test/checkout-url');
    expect(manualLink.href).toBe('https://mercadopago.test/checkout-url');
  });

  it('shows manual transaction validation controls after opening Mercado Pago', () => {
    pagoApiService.crearPreferencia.and.returnValue(
      of({
        pagoId: 24,
        preferenceId: 'pref_24',
        checkoutUrl: 'https://mercadopago.test/checkout-url'
      })
    );
    spyOn(window, 'open').and.returnValue({ closed: false } as Window);

    component.buyNow();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Numero de transaccion de Mercado Pago');
    expect(fixture.nativeElement.querySelector('[data-testid="mp-transaction-input"]')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('[data-testid="validate-payment-button"]')).not.toBeNull();
  });

  it('validates the typed Mercado Pago transaction for the last pago', () => {
    pagoApiService.crearPreferencia.and.returnValue(
      of({
        pagoId: 24,
        preferenceId: 'pref_24',
        checkoutUrl: 'https://mercadopago.test/checkout-url'
      })
    );
    spyOn(window, 'open').and.returnValue({ closed: false } as Window);

    component.buyNow();
    component.numeroTransaccionMercadoPago = ' 166617913516 ';
    component.validateManualPayment();

    expect(pagoApiService.validarTransaccion).toHaveBeenCalledOnceWith(24, '166617913516');
    expect(component.statusMessage).toContain('Su pago del producto');
  });

  it('shows a successful payment summary after approved manual validation', () => {
    pagoApiService.crearPreferencia.and.returnValue(
      of({
        pagoId: 24,
        preferenceId: 'pref_24',
        checkoutUrl: 'https://mercadopago.test/checkout-url'
      })
    );
    pagoApiService.validarTransaccion.and.returnValue(of({
      pagoId: 24,
      idOrden: 83,
      estado: 'APROBADO',
      mercadoPagoPaymentId: '166617913516',
      mpPaymentId: '166617913516',
      tituloProducto: 'Polera UPeU',
      preferenceId: 'pref_83',
      externalReference: 'ORDEN-83',
      monto: 100,
      moneda: 'PEN',
      mensaje: 'Su pago del producto Polera UPeU con numero de venta #83 por el precio de S/ 100.00 ha sido validado correctamente. En seguida el vendedor se contactara con usted para coordinar la entrega.'
    }));
    spyOn(window, 'open').and.returnValue({ closed: false } as Window);

    component.buyNow();
    component.numeroTransaccionMercadoPago = '166617913516';
    component.validateManualPayment();
    fixture.detectChanges();

    const successBox = fixture.nativeElement.querySelector('[data-testid="payment-success-summary"]') as HTMLElement;
    expect(successBox).not.toBeNull();
    expect(successBox.textContent).toContain('Pago validado correctamente');
    expect(successBox.textContent).toContain('Producto: Polera UPeU');
    expect(successBox.textContent).toContain('N. de venta: #83');
    expect(successBox.textContent).toContain('Monto: S/ 100.00');
    expect(successBox.textContent).toContain('N. de transaccion: 166617913516');
    expect(fixture.nativeElement.querySelector('[data-testid="mp-transaction-input"]')).toBeNull();
    expect(fixture.nativeElement.querySelector('[data-testid="go-sale-chat-button"]')).toBeNull();
  });

  it('refreshes the existing product chat after approved payment validation', fakeAsync(() => {
    pagoApiService.crearPreferencia.and.returnValue(
      of({
        pagoId: 24,
        preferenceId: 'pref_24',
        checkoutUrl: 'https://mercadopago.test/checkout-url'
      })
    );
    pagoApiService.validarTransaccion.and.returnValue(of({
      pagoId: 24,
      idOrden: 83,
      estado: 'APROBADO',
      mercadoPagoPaymentId: '166617913516',
      tituloProducto: 'Polera UPeU',
      monto: 100,
      moneda: 'PEN',
      mensaje: 'Pago validado'
    }));
    chatService.getMessages.and.returnValue(
      of([
        {
          from: 'system',
          author: 'Sistema',
          text: 'Compra confirmada. El usuario admin compro el producto "Polera UPeU" con numero de venta #83 por S/ 100.00.',
          time: '',
          createdAt: ''
        }
      ])
    );
    spyOn(window, 'open').and.returnValue({ closed: false } as Window);

    component.buyNow();
    component.numeroTransaccionMercadoPago = '166617913516';
    component.validateManualPayment();

    expect(chatService.getMessages).toHaveBeenCalledWith(1);
    expect(component.messages[0].from).toBe('system');
    expect(router.navigate).not.toHaveBeenCalledWith(['/chat'], jasmine.anything());
    tick(1000);
    tick(2000);
    expect(chatService.getMessages).toHaveBeenCalledTimes(3);
  }));

  it('disables buying when the current user owns the listing', () => {
    sessionService.personaId.and.returnValue(2);
    fixture = TestBed.createComponent(ListingDetailPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const button = fixture.nativeElement.querySelector('[data-testid="buy-now-button"]') as HTMLButtonElement;
    expect(button.disabled).toBeTrue();
    expect(fixture.nativeElement.textContent).toContain('No puedes comprar tu propio producto.');

    component.buyNow();

    expect(pagoApiService.crearPreferencia).not.toHaveBeenCalled();
    expect(component.statusMessage).toBe('No puedes comprar tu propio producto.');
  });

  it('does not validate an empty Mercado Pago transaction', () => {
    pagoApiService.crearPreferencia.and.returnValue(
      of({
        pagoId: 24,
        preferenceId: 'pref_24',
        checkoutUrl: 'https://mercadopago.test/checkout-url'
      })
    );
    spyOn(window, 'open').and.returnValue({ closed: false } as Window);

    component.buyNow();
    component.numeroTransaccionMercadoPago = '   ';

    component.validateManualPayment();

    expect(pagoApiService.validarTransaccion).not.toHaveBeenCalled();
    expect(component.statusMessage).toBe('Ingresa el numero de transaccion de Mercado Pago.');
  });

  it('maps validation conflicts to an order mismatch message', () => {
    pagoApiService.crearPreferencia.and.returnValue(
      of({
        pagoId: 24,
        preferenceId: 'pref_24',
        checkoutUrl: 'https://mercadopago.test/checkout-url'
      })
    );
    spyOn(window, 'open').and.returnValue({ closed: false } as Window);
    component.buyNow();
    component.numeroTransaccionMercadoPago = '166617913516';
    pagoApiService.validarTransaccion.and.returnValue(
      new Observable((subscriber) => subscriber.error(new HttpErrorResponse({ status: 409 })))
    );

    component.validateManualPayment();

    expect(component.statusMessage).toBe('El numero de transaccion no corresponde a esta orden');
  });

  it('shows a clear error when preference response does not include a checkout url', () => {
    pagoApiService.crearPreferencia.and.returnValue(of({ pagoId: 24, preferenceId: 'pref_24' }));
    spyOn(console, 'error');

    component.buyNow();

    expect(component.paymentRedirecting).toBeFalse();
    expect(component.statusMessage).toBe('La preferencia fue creada, pero no se recibio la URL de checkout.');
    expect(console.error).toHaveBeenCalledWith('[Pago] Respuesta sin URL de checkout', {
      pagoId: 24,
      preferenceId: 'pref_24'
    });
  });

  it('shows a loading state while the Mercado Pago preference is being created', () => {
    pagoApiService.crearPreferencia.and.returnValue(new Observable(() => undefined));

    component.buyNow();
    fixture.detectChanges();

    const button = fixture.nativeElement.querySelector('[data-testid="buy-now-button"]') as HTMLButtonElement;
    expect(button.disabled).toBeTrue();
    expect(button.textContent?.trim()).toBe('Redirigiendo a Mercado Pago...');
  });

  it('shows a session expired message when Mercado Pago preference creation returns 401', () => {
    pagoApiService.crearPreferencia.and.returnValue(
      new Observable((subscriber) =>
        subscriber.error(new HttpErrorResponse({ status: 401, url: 'http://localhost:18080/api/v1/pagos/mercadopago/preference' }))
      )
    );
    spyOn(console, 'error');

    component.buyNow();

    expect(component.statusMessage).toBe('Tu sesion expiro. Vuelve a iniciar sesion.');
    expect(console.error).toHaveBeenCalled();
  });

  it('shows a payments unavailable message when Mercado Pago preference creation returns 503', () => {
    pagoApiService.crearPreferencia.and.returnValue(
      new Observable((subscriber) =>
        subscriber.error(
          new HttpErrorResponse({
            status: 503,
            url: 'http://localhost:18080/api/v1/pagos/mercadopago/preference',
            error: { message: 'Service unavailable' }
          })
        )
      )
    );
    spyOn(console, 'error');

    component.buyNow();

    expect(component.statusMessage).toBe('El servicio de pagos no esta disponible o rechazo la creacion de preferencia.');
    expect(console.error).toHaveBeenCalled();
  });

  it('shows a simple error when Mercado Pago preference creation fails', () => {
    pagoApiService.crearPreferencia.and.returnValue(
      new Observable((subscriber) => subscriber.error(new Error('backend down')))
    );
    spyOn(console, 'error');

    component.buyNow();

    expect(component.statusMessage).toBe('No se pudo iniciar el pago. Intentalo nuevamente.');
    expect(console.error).toHaveBeenCalled();
  });
});
