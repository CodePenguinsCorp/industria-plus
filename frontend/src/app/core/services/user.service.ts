import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AppUser, UserPayload } from '../models';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/users';

  list(): Observable<AppUser[]> {
    return this.http.get<AppUser[]>(this.baseUrl);
  }

  create(payload: UserPayload): Observable<AppUser> {
    return this.http.post<AppUser>(this.baseUrl, payload);
  }

  /** Define uma nova senha provisória; o usuário deverá trocá-la ao entrar. */
  resetPassword(id: number, password: string): Observable<AppUser> {
    return this.http.post<AppUser>(`${this.baseUrl}/${id}/password-reset`, { password });
  }
}
