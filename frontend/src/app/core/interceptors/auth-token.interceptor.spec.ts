import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { authTokenInterceptor } from './auth-token.interceptor';
import { GatewayService } from '../services/gateway.service';
import { SessionService } from '../services/session.service';

describe('authTokenInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let sessionService: jasmine.SpyObj<SessionService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    sessionService = jasmine.createSpyObj('SessionService', ['authHeaderValue', 'clear']);
    router = jasmine.createSpyObj('Router', ['navigate']);
    Object.defineProperty(router, 'url', { value: '/publish' });

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authTokenInterceptor])),
        provideHttpClientTesting(),
        { provide: SessionService, useValue: sessionService },
        { provide: Router, useValue: router },
        {
          provide: GatewayService,
          useValue: { gatewayAvailable: () => true, baseUrl: () => 'http://localhost:18080' }
        }
      ]
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('attaches bearer token to protected requests', () => {
    sessionService.authHeaderValue.and.returnValue('Bearer token-123');

    http.get('http://localhost:18080/api/v1/personas/me').subscribe();

    const req = httpMock.expectOne('http://localhost:18080/api/v1/personas/me');
    expect(req.request.headers.get('Authorization')).toBe('Bearer token-123');
    req.flush({});
  });

  it('does not attach bearer token to login requests', () => {
    sessionService.authHeaderValue.and.returnValue('Bearer expired-token');

    http.post('http://localhost:18080/auth/login', {}).subscribe();

    const req = httpMock.expectOne('http://localhost:18080/auth/login');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('does not attach bearer token to register requests', () => {
    sessionService.authHeaderValue.and.returnValue('Bearer expired-token');

    http.post('http://localhost:18080/auth/register', {}).subscribe();

    const req = httpMock.expectOne('http://localhost:18080/auth/register');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('does not attach bearer token to public category reads', () => {
    sessionService.authHeaderValue.and.returnValue('Bearer token-123');

    http.get('http://localhost:18080/api/v1/categorias').subscribe();

    const req = httpMock.expectOne('http://localhost:18080/api/v1/categorias');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush([]);
  });

  it('logs structured diagnostics when Angular cannot process a 200 response', () => {
    sessionService.authHeaderValue.and.returnValue(null);
    spyOn(console, 'warn');

    http.get('http://localhost:18080/api/v1/categorias').subscribe({
      error: () => undefined
    });

    const req = httpMock.expectOne('http://localhost:18080/api/v1/categorias');
    req.flush('texto no json', { status: 200, statusText: 'OK' });

    expect(console.warn).toHaveBeenCalledWith(
      jasmine.stringContaining('El Gateway respondio HTTP 200'),
      jasmine.objectContaining({
        url: 'http://localhost:18080/api/v1/categorias',
        method: 'GET',
        status: 200,
        statusText: 'OK'
      })
    );
  });

  it('clears session and redirects on 401 for protected requests', () => {
    sessionService.authHeaderValue.and.returnValue('Bearer expired-token');

    http.get('http://localhost:18080/api/v1/personas/me').subscribe({
      error: () => undefined
    });

    const req = httpMock.expectOne('http://localhost:18080/api/v1/personas/me');
    req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    expect(sessionService.clear).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login'], {
      queryParams: { returnUrl: '/publish' }
    });
  });
});
