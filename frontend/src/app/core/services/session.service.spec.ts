import { fakeAsync, tick } from '@angular/core/testing';
import { AuthSession } from '../models/auth.model';
import { SessionService } from './session.service';

describe('SessionService', () => {
  let service: SessionService;

  const baseSession: AuthSession = {
    accessToken: 'token-abc',
    tokenType: 'Bearer',
    expiresIn: 3600,
    username: 'demo',
    roles: ['USER']
  };

  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
    service = new SessionService();
  });

  afterEach(() => {
    service.clear();
    localStorage.clear();
    sessionStorage.clear();
  });

  it('stores session and exposes authentication state', () => {
    service.setSession({ ...baseSession, expiresIn: 3600, expiresAt: Date.now() + 60_000 });

    expect(service.isAuthenticated()).toBeTrue();
    expect(service.getToken()).toBe('token-abc');
    expect(service.authHeaderValue()).toBe('Bearer token-abc');
  });

  it('clears session on manual logout', () => {
    service.setSession({ ...baseSession, expiresAt: Date.now() + 60_000 });
    service.clear();

    expect(service.isAuthenticated()).toBeFalse();
    expect(localStorage.getItem('smartcampus-session')).toBeNull();
  });

  it('expires session automatically based on expiresAt', fakeAsync(() => {
    const expiredNotifications: number[] = [];
    service.sessionExpired$.subscribe(() => expiredNotifications.push(1));

    service.setSession({ ...baseSession, expiresAt: Date.now() + 1000 });

    expect(service.isAuthenticated()).toBeTrue();

    tick(1001);

    expect(service.isAuthenticated()).toBeFalse();
    expect(expiredNotifications.length).toBe(1);
  }));

  it('returns null token when session is expired', () => {
    service.setSession({ ...baseSession, expiresAt: Date.now() - 1 });

    expect(service.getToken()).toBeNull();
    expect(service.isAuthenticated()).toBeFalse();
  });
});
