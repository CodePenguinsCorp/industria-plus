import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AssignmentPayload,
  MaintenanceRequest,
  MaintenanceRequestFilters,
  MaintenanceRequestPayload,
  MaintenanceRequestStatus,
  MaintenanceStatusPayload,
} from '../models';

@Injectable({ providedIn: 'root' })
export class MaintenanceRequestService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/maintenance-requests';

  list(filters: MaintenanceRequestFilters = {}): Observable<MaintenanceRequest[]> {
    let params = new HttpParams();

    if (filters.status) {
      params = params.set('status', filters.status);
    }

    if (filters.urgency) {
      params = params.set('urgency', filters.urgency);
    }

    return this.http.get<MaintenanceRequest[]>(this.baseUrl, { params });
  }

  create(payload: MaintenanceRequestPayload): Observable<MaintenanceRequest> {
    return this.http.post<MaintenanceRequest>(this.baseUrl, payload);
  }

  assign(id: number, technicianId: number): Observable<MaintenanceRequest> {
    const payload: AssignmentPayload = { technicianId };
    return this.http.patch<MaintenanceRequest>(`${this.baseUrl}/${id}/assignment`, payload);
  }

  updateStatus(id: number, status: MaintenanceRequestStatus): Observable<MaintenanceRequest> {
    const payload: MaintenanceStatusPayload = { status };
    return this.http.patch<MaintenanceRequest>(`${this.baseUrl}/${id}/status`, payload);
  }
}
