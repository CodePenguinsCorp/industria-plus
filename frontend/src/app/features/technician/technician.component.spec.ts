import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ManagerOption, Technician } from '../../core/models';
import { AuthService } from '../../core/services/auth.service';
import { TechnicianService } from '../../core/services/technician.service';
import { TechnicianComponent } from './technician.component';

describe('TechnicianComponent', () => {
  const managers: ManagerOption[] = [
    { id: 10, name: 'Gestor Central', email: 'central@industria.local' },
    { id: 11, name: 'Gestora Norte', email: 'norte@industria.local' },
  ];
  const ana: Technician = {
    id: 3,
    name: 'Ana Martins',
    email: 'ana@industria.local',
    specialty: 'Hidráulica',
    managerId: 10,
    managerName: 'Gestor Central',
    highUrgencyOpenRequests: 1,
    createdAt: '2026-08-28T08:20:00',
  };
  const semGestor: Technician = {
    ...ana,
    id: 4,
    name: 'Bruno Lima',
    email: 'bruno@industria.local',
    managerId: null,
    managerName: null,
  };

  const technicianService = {
    list: vi.fn(() => of([ana, semGestor])),
    managers: vi.fn(() => of(managers)),
    updateManager: vi.fn(() => of({ ...ana, managerId: 11, managerName: 'Gestora Norte' })),
    delete: vi.fn(() => of(void 0)),
  };

  async function render(role: 'ADMIN' | 'MANAGER') {
    vi.clearAllMocks();
    technicianService.list.mockReturnValue(of([ana, semGestor]));
    technicianService.managers.mockReturnValue(of(managers));
    technicianService.updateManager.mockReturnValue(
      of({ ...ana, managerId: 11, managerName: 'Gestora Norte' }),
    );
    await TestBed.configureTestingModule({
      imports: [TechnicianComponent],
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: { isAdmin: () => role === 'ADMIN', canManage: () => true },
        },
        { provide: TechnicianService, useValue: technicianService },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(TechnicianComponent);
    fixture.detectChanges();
    return fixture;
  }

  function selects(element: HTMLElement): HTMLSelectElement[] {
    return Array.from(element.querySelectorAll<HTMLSelectElement>('select.manager-select'));
  }

  it('no longer offers a form to register technicians', async () => {
    const fixture = await render('ADMIN');
    const element: HTMLElement = fixture.nativeElement;

    expect(element.textContent).not.toContain('Novo técnico');
    expect(element.querySelector('form')).toBeNull();
    expect(element.textContent).toContain('Usuários');
  });

  it('shows the linked manager of each technician in a dropdown', async () => {
    const fixture = await render('ADMIN');
    const [anaSelect, brunoSelect] = selects(fixture.nativeElement);

    expect(anaSelect.selectedOptions[0].textContent?.trim()).toBe('Gestor Central');
    expect(brunoSelect.selectedOptions[0].textContent?.trim()).toBe('Sem gestor vinculado');
    expect(anaSelect.options.length).toBe(2);
  });

  it('lets the administrator change the linked manager', async () => {
    const fixture = await render('ADMIN');
    const [anaSelect] = selects(fixture.nativeElement);
    expect(anaSelect.disabled).toBe(false);

    anaSelect.value = '11';
    anaSelect.dispatchEvent(new Event('change'));
    fixture.detectChanges();

    expect(technicianService.updateManager).toHaveBeenCalledWith(3, 11);
    const component = fixture.componentInstance as any;
    expect(component.technicians()[0].managerName).toBe('Gestora Norte');
    expect(component.successMessage()).toContain('Gestora Norte');
  });

  it('restores the previous manager when the change is rejected', async () => {
    const fixture = await render('ADMIN');
    technicianService.updateManager.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 400,
            error: { code: 'INVALID_MANAGER_LINK', message: 'Selecione um gestor válido.' },
          }),
      ),
    );
    const [anaSelect] = selects(fixture.nativeElement);

    anaSelect.value = '11';
    anaSelect.dispatchEvent(new Event('change'));
    fixture.detectChanges();

    expect(anaSelect.value).toBe('10');
    expect((fixture.componentInstance as any).errorMessage()).toBe('Selecione um gestor válido.');
  });

  it('shows the manager read-only to managers, without delete actions', async () => {
    const fixture = await render('MANAGER');
    const element: HTMLElement = fixture.nativeElement;
    const [anaSelect] = selects(element);

    expect(anaSelect.disabled).toBe(true);
    expect(anaSelect.selectedOptions[0].textContent?.trim()).toBe('Gestor Central');
    expect(element.textContent).not.toContain('Excluir');

    anaSelect.value = '11';
    anaSelect.dispatchEvent(new Event('change'));
    expect(technicianService.updateManager).not.toHaveBeenCalled();
  });

  it('searches technicians by manager name', async () => {
    const fixture = await render('MANAGER');
    const component = fixture.componentInstance as any;

    component.searchControl.setValue('gestor central');

    expect(component.filteredTechnicians).toEqual([ana]);
  });
});
