import { computed, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AppUser, UserRole } from '../core/models';
import { AuthService } from '../core/services/auth.service';
import { AppShellComponent } from './app-shell.component';

describe('AppShellComponent', () => {
  async function navigationFor(role: UserRole): Promise<string[]> {
    const user = signal<AppUser>({
      id: 1,
      name: 'Pessoa',
      email: 'pessoa@local.test',
      role,
      specialty: null,
      managerId: null,
      managerName: null,
      mustChangePassword: false,
    });
    TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [AppShellComponent],
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: {
            user,
            isAdmin: computed(() => user().role === 'ADMIN'),
            canManage: computed(() => user().role !== 'TECHNICIAN'),
            logout: vi.fn(),
          },
        },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(AppShellComponent);
    fixture.detectChanges();
    return Array.from(
      (fixture.nativeElement as HTMLElement).querySelectorAll(
        '.sidebar__nav .nav-label:not(.nav-label--short)',
      ),
    ).map((label) => label.textContent?.trim() ?? '');
  }

  it('shows Técnicos to administrators and managers only', async () => {
    expect(await navigationFor('ADMIN')).toContain('Técnicos');
    expect(await navigationFor('ADMIN')).toContain('Usuários');
    expect(await navigationFor('MANAGER')).toContain('Técnicos');
    expect(await navigationFor('MANAGER')).not.toContain('Usuários');
    expect(await navigationFor('TECHNICIAN')).not.toContain('Técnicos');
  });
});
