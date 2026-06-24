import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map, switchMap } from 'rxjs';
import { API_CONFIG, gatewayUrl } from '../config/api.config';
import { SessionService } from './session.service';
import {
  CategoriaDto,
  CheckoutRequest,
  MarketplaceListing,
  ProductRequest,
  ProductResponse,
  PurchaseSummary
} from '../models/product.model';

interface OrdenResponse {
  id: number;
  estado: string;
}

interface PagoResponse {
  id: number;
  estado: string;
}

@Injectable({ providedIn: 'root' })
export class MarketplaceService {
  private readonly http = inject(HttpClient);
  private readonly sessionService = inject(SessionService);

  getListings(): Observable<MarketplaceListing[]> {
    return this.http
      .get<ProductResponse[]>(gatewayUrl(API_CONFIG.endpoints.marketplace.products))
      .pipe(map((products) => products.map((product) => this.toListing(product))));
  }

  getListingById(id: number): Observable<MarketplaceListing> {
    return this.http
      .get<ProductResponse>(gatewayUrl(API_CONFIG.endpoints.marketplace.productDetail(id)))
      .pipe(map((product) => this.toListing(product)));
  }

  getCategories(): Observable<CategoriaDto[]> {
    return this.http.get<CategoriaDto[]>(gatewayUrl(API_CONFIG.endpoints.marketplace.categories));
  }

  createListing(payload: {
    titulo: string;
    descripcion: string;
    precio: number;
    categoriaId: number;
  }): Observable<MarketplaceListing> {
    const userId = this.requireUserId();
    const request: ProductRequest = {
      titulo: payload.titulo,
      descripcion: payload.descripcion,
      precio: payload.precio,
      moneda: 'PEN',
      estado: 'PUBLICADO',
      idCategoria: payload.categoriaId,
      idVendedor: userId
    };

    return this.http
      .post<ProductResponse>(gatewayUrl(API_CONFIG.endpoints.marketplace.products), request)
      .pipe(map((product) => this.toListing(product)));
  }

  checkout(listing: MarketplaceListing, quantity: number, paymentMethod: string): Observable<PurchaseSummary> {
    const userId = this.requireUserId();
    const orderRequest = {
      idComprador: userId,
      idProducto: listing.id,
      cantidad: quantity,
      precioUnitario: listing.price,
      estado: 'PENDIENTE'
    };

    return this.http
      .post<OrdenResponse>(gatewayUrl(API_CONFIG.endpoints.marketplace.orders), orderRequest)
      .pipe(
        switchMap((order) => {
          const paymentRequest: CheckoutRequest = {
            idComprador: userId,
            idProducto: listing.id,
            cantidad: quantity,
            precioUnitario: listing.price,
            metodoPago: paymentMethod,
            referenciaTransaccion: `SCM-${Date.now()}`
          };

          return this.http
            .post<PagoResponse>(
              gatewayUrl(API_CONFIG.endpoints.marketplace.payments),
              {
                idComprador: paymentRequest.idComprador,
                idOrden: order.id,
                monto: paymentRequest.precioUnitario * paymentRequest.cantidad,
                metodoPago: paymentRequest.metodoPago,
                estado: 'APROBADO',
                referenciaTransaccion: paymentRequest.referenciaTransaccion
              }
            )
            .pipe(
              map((payment) => ({
                orderId: order.id,
                paymentId: payment.id,
                status: payment.estado
              }))
            );
        })
      );
  }

  getProducts(): Observable<MarketplaceListing[]> {
    return this.getListings();
  }

  getProductById(id: number): Observable<MarketplaceListing> {
    return this.getListingById(id);
  }

  getSimilarProducts(product: MarketplaceListing): Observable<MarketplaceListing[]> {
    return this.getListings().pipe(map(items => items.filter(i => i.id !== product.id).slice(0, 4)));
  }

  getCurrentUser(): Observable<any> {
    return new Observable(sub => {
      sub.next({
        id: this.sessionService.userId()?.toString() || '0',
        fullName: this.sessionService.username() || 'Usuario Local',
        initials: (this.sessionService.username() || 'U').substring(0, 2).toUpperCase(),
        email: this.sessionService.username() + '@example.com',
        career: 'Ingeniería de Sistemas',
        cycle: '3er ciclo',
        rating: 4.8,
        reviews: 12,
        published: 5,
        sales: 3,
        favorites: 8
      });
      sub.complete();
    });
  }

  getUserProducts(): Observable<MarketplaceListing[]> {
    const userId = this.sessionService.userId();
    return this.getListings().pipe(map(items => items.filter(i => i.sellerId === userId)));
  }

  publishListing(model: any): Observable<MarketplaceListing> {
    return this.createListing({
      titulo: model.title,
      descripcion: model.description,
      precio: Number(model.price) || 0,
      categoriaId: 1
    });
  }

  private requireUserId(): number {
    const userId = this.sessionService.userId();
    if (!userId) {
      throw new Error('Debes iniciar sesion para continuar.');
    }

    return userId;
  }

  private toListing(product: ProductResponse): MarketplaceListing {
    return {
      id: product.id,
      title: product.titulo,
      description: product.descripcion ?? 'Sin descripcion',
      price: Number(product.precio),
      currency: product.moneda || 'PEN',
      status: product.estado,
      categoryId: product.idCategoria,
      categoryLabel: product.categoria?.nombre ?? `Categoria ${product.idCategoria}`,
      sellerId: product.idVendedor,
      sellerLabel: `Vendedor #${product.idVendedor}`,
      imageUrl: this.resolveImage(product),
      publishedAt: product.publicadoEn ?? null
    };
  }

  private resolveImage(product: ProductResponse): string {
    const bucket = [
      'https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=900&q=80',
      'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=900&q=80',
      'https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?auto=format&fit=crop&w=900&q=80',
      'https://images.unsplash.com/photo-1556740749-887f6717d7e4?auto=format&fit=crop&w=900&q=80',
      'https://images.unsplash.com/photo-1522071820081-009f0129c71c?auto=format&fit=crop&w=900&q=80'
    ];

    const seed = `${product.titulo}-${product.idCategoria}-${product.id}`;
    const index = Math.abs(this.hashCode(seed)) % bucket.length;
    return bucket[index];
  }

  private hashCode(value: string): number {
    let hash = 0;
    for (let index = 0; index < value.length; index++) {
      hash = (hash << 5) - hash + value.charCodeAt(index);
      hash |= 0;
    }
    return hash;
  }
}
