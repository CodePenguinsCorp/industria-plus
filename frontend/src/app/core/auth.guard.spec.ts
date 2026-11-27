import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  Router,
  RouterStateSnapshot,
  UrlTree,
  provideRouter,
} from '@angular/router';
import { AppUser, UserRole } from './models';
import { authGuard, managerGuard, passwordChangeGuard } from './auth.guard';
import { AuthService } from './services/auth.service';

describe('auth guards', () => {
  let current: AppUser | null;
  const auth = {
    restore: vi.fn(async () => current !== null),
    mustChangePassword: () => current?.mustChangePassword === true,
    canManage: () => current?.role === 'ADMIN' || current?.role === 'MANAGER',
    isAdmin: () => current?.role === 'ADMIN',
  };

  function as(role: UserRole, mustChangePassword = false) {
    current = {
      id: 1,
      name: 'Pessoa',
      email: 'pessoa@local.test',
      role,
      specialty: null,
      managerId: null,
      managerName: null,
      mustChangePassword,
    };
  }

  beforeEach(() => {
    current = null;
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: AuthService, useValue: auth }],
    });
  });

  async function run(guard: typeof authGuard): Promise<string | true> {
    const result = await TestBed.runInInjectionContext(() =>
      guard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );
    if (result === true) return true;
    return TestBed.inject(Router).serializeUrl(result as UrlTree);
  }

  it('sends users with a temporary password to the first access page', async () => {
    expect(await run(authGuard)).toBe('/login');
    as('TECHNICIAN', true);
    expect(await run(authGuard)).toBe('/primeiro-acesso');
    as('TECHNICIAN', false);
    expect(await run(authGuard)).toBe(true);
  });

  it('only opens the first access page while the password is temporary', async () => {
    expect(await run(passwordChangeGuard)).toBe('/login');
    as('MANAGER', true);
    expect(await run(passwordChangeGuard)).toBe(true);
    as('MANAGER', false);
    expect(await run(passwordChangeGuard)).toBe('/');
  });

  it('opens the technicians page only for administrators and managers', async () => {
    as('ADMIN');
    expect(await run(managerGuard)).toBe(true);
    as('MANAGER');
    expect(await run(managerGuard)).toBe(true);
    as('TECHNICIAN');
    expect(await run(managerGuard)).toBe('/');
  });
});
