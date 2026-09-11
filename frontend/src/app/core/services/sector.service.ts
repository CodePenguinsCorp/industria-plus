import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Sector, SectorPayload } from '../models';

@Injectable({ providedIn: 'root' })
export class SectorService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/sectors';

  list(): Observable<Sector[]> {
    return this.http.get<Sector[]>(this.baseUrl);
  }

  create(payload: SectorPayload): Observable<Sector> {
    return this.http.post<Sector>(this.baseUrl, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
