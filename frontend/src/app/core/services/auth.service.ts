import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, firstValueFrom, map, switchMap, tap } from 'rxjs';
import { AppUser } from '../models';
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  readonly user = signal<AppUser | null>(null);
  readonly canManage = computed(
    () => this.user()?.role === 'ADMIN' || this.user()?.role === 'MANAGER',
  );
  readonly isAdmin = computed(() => this.user()?.role === 'ADMIN');
  readonly mustChangePassword = computed(() => this.user()?.mustChangePassword === true);
  csrf() {
    return this.http.get('/api/auth/csrf');
  }
  async restore(): Promise<boolean> {
    if (this.user()) return true;
    try {
      await firstValueFrom(this.csrf());
      this.user.set(await firstValueFrom(this.http.get<AppUser>('/api/auth/me')));
      return true;
    } catch {
      this.user.set(null);
      return false;
    }
  }
  login(email: string, password: string): Observable<AppUser> {
    const body = new HttpParams().set('email', email.trim()).set('password', password);
    return this.csrf().pipe(
      switchMap(() => this.http.post<AppUser>('/api/auth/login', body)),
      tap((user) => this.user.set(user)),
      switchMap((user) => this.csrf().pipe(map(() => user))),
    );
  }
  /** Troca a própria senha; obrigatória enquanto a senha for provisória. */
  changePassword(currentPassword: string, newPassword: string): Observable<AppUser> {
    return this.http
      .post<AppUser>('/api/auth/password', { currentPassword, newPassword })
      .pipe(tap((user) => this.user.set(user)));
  }
  logout() {
    this.http.post('/api/auth/logout', {}).subscribe({
      next: () => {
        this.user.set(null);
        void this.router.navigateByUrl('/login');
      },
      error: () => {
        this.user.set(null);
        void this.router.navigateByUrl('/login');
      },
    });
  }
  /** Técnicos só atendem os chamados atribuídos a eles (o técnico é o próprio usuário). */
  canAttend(technicianId: number | null): boolean {
    const user = this.user();
    return (
      this.canManage() ||
      (user?.role === 'TECHNICIAN' && technicianId !== null && user.id === technicianId)
    );
  }
}
