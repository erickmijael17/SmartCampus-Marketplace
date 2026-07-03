import { Injectable, computed, signal } from '@angular/core';
import { Subject } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthSession } from '../models/auth.model';

const SESSION_KEY = 'smartcampus-session';

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly sessionSignal = signal<AuthSession | null>(this.loadSession());
  private expirationTimerId: ReturnType<typeof setTimeout> | null = null;

  readonly sessionExpired$ = new Subject<void>();

  readonly session = this.sessionSignal.asReadonly();
  readonly isAuthenticated = computed(() => {
    const current = this.sessionSignal();
    return current !== null && !this.isExpired(current);
  });
  readonly userId = computed(() => this.sessionSignal()?.userId ?? null);
  readonly personaId = computed(() => this.sessionSignal()?.personaId ?? null);
  readonly username = computed(() => this.sessionSignal()?.username ?? '');
  readonly roles = computed(() => this.sessionSignal()?.roles ?? []);
  readonly token = computed(() => this.sessionSignal()?.accessToken ?? '');

  setSession(session: AuthSession): void {
    const normalizedSession = this.normalizeSession(session);
    this.sessionSignal.set(normalizedSession);
    this.persistSession(normalizedSession);
    this.scheduleExpiration(normalizedSession);
  }

  clear(): void {
    this.clearExpirationTimer();
    this.sessionSignal.set(null);
    this.removePersistedSession();
  }

  getToken(): string | null {
    const current = this.sessionSignal();
    if (!current || this.isExpired(current)) {
      return null;
    }

    return current.accessToken || null;
  }

  authHeaderValue(): string | null {
    const token = this.getToken();
    return token ? `Bearer ${token}` : null;
  }

  isSessionExpired(): boolean {
    const current = this.sessionSignal();
    return current !== null && this.isExpired(current);
  }

  private loadSession(): AuthSession | null {
    const raw = this.storage.getItem(SESSION_KEY);
    if (!raw) {
      return null;
    }

    try {
      const session = this.normalizeSession(JSON.parse(raw) as AuthSession);
      if (this.isExpired(session)) {
        this.removePersistedSession();
        return null;
      }

      this.scheduleExpiration(session);
      return session;
    } catch {
      this.removePersistedSession();
      return null;
    }
  }

  private normalizeSession(session: AuthSession): AuthSession {
    const expiresAt = session.expiresAt ?? this.computeExpiresAt(session.expiresIn);

    return {
      ...session,
      tokenType: session.tokenType || 'Bearer',
      roles: session.roles ?? [],
      userId: session.userId ?? null,
      expiresAt
    };
  }

  private scheduleExpiration(session: AuthSession): void {
    this.clearExpirationTimer();

    if (!session.expiresAt) {
      return;
    }

    const remainingMs = session.expiresAt - Date.now();
    if (remainingMs <= 0) {
      this.handleExpiration();
      return;
    }

    this.expirationTimerId = setTimeout(() => this.handleExpiration(), remainingMs);
  }

  private handleExpiration(): void {
    this.clear();
    this.sessionExpired$.next();
  }

  private clearExpirationTimer(): void {
    if (this.expirationTimerId !== null) {
      clearTimeout(this.expirationTimerId);
      this.expirationTimerId = null;
    }
  }

  private computeExpiresAt(expiresIn: number): number {
    const ttlSeconds = Number.isFinite(expiresIn) && expiresIn > 0 ? expiresIn : 0;
    return Date.now() + ttlSeconds * 1000;
  }

  private isExpired(session: AuthSession): boolean {
    if (!session.expiresAt) {
      return false;
    }

    return Date.now() >= session.expiresAt;
  }

  private get storage(): Storage {
    return environment.sessionStorageMode === 'sessionStorage' ? sessionStorage : localStorage;
  }

  private persistSession(session: AuthSession): void {
    this.storage.setItem(SESSION_KEY, JSON.stringify(session));
  }

  private removePersistedSession(): void {
    this.storage.removeItem(SESSION_KEY);
  }
}
