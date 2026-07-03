import { Injectable, signal, computed } from '@angular/core';
import { Observable, ReplaySubject } from 'rxjs';
import { environment } from '../../../environments/environment';
import { GatewayLabel } from '../../../environments/environment.model';
import { API_CONFIG } from '../config/api.config';

const HEALTH_TIMEOUT_MS = 3_000;

@Injectable({ providedIn: 'root' })
export class GatewayService {
  private readonly baseUrlSignal = signal<string>(environment.gatewayUrl ?? API_CONFIG.gatewayUrl);
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
   * Resolves the active Gateway URL.
   * Production uses a fixed public URL; development probes candidates in order.
   */
  async detectActiveGateway(): Promise<void> {
    if (!environment.gatewayProbeEnabled) {
      this.applyCandidate(environment.gatewayCandidates[0], true);
      console.info(
        `[GatewayService] Gateway fijo (${environment.production ? 'PROD' : 'config'}): ${this.baseUrl()}`
      );
      this.initializedSubject.next(true);
      return;
    }

    for (const candidate of environment.gatewayCandidates) {
      const isReachable = await this.probe(candidate.url);
      if (isReachable) {
        this.applyCandidate(candidate, true);
        console.info(`[GatewayService] Gateway activo: ${candidate.label} (${candidate.url})`);
        this.initializedSubject.next(true);
        return;
      }
    }

    const fallback = environment.gatewayCandidates[0];
    this.applyCandidate(fallback, false);
    console.error(
      `[GatewayService] No hay Gateway disponible. Se intentaron todos los candidatos. Fallback: ${fallback?.url ?? 'ninguno'}`
    );
    this.initializedSubject.next(true);
  }

  private applyCandidate(
    candidate: (typeof environment.gatewayCandidates)[number] | undefined,
    available: boolean
  ): void {
    this.baseUrlSignal.set(candidate?.url ?? environment.gatewayUrl ?? API_CONFIG.gatewayUrl);
    this.labelSignal.set((candidate?.label as GatewayLabel) ?? 'NONE');
    this.availableSignal.set(available);
  }

  private async probe(url: string): Promise<boolean> {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), HEALTH_TIMEOUT_MS);

    try {
      const response = await fetch(`${url}/actuator/health`, {
        method: 'GET',
        signal: controller.signal
      });
      return response.ok;
    } catch {
      try {
        const infoResponse = await fetch(`${url}/actuator/info`, {
          method: 'GET',
          signal: controller.signal
        });
        return infoResponse.ok;
      } catch {
        return false;
      }
    } finally {
      clearTimeout(timeoutId);
    }
  }
}
