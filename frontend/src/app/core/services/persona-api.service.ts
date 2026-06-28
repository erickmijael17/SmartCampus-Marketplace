import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../config/api.config';
import { PersonaRequest, PersonaResponse } from '../models/persona.model';
import { GatewayService } from './gateway.service';

@Injectable({ providedIn: 'root' })
export class PersonaApiService {
  private readonly http = inject(HttpClient);
  private readonly gateway = inject(GatewayService);

  getMyProfile(): Observable<PersonaResponse> {
    return this.http.get<PersonaResponse>(this.url(API_CONFIG.endpoints.personas.me));
  }

  getById(id: number): Observable<PersonaResponse> {
    return this.http.get<PersonaResponse>(this.url(API_CONFIG.endpoints.personas.detail(id)));
  }

  create(request: PersonaRequest): Observable<PersonaResponse> {
    return this.http.post<PersonaResponse>(this.url(API_CONFIG.endpoints.personas.base), request);
  }

  update(id: number, request: PersonaRequest): Observable<PersonaResponse> {
    return this.http.put<PersonaResponse>(this.url(API_CONFIG.endpoints.personas.detail(id)), request);
  }

  private url(path: string): string {
    return this.gateway.baseUrl() + path;
  }
}
