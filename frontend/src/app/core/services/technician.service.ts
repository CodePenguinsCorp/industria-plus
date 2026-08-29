import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Technician, TechnicianPayload } from '../models';

@Injectable({ providedIn: 'root' })
export class TechnicianService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/technicians';

  list(): Observable<Technician[]> {
    return this.http.get<Technician[]>(this.baseUrl);
  }

  create(payload: TechnicianPayload): Observable<Technician> {
    return this.http.post<Technician>(this.baseUrl, payload);
  }
}
