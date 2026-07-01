import { ComponentFixture, TestBed } from '@angular/core/testing';
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

    pagoApiService = jasmine.createSpyObj<PagoApiService>('PagoApiService', ['crearPreferencia']);
    pagoApiService.crearPreferencia.and.returnValue(of({ initPoint: 'https://mercadopago.test/checkout' }));

    chatService = jasmine.createSpyObj<ChatService>('ChatService', ['getOrCreateConversation']);
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

  it('creates a Mercado Pago preference directly after an authenticated user chooses to buy', () => {
    spyOn(component, 'redirectToPayment');
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
    expect(component.redirectToPayment).toHaveBeenCalledOnceWith('https://mercadopago.test/checkout');
    expect(fixture.nativeElement.querySelector('.checkout-panel')).toBeNull();
  });

  it('shows a loading state while the Mercado Pago preference is being created', () => {
    pagoApiService.crearPreferencia.and.returnValue(new Observable(() => undefined));

    component.buyNow();
    fixture.detectChanges();

    const button = fixture.nativeElement.querySelector('[data-testid="buy-now-button"]') as HTMLButtonElement;
    expect(button.disabled).toBeTrue();
    expect(button.textContent?.trim()).toBe('Redirigiendo a Mercado Pago...');
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
