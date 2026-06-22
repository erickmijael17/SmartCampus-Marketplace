import { Injectable, computed, signal } from '@angular/core';
import { AuthSession } from '../models/auth.model';

const SESSION_KEY = 'smartcampus-session';

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly sessionSignal = signal<AuthSession | null>(this.loadSession());

  readonly session = this.sessionSignal.asReadonly();
  readonly isAuthenticated = computed(() => this.sessionSignal() !== null);
  readonly userId = computed(() => this.sessionSignal()?.userId ?? null);
  readonly username = computed(() => this.sessionSignal()?.username ?? '');
  readonly roles = computed(() => this.sessionSignal()?.roles ?? []);
  readonly token = computed(() => this.sessionSignal()?.accessToken ?? '');

  setSession(session: AuthSession): void {
    const normalizedSession = this.normalizeSession(session);
    this.sessionSignal.set(normalizedSession);
    localStorage.setItem(SESSION_KEY, JSON.stringify(normalizedSession));
  }

  clear(): void {
    this.sessionSignal.set(null);
    localStorage.removeItem(SESSION_KEY);
  }

  getToken(): string | null {
    return this.token() || null;
  }

  authHeaderValue(): string | null {
    const token = this.getToken();
    return token ? `Bearer ${token}` : null;
  }

  private loadSession(): AuthSession | null {
    const rawSession = localStorage.getItem(SESSION_KEY);
    if (!rawSession) {
      return null;
    }

    try {
      return this.normalizeSession(JSON.parse(rawSession) as AuthSession);
    } catch {
      localStorage.removeItem(SESSION_KEY);
      return null;
    }
  }

  private normalizeSession(session: AuthSession): AuthSession {
    return {
      ...session,
      tokenType: session.tokenType || 'Bearer',
      roles: session.roles ?? [],
      userId: session.userId ?? null
    };
  }
}
