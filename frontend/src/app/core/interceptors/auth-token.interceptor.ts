import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { SessionService } from '../services/session.service';

export const authTokenInterceptor: HttpInterceptorFn = (request, next) => {
  const sessionService = inject(SessionService);
  const authHeader = sessionService.authHeaderValue();

  // Temporary log for diagnosis:
  console.log(`[HTTP Request] ${request.method} ${request.url}`, {
    hasToken: !!authHeader,
    body: request.body
  });

  let authReq = request;
  if (authHeader) {
    authReq = request.clone({
      setHeaders: {
        Authorization: authHeader
      }
    });
  }

  return next(authReq).pipe(
    tap(event => {
      // Could log success responses here if needed
    }),
    catchError((error: HttpErrorResponse) => {
      console.error(`[HTTP Error] ${request.method} ${request.url}`, error);
      if (error.status === 401) {
        console.warn('No autorizado (401). El token puede haber expirado.');
      } else if (error.status === 403) {
        console.warn('Acceso denegado (403).');
      } else if (error.status === 404) {
        console.warn('Recurso no encontrado (404).');
      } else if (error.status === 500) {
        console.error('Error interno del servidor (500).');
      } else if (error.status === 0) {
        console.error('Error de red o CORS (0). El backend podría estar apagado o bloqueando la petición por CORS.');
      }
      return throwError(() => error);
    })
  );
};
