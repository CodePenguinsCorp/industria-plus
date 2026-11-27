import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AuthService } from '../../core/services/auth.service';
import { EquipmentMaintenanceComponent } from './equipment-maintenance.component';

describe('EquipmentMaintenanceComponent', () => {
  let http: HttpTestingController;
  const auth = { canManage: () => true };
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [EquipmentMaintenanceComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: auth },
      ],
    });
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  function create(state = 'INSUFFICIENT_DATA', mtbfHours: number | null = null) {
    const fixture = TestBed.createComponent(EquipmentMaintenanceComponent);
    fixture.componentRef.setInput('equipmentId', 1);
    fixture.detectChanges();
    http.expectOne('/api/equipments/1/reliability').flush({
      startDate: null,
      endDate: null,
      operatingHours: null,
      failureCount: 0,
      mtbfHours,
      state,
    });
    http.expectOne('/api/equipments/1/preventive-plan').flush(null);
    fixture.detectChanges();
    return fixture;
  }

  it('distinguishes insufficient data from a calculated MTBF', () => {
    const fixture = create('CALCULATED', 50);
    expect(fixture.nativeElement.textContent).toContain('50');
    expect(fixture.nativeElement.textContent).toContain('MTBF');
  });

  it('sends the real hours and matching period entered by the manager', () => {
    const fixture = create();
    expect(fixture.nativeElement.textContent).toContain('Dados insuficientes');
    fixture.componentInstance.reliabilityForm.setValue({
      startDate: '2026-10-01',
      endDate: '2026-10-31',
      operatingHours: 100,
    });
    fixture.componentInstance.saveReliability();
    const request = http.expectOne('/api/equipments/1/reliability');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({
      startDate: '2026-10-01',
      endDate: '2026-10-31',
      operatingHours: 100,
    });
    request.flush({
      startDate: '2026-10-01',
      endDate: '2026-10-31',
      operatingHours: 100,
      failureCount: 2,
      mtbfHours: 50,
      state: 'CALCULATED',
    });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('50');
  });

  it('shows the next actual due date returned after generating an overdue plan', () => {
    const fixture = create();
    fixture.componentInstance.planForm.setValue({
      title: 'Inspeção',
      description: 'Inspeção da máquina.',
      intervalDays: 7,
      nextDueDate: '2026-10-01',
      enabled: true,
    });
    fixture.componentInstance.savePlan();
    const request = http.expectOne('/api/equipments/1/preventive-plan');
    expect(request.request.method).toBe('PUT');
    request.flush({
      id: 1,
      title: 'Inspeção',
      description: 'Inspeção da máquina.',
      intervalDays: 7,
      nextDueDate: '2026-12-01',
      enabled: true,
    });
    expect(fixture.componentInstance.planForm.controls.nextDueDate.value).toBe('2026-12-01');
  });
});
