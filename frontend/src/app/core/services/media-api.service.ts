import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../config/api.config';
import { MediaFileRequest, MediaFileResponse } from '../models/media.model';
import { GatewayService } from './gateway.service';

@Injectable({ providedIn: 'root' })
export class MediaApiService {
  private readonly http = inject(HttpClient);
  private readonly gateway = inject(GatewayService);

  findAll(): Observable<MediaFileResponse[]> {
    return this.http.get<MediaFileResponse[]>(this.url(API_CONFIG.endpoints.media.base));
  }

  create(request: MediaFileRequest): Observable<MediaFileResponse> {
    return this.http.post<MediaFileResponse>(this.url(API_CONFIG.endpoints.media.base), request);
  }

  upload(file: File, idUploader: number, idPublicacion: number): Observable<MediaFileResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('idUploader', String(idUploader));
    formData.append('idPublicacion', String(idPublicacion));

    return this.http.post<MediaFileResponse>(this.url(`${API_CONFIG.endpoints.media.base}/upload`), formData);
  }

  private url(path: string): string {
    return path.startsWith('http://') || path.startsWith('https://') ? path : this.gateway.baseUrl() + path;
  }
}
