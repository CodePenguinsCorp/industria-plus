import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { EquipmentDetail, HistoryEntry, ReplacedPart } from '../models';
@Injectable({ providedIn: 'root' })
export class HistoryService {
  private readonly http = inject(HttpClient);
  request(id: number) {
    return this.http.get<HistoryEntry[]>(`/api/maintenance-requests/${id}/history`);
  }
  equipment(id: number) {
    return this.http.get<EquipmentDetail>(`/api/equipments/${id}`);
  }
  intervene(id: number, description: string, parts: ReplacedPart[]) {
    return this.http.post<HistoryEntry>(`/api/maintenance-requests/${id}/interventions`, {
      description,
      parts,
    });
  }
}
