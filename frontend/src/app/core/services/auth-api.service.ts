import { HttpClient } from '@angular/common/http';

import { Injectable, inject } from '@angular/core';

import { Observable, catchError, map, of, switchMap, tap } from 'rxjs';

import { API_CONFIG } from '../config/api.config';

import {

  AuthLoginRequest,

  AuthMeResponse,

  AuthRegisterRequest,

  AuthSession

} from '../models/auth.model';

import { GatewayService } from './gateway.service';

import { SessionService } from './session.service';

@Injectable({ providedIn: 'root' })

export class AuthApiService {

  private readonly http = inject(HttpClient);

  private readonly gateway = inject(GatewayService);

  private readonly sessionService = inject(SessionService);

  login(request: AuthLoginRequest): Observable<AuthSession> {

    return this.http

      .post<AuthSession>(this.url(API_CONFIG.endpoints.auth.login), request)

      .pipe(switchMap((session) => this.establishSession(session)));

  }

  register(request: AuthRegisterRequest): Observable<AuthSession> {

    return this.http

      .post<AuthSession>(this.url(API_CONFIG.endpoints.auth.register), request)

      .pipe(switchMap((session) => this.establishSession(session)));

  }

  me(): Observable<AuthMeResponse> {

    return this.http.get<AuthMeResponse>(this.url(API_CONFIG.endpoints.auth.me));

  }

  /** Revalidates the stored session against /auth/me (Gateway only). */

  refreshSession(): Observable<AuthSession | null> {

    const current = this.sessionService.session();

    if (!current || this.sessionService.isSessionExpired()) {

      this.sessionService.clear();

      return of(null);

    }

    this.sessionService.setSession(current);

    return this.me().pipe(

      map((me) => this.mergeSession(current, me)),

      tap((session) => this.sessionService.setSession(session)),

      catchError(() => {

        this.sessionService.clear();

        return of(null);

      })

    );

  }

  private establishSession(loginSession: AuthSession): Observable<AuthSession> {

    this.sessionService.setSession(loginSession);

    const normalizedLogin = this.sessionService.session() ?? loginSession;

    return this.me().pipe(

      map((me) => this.mergeSession(normalizedLogin, me)),

      tap((session) => this.sessionService.setSession(session)),

      catchError(() => of(normalizedLogin))

    );

  }

  private mergeSession(loginSession: AuthSession, me: AuthMeResponse): AuthSession {

    return {

      accessToken: me.accessToken ?? loginSession.accessToken,

      tokenType: loginSession.tokenType,

      expiresIn: loginSession.expiresIn,

      expiresAt: loginSession.expiresAt,

      username: me.username ?? loginSession.username,

      email: me.email ?? loginSession.email ?? null,

      roles: me.roles ?? loginSession.roles ?? [],

      userId: me.userId ?? loginSession.userId ?? null,

      personaId: me.persona?.id ?? loginSession.personaId ?? null

    };

  }

  private url(path: string): string {

    return path.startsWith('http://') || path.startsWith('https://') ? path : this.gateway.baseUrl() + path;

  }

}
