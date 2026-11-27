import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { EquipmentDetailComponent } from './equipment-detail.component';
import { EquipmentMaintenanceComponent } from './equipment-maintenance.component';

describe('EquipmentDetailComponent', () => {
  it('refreshes the visible request list after saving an overdue preventive plan', () => {
    TestBed.configureTestingModule({
      imports: [EquipmentDetailComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { paramMap: of(convertToParamMap({ id: '1' })) } },
        { provide: AuthService, useValue: { canManage: () => true } },
      ],
    });
    const http = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(EquipmentDetailComponent);
    const equipment = {
      id: 1,
      assetTag: 'EQ-01',
      name: 'Prensa',
      sectorId: 1,
      sectorName: 'Produção',
      description: null,
      createdAt: '2026-10-01T08:00:00',
    };
    fixture.detectChanges();
    http.expectOne('/api/equipments/1').flush({ equipment, requests: [], history: [] });
    fixture.detectChanges();
    http
      .expectOne('/api/equipments/1/reliability')
      .flush({ state: 'INSUFFICIENT_DATA', failureCount: 0 });
    http.expectOne('/api/equipments/1/preventive-plan').flush(null);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Chamados (0)');
    const panel = fixture.debugElement.query(By.directive(EquipmentMaintenanceComponent))
      .componentInstance as EquipmentMaintenanceComponent;
    panel.planForm.setValue({
      title: 'Lubrificação',
      description: 'Lubrificar componentes da máquina.',
      intervalDays: 7,
      nextDueDate: '2026-10-01',
      enabled: true,
    });
    panel.savePlan();
    http.expectOne('/api/equipments/1/preventive-plan').flush({
      id: 1,
      title: 'Lubrificação',
      description: 'Lubrificar componentes da máquina.',
      intervalDays: 7,
      nextDueDate: '2026-12-01',
      enabled: true,
    });
    http.expectOne('/api/equipments/1').flush({
      equipment,
      history: [],
      requests: [
        {
          id: 10,
          title: 'Lubrificação',
          status: 'OPEN',
          technicianName: null,
        },
      ],
    });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Chamados (1)');
    expect(fixture.nativeElement.textContent).toContain('#10 · Lubrificação');
    http.verify();
  });
});
