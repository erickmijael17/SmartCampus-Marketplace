import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../config/api.config';
import { CalificacionRequest, CalificacionResponse } from '../models/calificacion.model';
import { GatewayService } from './gateway.service';

@Injectable({ providedIn: 'root' })
export class CalificacionApiService {
  private readonly http = inject(HttpClient);
  private readonly gateway = inject(GatewayService);

  findAll(): Observable<CalificacionResponse[]> {
    return this.http.get<CalificacionResponse[]>(this.url(API_CONFIG.endpoints.calificaciones.base));
  }

  create(request: CalificacionRequest): Observable<CalificacionResponse> {
    return this.http.post<CalificacionResponse>(this.url(API_CONFIG.endpoints.calificaciones.base), request);
  }

  private url(path: string): string {
    return path.startsWith('http://') || path.startsWith('https://') ? path : this.gateway.baseUrl() + path;
  }
}
