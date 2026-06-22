import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { API_CONFIG, gatewayUrl } from '../config/api.config';
import { AuthLoginRequest, AuthRegisterRequest, AuthSession } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly http = inject(HttpClient);

  login(request: AuthLoginRequest) {
    return this.http.post<AuthSession>(gatewayUrl(API_CONFIG.endpoints.auth.login), request);
  }

  register(request: AuthRegisterRequest) {
    return this.http.post<AuthSession>(gatewayUrl(API_CONFIG.endpoints.auth.register), request);
  }

  me() {
    return this.http.get<AuthSession>(gatewayUrl(API_CONFIG.endpoints.auth.me));
  }
}
