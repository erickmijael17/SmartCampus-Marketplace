import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { API_CONFIG } from '../config/api.config';
import { AuthLoginRequest, AuthRegisterRequest, AuthSession } from '../models/auth.model';
import { GatewayService } from './gateway.service';

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly http = inject(HttpClient);
  private readonly gateway = inject(GatewayService);

  login(request: AuthLoginRequest) {
    return this.http.post<AuthSession>(this.url(API_CONFIG.endpoints.auth.login), request);
  }

  register(request: AuthRegisterRequest) {
    return this.http.post<AuthSession>(this.url(API_CONFIG.endpoints.auth.register), request);
  }

  me() {
    return this.http.get<AuthSession>(this.url(API_CONFIG.endpoints.auth.me));
  }

  private url(path: string): string {
    return this.gateway.baseUrl() + path;
  }
}

