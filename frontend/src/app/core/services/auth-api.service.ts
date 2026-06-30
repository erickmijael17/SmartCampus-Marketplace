import { HttpClient } from '@angular/common/http';

import { Injectable, inject } from '@angular/core';

import { Observable, catchError, map, of, switchMap, tap, throwError } from 'rxjs';

import { API_CONFIG } from '../config/api.config';

import {

  AuthLoginRequest,

  AuthMeResponse,

  AuthRegisterRequest,

  AuthSession

} from '../models/auth.model';

import { PersonaResponse } from '../models/persona.model';

import { GatewayService } from './gateway.service';

import { PersonaApiService } from './persona-api.service';

import { SessionService } from './session.service';



@Injectable({ providedIn: 'root' })

export class AuthApiService {

  private readonly http = inject(HttpClient);

  private readonly gateway = inject(GatewayService);

  private readonly sessionService = inject(SessionService);

  private readonly personaApi = inject(PersonaApiService);



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



  /** Revalidates the stored session against /auth/me and persona-ms (Gateway only). */

  refreshSession(): Observable<AuthSession | null> {

    const current = this.sessionService.session();

    if (!current || this.sessionService.isSessionExpired()) {

      this.sessionService.clear();

      return of(null);

    }



    this.sessionService.setSession(current);

    return this.me().pipe(

      switchMap((me) => this.enrichWithPersona(this.mergeSession(current, me))),

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

      switchMap((me) => this.enrichWithPersona(this.mergeSession(normalizedLogin, me))),

      tap((session) => this.sessionService.setSession(session)),

      catchError(() => of(normalizedLogin))

    );

  }



  private enrichWithPersona(session: AuthSession): Observable<AuthSession> {

    return this.personaApi.getMyProfile().pipe(

      map((persona) => this.mergePersona(session, persona)),

      catchError(() => of(session))

    );

  }



  private mergeSession(loginSession: AuthSession, me: AuthMeResponse): AuthSession {

    const meUserId = me.userId ?? null;



    return {

      accessToken: me.accessToken ?? loginSession.accessToken,

      tokenType: loginSession.tokenType,

      expiresIn: loginSession.expiresIn,

      expiresAt: loginSession.expiresAt,

      username: me.username ?? loginSession.username,

      email: me.email ?? loginSession.email ?? null,

      roles: me.roles ?? loginSession.roles ?? [],

      userId: this.extractNumericUserId(meUserId) ?? loginSession.userId ?? null,

      personaId: loginSession.personaId ?? null,

      keycloakUserId: this.extractKeycloakUserId(meUserId) ?? loginSession.keycloakUserId ?? null

    };

  }



  private mergePersona(session: AuthSession, persona: PersonaResponse): AuthSession {

    return {

      ...session,

      userId: persona.userId,

      personaId: persona.id,

      email: persona.email,

      username: session.username || persona.email

    };

  }



  private extractNumericUserId(value: string | number | null | undefined): number | null {

    if (typeof value === 'number' && Number.isFinite(value) && value > 0) {

      return value;

    }



    if (typeof value === 'string' && /^\d+$/.test(value)) {

      const parsed = Number(value);

      return parsed > 0 ? parsed : null;

    }



    return null;

  }



  private extractKeycloakUserId(value: string | number | null | undefined): string | null {

    if (typeof value === 'string' && value.length > 0 && !/^\d+$/.test(value)) {

      return value;

    }



    return null;

  }



  private url(path: string): string {

    return this.gateway.baseUrl() + path;

  }

}


