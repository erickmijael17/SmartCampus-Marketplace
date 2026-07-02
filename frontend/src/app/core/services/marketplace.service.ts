import { HttpClient } from '@angular/common/http';

import { Injectable, inject } from '@angular/core';

import { Observable, catchError, forkJoin, map, of, switchMap, tap, throwError } from 'rxjs';

import { API_CONFIG } from '../config/api.config';
import { environment } from '../../../environments/environment';

import { AuthMeResponse } from '../models/auth.model';
import { PersonaResponse } from '../models/persona.model';

import { CalificacionResponse } from '../models/calificacion.model';

import { FavoritoResponse } from '../models/favorito.model';

import { MediaFileResponse } from '../models/media.model';

import { PublicacionResponse } from '../models/publicacion.model';

import {

  CategoriaDto,

  CheckoutRequest,

  MarketplaceListing,

  ProductRequest,

  ProductResponse,

  PurchaseSummary

} from '../models/product.model';

import { MarketplaceUser } from '../models/user.model';

import { CalificacionApiService } from './calificacion-api.service';

import { FavoritosApiService } from './favoritos-api.service';

import { GatewayService } from './gateway.service';

import { MediaApiService } from './media-api.service';

import { PublicacionApiService } from './publicacion-api.service';

import { SessionService } from './session.service';

interface OrdenResponse {

  id: number;

  estado: string;

}

interface PagoResponse {

  id: number;

  estado: string;

}

interface MercadoPagoPreferenceResponse {

  pagoId: number;

  idOrden: number;

  estado: string;

  preferenceId: string;

  initPoint: string;

  sandboxInitPoint: string;

}

interface VendedorVentasResumenResponse {

  idVendedor: number;

  ventas: number;

  montoTotal?: number;

}

interface CatalogContext {

  publicaciones: PublicacionResponse[];

  mediaFiles: MediaFileResponse[];

}

@Injectable({ providedIn: 'root' })

export class MarketplaceService {

  private readonly http = inject(HttpClient);

  private readonly gateway = inject(GatewayService);

  private readonly sessionService = inject(SessionService);

  private readonly publicacionApi = inject(PublicacionApiService);

  private readonly mediaApi = inject(MediaApiService);

  private readonly favoritosApi = inject(FavoritosApiService);

  private readonly calificacionApi = inject(CalificacionApiService);

  getListings(): Observable<MarketplaceListing[]> {

    return this.http.get<ProductResponse[]>(this.url(API_CONFIG.endpoints.marketplace.products)).pipe(

      switchMap((products) =>

        this.loadCatalogContext().pipe(map((context) => this.joinCatalog(products, context)))

      )

    );

  }

  getListingById(id: number): Observable<MarketplaceListing> {

    return this.http

      .get<ProductResponse>(this.url(API_CONFIG.endpoints.marketplace.productDetail(id)))

      .pipe(

        switchMap((product) =>

          this.loadCatalogContext().pipe(

            map((context) => this.joinCatalog([product], context)[0] ?? this.toListing(product))

          )

        )

      );

  }

  getCategories(): Observable<CategoriaDto[]> {

    return this.http.get<CategoriaDto[]>(this.url(API_CONFIG.endpoints.marketplace.categories));

  }

  createListing(

    payload: {

      titulo: string;

      descripcion: string;

      precio: number;

      categoriaId: number;

      imageUrl?: string;

    }

  ): Observable<MarketplaceListing> {

    return this.ensurePersona().pipe(

      switchMap(() => {

        const userId = this.requireUserId();

        const publicacionRequest = {

          titulo: payload.titulo,

          descripcion: payload.descripcion,

          precio: payload.precio,

          estado: 'ACTIVO',

          idUsuario: userId,

          idCategoria: payload.categoriaId

        };

        return this.publicacionApi.create(publicacionRequest).pipe(

      switchMap((publicacion) => {

        const productRequest: ProductRequest = {

          titulo: payload.titulo,

          descripcion: payload.descripcion,

          precio: payload.precio,

          moneda: 'PEN',

          estado: 'PUBLICADO',

          idCategoria: payload.categoriaId,

          idVendedor: userId

        };

        return this.http

          .post<ProductResponse>(this.url(API_CONFIG.endpoints.marketplace.products), productRequest)

          .pipe(

            switchMap((product) => {

              if (!payload.imageUrl?.trim()) {

                return of(this.toListing(product, publicacion.id, null));

              }

              return this.mediaApi

                .create({

                  url: payload.imageUrl.trim(),

                  tipoMime: 'image/jpeg',

                  idUploader: userId,

                  idPublicacion: publicacion.id

                })

                .pipe(

                  map((media) => this.toListing(product, publicacion.id, media.url)),

                  catchError(() => of(this.toListing(product, publicacion.id, payload.imageUrl?.trim() ?? null)))

                );

            }),

            catchError(() =>

              of(

                this.toListing(

                  {

                    id: publicacion.id,

                    titulo: publicacion.titulo,

                    descripcion: publicacion.descripcion,

                    precio: publicacion.precio,

                    moneda: 'PEN',

                    estado: publicacion.estado,

                    idCategoria: publicacion.idCategoria,

                    idVendedor: publicacion.idUsuario

                  },

                  publicacion.id,

                  payload.imageUrl?.trim() ?? null

                )

              )

            )

          );

      })

    );

      })

    );

  }

  uploadListingImage(publicacionId: number, file: File): Observable<MediaFileResponse> {
    return this.ensurePersona().pipe(
      switchMap(() => this.mediaApi.upload(file, this.requireUserId(), publicacionId))
    );
  }

  checkout(listing: MarketplaceListing, quantity: number, paymentMethod: string): Observable<PurchaseSummary> {

    return this.ensurePersona().pipe(

      switchMap(() => {

    const userId = this.requireUserId();

    const orderRequest = {

      idComprador: userId,

      idProducto: listing.id,

      cantidad: quantity,

      precioUnitario: listing.price,

      estado: 'PENDIENTE'

    };

    return this.http.post<OrdenResponse>(this.url(API_CONFIG.endpoints.marketplace.orders), orderRequest).pipe(

      switchMap((order) => {

        return this.http

          .post<MercadoPagoPreferenceResponse>(this.url(API_CONFIG.endpoints.marketplace.mercadoPagoPreference), {

            idOrden: order.id,

            idComprador: userId,

            idProducto: listing.id,

            titulo: listing.title,

            descripcion: listing.description,

            cantidad: quantity,

            precioUnitario: listing.price,

            metodoPago: paymentMethod

          })
          .pipe(

            map((preference) => ({

              orderId: order.id,

              paymentId: preference.pagoId,

              status: preference.estado,

              checkoutUrl: environment.production
                ? preference.initPoint || preference.sandboxInitPoint
                : preference.sandboxInitPoint || preference.initPoint

            }))

          );

      })

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

    return this.getListings().pipe(map((items) => items.filter((i) => i.id !== product.id).slice(0, 4)));

  }

  getCurrentUser(): Observable<MarketplaceUser> {

    if (!this.sessionService.isAuthenticated()) {

      return throwError(() => new Error('Debes iniciar sesion para ver tu perfil.'));

    }

    const session = this.sessionService.session();

    if (!session) {

      return throwError(() => new Error('Sesion no disponible.'));

    }

    return forkJoin({

      me: this.http.get<AuthMeResponse>(this.url(API_CONFIG.endpoints.auth.me)).pipe(catchError(() => of(null))),

      listings: this.getUserProducts(),

      favoritos: this.getMyFavoritos().pipe(catchError(() => of([] as FavoritoResponse[]))),

      calificaciones: this.getMyCalificaciones().pipe(catchError(() => of([] as CalificacionResponse[]))),

      ventas: this.getMySalesCount().pipe(catchError(() => of(0)))

    }).pipe(

      map(({ me, listings, favoritos, calificaciones, ventas }) =>

        this.toMarketplaceUser(me, listings.length, favoritos.length, calificaciones.length, ventas)

      )

    );

  }

  getUserProducts(): Observable<MarketplaceListing[]> {

    const personaId = this.sessionService.personaId();

    return this.getListings().pipe(map((items) => items.filter((i) => personaId !== null && i.sellerId === personaId)));

  }

  getMyFavoritos(): Observable<FavoritoResponse[]> {

    if (!this.sessionService.isAuthenticated()) {

      return of([]);

    }

    const personaId = this.sessionService.personaId();

    return this.favoritosApi

      .findAll()

      .pipe(map((items) => items.filter((item) => personaId !== null && item.idUsuario === personaId)));

  }

  getFavoriteListings(): Observable<MarketplaceListing[]> {

    return forkJoin({

      favoritos: this.getMyFavoritos(),

      listings: this.getListings(),

      publicaciones: this.loadPublicaciones()

    }).pipe(

      map(({ favoritos, listings, publicaciones }) => {

        const favoritePublicationIds = new Set(favoritos.map((item) => item.idPublicacion));

        const publicationById = new Map(publicaciones.map((item) => [item.id, item]));

        const favoriteKeys = new Set(

          favoritos

            .map((favorito) => publicationById.get(favorito.idPublicacion))

            .filter((item): item is PublicacionResponse => !!item)

            .map((item) => this.publicationKey(item.idUsuario, item.titulo, item.idCategoria))

        );

        const matched = listings.filter(

          (listing) =>

            (listing.publicacionId && favoritePublicationIds.has(listing.publicacionId)) ||

            favoriteKeys.has(this.publicationKey(listing.sellerId, listing.title, listing.categoryId))

        );

        return Array.from(new Map(matched.map((item) => [item.id, item])).values());

      })

    );

  }

  getMyCalificaciones(): Observable<CalificacionResponse[]> {

    if (!this.sessionService.isAuthenticated()) {

      return of([]);

    }

    return this.calificacionApi.findAll().pipe(

      switchMap((items) =>

        this.getUserProducts().pipe(

          map((products) => {

            const publicationIds = new Set(products.map((product) => product.publicacionId).filter(Boolean));

            return items.filter((item) => publicationIds.has(item.idPublicacion));

          })

        )

      )

    );

  }

  getMySalesCount(): Observable<number> {

    const personaId = this.sessionService.personaId();

    if (!this.sessionService.isAuthenticated() || personaId === null) {

      return of(0);

    }

    return this.http

      .get<VendedorVentasResumenResponse>(this.url(API_CONFIG.endpoints.payments.sellerSummary(personaId)))

      .pipe(map((summary) => Number(summary.ventas) || 0));

  }

  getCalificacionesForListing(listing: MarketplaceListing): Observable<CalificacionResponse[]> {

    if (!listing.publicacionId) {

      return of([]);

    }

    return this.calificacionApi

      .findAll()

      .pipe(map((items) => items.filter((item) => item.idPublicacion === listing.publicacionId)));

  }

  addFavorite(listing: MarketplaceListing): Observable<FavoritoResponse> {

    return this.ensurePersona().pipe(

      switchMap(() => {

    const userId = this.requireUserId();

    if (!listing.publicacionId) {

      return throwError(

        () => new Error('Esta publicacion no tiene idPublicacion asociado. Publica nuevamente o sincroniza con publicacion-ms.')

      );

    }

    return this.favoritosApi.create({

      idUsuario: userId,

      idPublicacion: listing.publicacionId

    });

      })

    );

  }

  removeFavorite(favoritoId: number): Observable<void> {

    return this.favoritosApi.delete(favoritoId);

  }

  findFavoriteForListing(listing: MarketplaceListing): Observable<FavoritoResponse | null> {

    if (!listing.publicacionId) {

      return of(null);

    }

    return this.getMyFavoritos().pipe(

      map((items) => items.find((item) => item.idPublicacion === listing.publicacionId) ?? null)

    );

  }

  submitCalificacion(

    listing: MarketplaceListing,

    puntuacion: number,

    comentario: string

  ): Observable<CalificacionResponse> {

    return this.ensurePersona().pipe(

      switchMap(() => {

    const userId = this.requireUserId();

    if (!listing.publicacionId) {

      return throwError(

        () => new Error('Esta publicacion no tiene idPublicacion asociado para calificar.')

      );

    }

    return this.calificacionApi.create({

      idUsuario: userId,

      idPublicacion: listing.publicacionId,

      puntuacion,

      comentario: comentario.trim() || undefined

    });

      })

    );

  }

  isFavorite(listing: MarketplaceListing): Observable<boolean> {

    if (!listing.publicacionId) {

      return of(false);

    }

    return this.getMyFavoritos().pipe(

      map((items) => items.some((item) => item.idPublicacion === listing.publicacionId))

    );

  }

  publishListing(model: {

    title: string;

    description: string;

    price: number | string;

    imageUrl?: string;

  }): Observable<MarketplaceListing> {

    return this.createListing({

      titulo: model.title,

      descripcion: model.description,

      precio: Number(model.price) || 0,

      categoriaId: 1,

      imageUrl: model.imageUrl

    });

  }

  private loadCatalogContext(): Observable<CatalogContext> {
    return forkJoin({

      publicaciones: this.loadPublicaciones(),

      mediaFiles: this.mediaApi.findAll().pipe(catchError(() => of([] as MediaFileResponse[])))

    });

  }

  private loadPublicaciones(): Observable<PublicacionResponse[]> {
    return this.publicacionApi.findAll().pipe(catchError(() => of([] as PublicacionResponse[])));

  }

  private joinCatalog(products: ProductResponse[], context: CatalogContext): MarketplaceListing[] {

    const publicationByKey = new Map(

      context.publicaciones.map((item) => [this.publicationKey(item.idUsuario, item.titulo, item.idCategoria), item])

    );

    const mediaByPublication = new Map<number, string>();

    for (const media of context.mediaFiles) {

      if (media.idPublicacion) {

        mediaByPublication.set(media.idPublicacion, media.url);

      }

    }

    return products.map((product) => {

      const publication =

        publicationByKey.get(this.publicationKey(product.idVendedor, product.titulo, product.idCategoria)) ?? null;

      const mediaUrl = publication ? mediaByPublication.get(publication.id) ?? null : null;

      return this.toListing(product, publication?.id ?? null, mediaUrl);

    });

  }

  private publicationKey(userId: number, title: string, categoryId: number): string {

    return `${userId}::${title.trim().toLowerCase()}::${categoryId}`;

  }

  private toMarketplaceUser(

    me: AuthMeResponse | null,

    published: number,

    favorites: number,

    reviews: number,

    sales: number

  ): MarketplaceUser {

    const persona = me?.persona;

    const fullName = persona ? `${persona.nombres} ${persona.apellidos}`.trim() : this.sessionService.username();

    const initials = fullName

      .split(' ')

      .filter(Boolean)

      .slice(0, 2)

      .map((part) => part[0]?.toUpperCase() ?? '')

      .join('') || 'U';

    return {

      id: String(persona?.id ?? this.sessionService.personaId() ?? this.sessionService.userId() ?? '0'),

      fullName: fullName || 'Usuario',

      initials,

      email: persona?.email ?? me?.email ?? `${this.sessionService.username()}@smartcampus.test`,

      career: persona?.carrera ?? 'Sin carrera registrada',

      cycle: persona?.tipoUsuario ?? 'Estudiante',

      rating: reviews > 0 ? 4.5 : 0,

      reviews,

      published,

      sales,

      favorites

    };

  }

  /** Ensures personaId exists in session by fetching /auth/profile if needed. */
  private ensurePersona(): Observable<void> {
    if (this.sessionService.personaId() !== null) {
      return of(void 0);
    }
    return this.http.get<PersonaResponse>(this.url(API_CONFIG.endpoints.auth.profile)).pipe(
      tap((persona) => {
        const session = this.sessionService.session();
        if (session) {
          this.sessionService.setSession({ ...session, personaId: persona.id });
        }
      }),
      map(() => void 0),
      catchError(() => {
        this.sessionService.clear();
        return of(void 0);
      })
    );
  }

  private requireUserId(): number {

    if (!this.sessionService.isAuthenticated()) {

      throw new Error('Debes iniciar sesion para continuar.');

    }

    const personaId = this.sessionService.personaId();

    if (personaId !== null && personaId > 0) {

      return personaId;

    }

    throw new Error(

      'Tu sesion no incluye un ID de usuario valido. Cierra sesion, vuelve a ingresar e intenta nuevamente.'

    );

  }

  private toListing(

    product: ProductResponse,

    publicacionId: number | null = null,

    imageOverride: string | null = null

  ): MarketplaceListing {

    return {

      id: product.id,

      publicacionId,

      title: product.titulo,

      description: product.descripcion ?? 'Sin descripcion',

      price: Number(product.precio),

      currency: product.moneda || 'PEN',

      status: product.estado,

      categoryId: product.idCategoria,

      categoryLabel: product.categoria?.nombre ?? `Categoria ${product.idCategoria}`,

      sellerId: product.idVendedor,

      sellerLabel: `Vendedor #${product.idVendedor}`,

      imageUrl: this.resolveImageUrl(imageOverride),

      publishedAt: product.publicadoEn ?? null

    };

  }

  private resolveImageUrl(url: string | null): string {

    if (!url) {

      return '/assets/placeholder-listing.svg';

    }

    if (url.startsWith('http://') || url.startsWith('https://')) {

      return url;

    }

    if (url.startsWith('/')) {

      return this.url(url);

    }

    return url;

  }

  private url(path: string): string {

    return path.startsWith('http://') || path.startsWith('https://') ? path : this.gateway.baseUrl() + path;

  }

}
