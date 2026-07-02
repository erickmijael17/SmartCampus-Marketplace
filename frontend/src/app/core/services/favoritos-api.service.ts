import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../config/api.config';
import { FavoritoRequest, FavoritoResponse } from '../models/favorito.model';
import { GatewayService } from './gateway.service';

@Injectable({ providedIn: 'root' })
export class FavoritosApiService {
  private readonly http = inject(HttpClient);
  private readonly gateway = inject(GatewayService);

  findAll(): Observable<FavoritoResponse[]> {
    return this.http.get<FavoritoResponse[]>(this.url(API_CONFIG.endpoints.favoritos.base));
  }

  create(request: FavoritoRequest): Observable<FavoritoResponse> {
    return this.http.post<FavoritoResponse>(this.url(API_CONFIG.endpoints.favoritos.base), request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(this.url(API_CONFIG.endpoints.favoritos.detail(id)));
  }

  private url(path: string): string {
    return path.startsWith('http://') || path.startsWith('https://') ? path : this.gateway.baseUrl() + path;
  }
}
