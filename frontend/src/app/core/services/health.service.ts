import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { HealthStatus } from '../models';

@Injectable({ providedIn: 'root' })
export class HealthService {
  private readonly http = inject(HttpClient);

  getStatus(): Observable<HealthStatus> {
    return this.http.get<HealthStatus>('/api/health');
  }
}
