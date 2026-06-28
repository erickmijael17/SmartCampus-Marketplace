import { ApplicationConfig, inject, provideBrowserGlobalErrorListeners, provideAppInitializer, provideZoneChangeDetection } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { routes } from './app.routes';
import { authTokenInterceptor } from './core/interceptors/auth-token.interceptor';
import { AuthApiService } from './core/services/auth-api.service';
import { GatewayService } from './core/services/gateway.service';
import { SessionService } from './core/services/session.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideHttpClient(withInterceptors([authTokenInterceptor])),
    provideRouter(routes),
    provideAppInitializer(async () => {
      const gateway = inject(GatewayService);
      const sessionService = inject(SessionService);
      const authApi = inject(AuthApiService);
      const router = inject(Router);

      sessionService.sessionExpired$.subscribe(() => {
        const currentUrl = router.url;
        if (!currentUrl.startsWith('/login') && !currentUrl.startsWith('/register')) {
          void router.navigate(['/login'], {
            queryParams: { returnUrl: currentUrl, reason: 'session-expired' }
          });
        }
      });

      await gateway.detectActiveGateway();

      if (sessionService.isSessionExpired()) {
        sessionService.clear();
        return;
      }

      if (sessionService.isAuthenticated()) {
        await firstValueFrom(authApi.refreshSession());
      }
    })
  ]
};