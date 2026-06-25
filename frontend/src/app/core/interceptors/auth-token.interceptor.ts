import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { SessionService } from '../services/session.service';
import { GatewayService } from '../services/gateway.service';
import { describeHttpError } from '../utils/http-error.util';

export const authTokenInterceptor: HttpInterceptorFn = (request, next) => {
  const sessionService = inject(SessionService);
  const gatewayService = inject(GatewayService);
  const authHeader = sessionService.authHeaderValue();

  const authReq = authHeader
    ? request.clone({
        setHeaders: {
          Authorization: authHeader
        }
      })
    : request;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      const msg = describeHttpError(error, 'la solicitud', gatewayService.gatewayAvailable());
      console.warn(`[HTTP Error] ${request.method} ${request.url}: ${msg}`);
      return throwError(() => error);
    })
  );
};

