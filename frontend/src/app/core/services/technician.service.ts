import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ManagerOption, Technician } from '../models';

/** Técnicos são cadastrados em Usuários; aqui ficam consulta, vínculo com gestor e exclusão. */
@Injectable({ providedIn: 'root' })
export class TechnicianService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/technicians';

  list(): Observable<Technician[]> {
    return this.http.get<Technician[]>(this.baseUrl);
  }

  managers(): Observable<ManagerOption[]> {
    return this.http.get<ManagerOption[]>(`${this.baseUrl}/managers`);
  }

  updateManager(id: number, managerId: number): Observable<Technician> {
    return this.http.patch<Technician>(`${this.baseUrl}/${id}/manager`, { managerId });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
