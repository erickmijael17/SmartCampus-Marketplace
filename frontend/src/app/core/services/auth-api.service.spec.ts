import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { of } from 'rxjs';
import { AuthApiService } from './auth-api.service';
import { GatewayService } from './gateway.service';
import { PersonaApiService } from './persona-api.service';
import { SessionService } from './session.service';
import { AuthSession } from '../models/auth.model';

describe('AuthApiService', () => {
  let service: AuthApiService;
  let httpMock: HttpTestingController;
  let sessionService: jasmine.SpyObj<SessionService>;
  let personaApi: jasmine.SpyObj<PersonaApiService>;

  const loginResponse: AuthSession = {
    accessToken: 'login-token',
    tokenType: 'Bearer',
    expiresIn: 3600,
    username: 'demo',
    roles: ['USER']
  };

  beforeEach(() => {
    sessionService = jasmine.createSpyObj('SessionService', ['setSession', 'clear', 'session', 'isSessionExpired']);
    personaApi = jasmine.createSpyObj('PersonaApiService', ['getMyProfile']);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        AuthApiService,
        { provide: GatewayService, useValue: { baseUrl: () => 'http://localhost:18080' } },
        { provide: SessionService, useValue: sessionService },
        { provide: PersonaApiService, useValue: personaApi }
      ]
    });

    service = TestBed.inject(AuthApiService);
    httpMock = TestBed.inject(HttpTestingController);
    sessionService.session.and.returnValue(loginResponse);
    sessionService.isSessionExpired.and.returnValue(false);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('login establishes session through /auth/me and persona-ms', () => {
    personaApi.getMyProfile.and.returnValue(
      of({
        id: 1,
        userId: 1001,
        nombres: 'Ana',
        apellidos: 'Ramos',
        email: 'ana@test.com',
        tipoUsuario: 'ESTUDIANTE',
        activo: true
      })
    );

    service.login({ username: 'demo', password: 'secret123' }).subscribe((session) => {
      expect(session.userId).toBe(1001);
      expect(session.personaId).toBe(1);
    });

    const loginReq = httpMock.expectOne('http://localhost:18080/auth/login');
    expect(loginReq.request.method).toBe('POST');
    loginReq.flush(loginResponse);

    const meReq = httpMock.expectOne('http://localhost:18080/auth/me');
    meReq.flush({ username: 'demo', roles: ['USER'], userId: 'uuid-keycloak' });

    expect(sessionService.setSession).toHaveBeenCalled();
  });

  it('refreshSession clears session when /auth/me fails', () => {
    service.refreshSession().subscribe((session) => {
      expect(session).toBeNull();
    });

    const meReq = httpMock.expectOne('http://localhost:18080/auth/me');
    meReq.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    expect(sessionService.clear).toHaveBeenCalled();
  });
});
