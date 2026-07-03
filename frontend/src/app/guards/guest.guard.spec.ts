import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { guestGuard } from './guest.guard';
import { SessionService } from '../core/services/session.service';

describe('guestGuard', () => {
  let sessionService: jasmine.SpyObj<SessionService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    sessionService = jasmine.createSpyObj('SessionService', ['isAuthenticated']);
    router = jasmine.createSpyObj('Router', ['createUrlTree']);
    router.createUrlTree.and.returnValue({} as UrlTree);

    TestBed.configureTestingModule({
      providers: [
        { provide: SessionService, useValue: sessionService },
        { provide: Router, useValue: router }
      ]
    });
  });

  it('allows guests', () => {
    sessionService.isAuthenticated.and.returnValue(false);

    const result = TestBed.runInInjectionContext(() => guestGuard({} as never, {} as never));

    expect(result).toBeTrue();
  });

  it('redirects authenticated users away from login/register', () => {
    sessionService.isAuthenticated.and.returnValue(true);

    TestBed.runInInjectionContext(() => guestGuard({} as never, {} as never));

    expect(router.createUrlTree).toHaveBeenCalledWith(['/']);
  });
});
