import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../config/api.config';
import { PublicacionRequest, PublicacionResponse } from '../models/publicacion.model';
import { GatewayService } from './gateway.service';

@Injectable({ providedIn: 'root' })
export class PublicacionApiService {
  private readonly http = inject(HttpClient);
  private readonly gateway = inject(GatewayService);

  findAll(): Observable<PublicacionResponse[]> {
    return this.http.get<PublicacionResponse[]>(this.url(API_CONFIG.endpoints.publicaciones.base));
  }

  findById(id: number): Observable<PublicacionResponse> {
    return this.http.get<PublicacionResponse>(this.url(API_CONFIG.endpoints.publicaciones.detail(id)));
  }

  create(request: PublicacionRequest): Observable<PublicacionResponse> {
    return this.http.post<PublicacionResponse>(this.url(API_CONFIG.endpoints.publicaciones.base), request);
  }

  private url(path: string): string {
    return this.gateway.baseUrl() + path;
  }
}
