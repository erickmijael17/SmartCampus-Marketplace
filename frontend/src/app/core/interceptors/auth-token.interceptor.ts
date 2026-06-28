import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { SessionService } from '../services/session.service';
import { GatewayService } from '../services/gateway.service';
import { describeHttpError } from '../utils/http-error.util';

export const authTokenInterceptor: HttpInterceptorFn = (request, next) => {
  const sessionService = inject(SessionService);
  const gatewayService = inject(GatewayService);
  const router = inject(Router);
  const authHeader = sessionService.authHeaderValue();
  const shouldAttachAuth = authHeader && !isPublicReadEndpoint(request.method, request.url);

  const authReq = shouldAttachAuth
    ? request.clone({
        setHeaders: {
          Authorization: authHeader
        }
      })
    : request;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && shouldAttachAuth && shouldHandleUnauthorized(request.url)) {
        sessionService.clear();

        const currentUrl = router.url;
        if (!currentUrl.startsWith('/login') && !currentUrl.startsWith('/register')) {
          void router.navigate(['/login'], {
            queryParams: { returnUrl: currentUrl }
          });
        }
      }

      const msg = describeHttpError(error, 'la solicitud', gatewayService.gatewayAvailable());
      console.warn(`[HTTP Error] ${request.method} ${request.url}: ${msg}`);
      return throwError(() => error);
    })
  );
};

function shouldHandleUnauthorized(url: string): boolean {
  const path = extractPath(url);
  return (
    !matchesPath(path, '/auth/login') &&
    !matchesPath(path, '/auth/register') &&
    !matchesPath(path, '/auth/me')
  );
}

function isPublicReadEndpoint(method: string, url: string): boolean {
  if (method.toUpperCase() !== 'GET') {
    return false;
  }

  const path = extractPath(url);
  return matchesPath(path, '/api/v1/productos') || matchesPath(path, '/api/v1/categorias');
}

function extractPath(url: string): string {
  try {
    return new URL(url).pathname;
  } catch {
    return url.split('?')[0] ?? url;
  }
}

function matchesPath(path: string, basePath: string): boolean {
  return path === basePath || path.startsWith(`${basePath}/`);
}
