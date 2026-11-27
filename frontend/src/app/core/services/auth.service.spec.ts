import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AppUser } from '../models';
import { AuthService } from './auth.service';

function appUser(overrides: Partial<AppUser>): AppUser {
  return {
    id: 1,
    name: 'Usuário',
    email: 'usuario@local.test',
    role: 'ADMIN',
    specialty: null,
    managerId: null,
    managerName: null,
    mustChangePassword: false,
    ...overrides,
  };
}

describe('AuthService', () => {
  let auth: AuthService;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    auth = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());
  it('limits attendance to the technician user while managers can manage', () => {
    auth.user.set(appUser({ id: 7, name: 'Ana', role: 'TECHNICIAN', managerId: 2 }));
    expect(auth.canManage()).toBe(false);
    expect(auth.isAdmin()).toBe(false);
    expect(auth.canAttend(7)).toBe(true);
    expect(auth.canAttend(8)).toBe(false);
    expect(auth.canAttend(null)).toBe(false);
    auth.user.set(appUser({ id: 2, name: 'Gestor', role: 'MANAGER' }));
    expect(auth.canManage()).toBe(true);
    expect(auth.isAdmin()).toBe(false);
    expect(auth.canAttend(8)).toBe(true);
  });
  it('fetches CSRF before login and refreshes it after authentication', () => {
    let result: AppUser | undefined;
    auth.login(' admin@local.test ', 'SenhaDeTeste123!').subscribe((user) => (result = user));
    http.expectOne('/api/auth/csrf').flush({ token: 'first' });
    const login = http.expectOne('/api/auth/login');
    expect(login.request.method).toBe('POST');
    expect(login.request.body.get('email')).toBe('admin@local.test');
    login.flush(appUser({ name: 'Administrador', email: 'admin@local.test' }));
    http.expectOne('/api/auth/csrf').flush({ token: 'new' });
    expect(result?.email).toBe('admin@local.test');
    expect(auth.isAdmin()).toBe(true);
    expect(auth.mustChangePassword()).toBe(false);
  });
  it('exposes the temporary password flag and clears it after changing the password', () => {
    auth.user.set(appUser({ id: 7, role: 'TECHNICIAN', mustChangePassword: true }));
    expect(auth.mustChangePassword()).toBe(true);

    auth.changePassword('Provisoria1234', 'NovaSenhaSegura1').subscribe();
    const request = http.expectOne('/api/auth/password');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      currentPassword: 'Provisoria1234',
      newPassword: 'NovaSenhaSegura1',
    });
    request.flush(appUser({ id: 7, role: 'TECHNICIAN', mustChangePassword: false }));

    expect(auth.mustChangePassword()).toBe(false);
  });
});
