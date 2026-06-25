import { Injectable, signal, computed } from '@angular/core';
import { Observable, ReplaySubject } from 'rxjs';
import { environment } from '../../../environments/environment';

const HEALTH_TIMEOUT_MS = 3_000;

export type GatewayLabel = 'PROD' | 'DEV' | 'NONE';

@Injectable({ providedIn: 'root' })
export class GatewayService {
  private readonly baseUrlSignal = signal<string>('');
  private readonly labelSignal = signal<GatewayLabel>('NONE');
  private readonly availableSignal = signal<boolean>(false);

  private readonly initializedSubject = new ReplaySubject<boolean>(1);
  readonly initialized$: Observable<boolean> = this.initializedSubject.asObservable();

  readonly gatewayAvailable = this.availableSignal.asReadonly();
  readonly activeGatewayLabel = computed(() => this.labelSignal());

  baseUrl(): string {
    return this.baseUrlSignal();
  }

  /**
   * Probes each gateway candidate in priority order (PROD first).
   * Uses native fetch + AbortController to avoid HttpClient/interceptor conflicts.
   */
  async detectActiveGateway(): Promise<void> {
    for (const candidate of environment.gatewayCandidates) {
      const isReachable = await this.probe(candidate.url);
      if (isReachable) {
        this.baseUrlSignal.set(candidate.url);
        this.labelSignal.set(candidate.label as GatewayLabel);
        this.availableSignal.set(true);
        console.info(`[GatewayService] Gateway activo: ${candidate.label} (${candidate.url})`);
        this.initializedSubject.next(true);
        return;
      }
    }

    this.baseUrlSignal.set('');
    this.labelSignal.set('NONE');
    this.availableSignal.set(false);
    console.error(
      '[GatewayService] No hay Gateway disponible. Se intentó PROD y DEV.'
    );
    this.initializedSubject.next(true);
  }

  private async probe(url: string): Promise<boolean> {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), HEALTH_TIMEOUT_MS);

    try {
      const response = await fetch(`${url}/actuator/info`, {
        method: 'GET',
        signal: controller.signal,
      });
      return response.ok;
    } catch {
      return false;
    } finally {
      clearTimeout(timeoutId);
    }
  }
}
