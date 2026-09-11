import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Equipment, EquipmentPayload } from '../models';

@Injectable({ providedIn: 'root' })
export class EquipmentService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/equipments';

  list(): Observable<Equipment[]> {
    return this.http.get<Equipment[]>(this.baseUrl);
  }

  create(payload: EquipmentPayload): Observable<Equipment> {
    return this.http.post<Equipment>(this.baseUrl, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
