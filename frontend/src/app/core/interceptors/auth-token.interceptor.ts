import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { SessionService } from '../services/session.service';

export const authTokenInterceptor: HttpInterceptorFn = (request, next) => {
  const sessionService = inject(SessionService);
  const authHeader = sessionService.authHeaderValue();

  if (!authHeader) {
    return next(request);
  }

  return next(
    request.clone({
      setHeaders: {
        Authorization: authHeader
      }
    })
  );
};
