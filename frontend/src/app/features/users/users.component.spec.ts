import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AppUser } from '../../core/models';
import { UserService } from '../../core/services/user.service';
import { UsersComponent } from './users.component';

describe('UsersComponent', () => {
  const base = { specialty: null, managerId: null, managerName: null, mustChangePassword: false };
  const users: AppUser[] = [
    { ...base, id: 1, name: 'Administrador', email: 'admin@local.test', role: 'ADMIN' },
    { ...base, id: 2, name: 'Gestor Central', email: 'gestor@local.test', role: 'MANAGER' },
    {
      ...base,
      id: 3,
      name: 'Ana Martins',
      email: 'ana@local.test',
      role: 'TECHNICIAN',
      managerId: 2,
      managerName: 'Gestor Central',
      mustChangePassword: true,
    },
  ];

  const userService = {
    list: vi.fn(() => of(users)),
    create: vi.fn((payload: unknown) => of({ ...users[2], ...(payload as object), id: 4 })),
    resetPassword: vi.fn(() => of({ ...users[1], mustChangePassword: true })),
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    userService.list.mockReturnValue(of(users));
    await TestBed.configureTestingModule({
      imports: [UsersComponent],
      providers: [{ provide: UserService, useValue: userService }],
    }).compileComponents();
  });

  function render() {
    const fixture = TestBed.createComponent(UsersComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('lists every registered access, with manager and password status', () => {
    const fixture = render();
    const rows = Array.from(
      (fixture.nativeElement as HTMLElement).querySelectorAll('tbody tr'),
    ).map((row) => row.textContent ?? '');

    expect(rows.length).toBe(3);
    expect(rows[0]).toContain('Administrador');
    expect(rows[1]).toContain('Gestor');
    expect(rows[2]).toContain('Gestor Central');
    expect(rows[2]).toContain('Provisória');
  });

  it('asks for the linked manager (not a technician record) when creating a technician', () => {
    const fixture = render();
    const component = fixture.componentInstance;
    component.form.controls.role.setValue('TECHNICIAN');
    fixture.detectChanges();
    const element: HTMLElement = fixture.nativeElement;

    expect(element.querySelector('label[for="user-manager"]')?.textContent).toBe(
      'Gestor vinculado',
    );
    expect(element.textContent).not.toContain('Técnico vinculado');
    const options = Array.from(
      element.querySelectorAll<HTMLOptionElement>('#user-manager option'),
    ).map((option) => option.textContent?.trim());
    expect(options).toEqual(['Selecione', 'Gestor Central · gestor@local.test']);
  });

  it('requires a manager for technicians and sends it in the payload', () => {
    const fixture = render();
    const component = fixture.componentInstance;
    component.form.setValue({
      name: ' Bruno Lima ',
      email: 'bruno@local.test',
      password: 'SenhaProvisoria1',
      role: 'TECHNICIAN',
      managerId: 0,
      specialty: ' Elétrica ',
    });

    component.save();
    expect(userService.create).not.toHaveBeenCalled();
    expect(component.error()).toBe('Selecione o gestor vinculado ao técnico.');

    component.form.controls.managerId.setValue(2);
    component.save();
    expect(userService.create).toHaveBeenCalledWith({
      name: 'Bruno Lima',
      email: 'bruno@local.test',
      password: 'SenhaProvisoria1',
      role: 'TECHNICIAN',
      managerId: 2,
      specialty: 'Elétrica',
    });
    expect(userService.list).toHaveBeenCalledTimes(2);
  });

  it('does not send a manager link for managers or administrators', () => {
    const component = render().componentInstance;
    component.form.setValue({
      name: 'Nova Gestora',
      email: 'nova@local.test',
      password: 'SenhaProvisoria1',
      role: 'MANAGER',
      managerId: 2,
      specialty: 'Ignorada',
    });

    component.save();

    expect(userService.create).toHaveBeenCalledWith(
      expect.objectContaining({ role: 'MANAGER', managerId: null, specialty: null }),
    );
  });

  it('defines a new temporary password for an existing user', () => {
    const fixture = render();
    const component = fixture.componentInstance;

    component.startReset(users[1]);
    component.resetForm.setValue({ password: 'curta' });
    component.resetPassword(users[1]);
    expect(userService.resetPassword).not.toHaveBeenCalled();

    component.resetForm.setValue({ password: 'OutraProvisoria1' });
    component.resetPassword(users[1]);

    expect(userService.resetPassword).toHaveBeenCalledWith(2, 'OutraProvisoria1');
    expect(component.users()[1].mustChangePassword).toBe(true);
    expect(component.resettingId()).toBeNull();
  });
});
