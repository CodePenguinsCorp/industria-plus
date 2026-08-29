import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { Equipment, MaintenanceRequest, Sector, Technician } from '../../core/models';
import { EquipmentService } from '../../core/services/equipment.service';
import { MaintenanceRequestService } from '../../core/services/maintenance-request.service';
import { SectorService } from '../../core/services/sector.service';
import { TechnicianService } from '../../core/services/technician.service';
import { MaintenanceRequestComponent } from './maintenance-request.component';

describe('MaintenanceRequestComponent', () => {
  const sector: Sector = {
    id: 1,
    name: 'Usinagem',
    description: null,
    createdAt: '2026-08-28T08:00:00',
  };

  const equipment: Equipment = {
    id: 2,
    assetTag: 'PR-042',
    name: 'Prensa hidráulica',
    description: null,
    sectorId: 1,
    sectorName: 'Usinagem',
    createdAt: '2026-08-28T08:10:00',
  };

  const technician: Technician = {
    id: 3,
    name: 'Ana Martins',
    email: 'ana@industria.com.br',
    specialty: 'Hidráulica',
    highUrgencyOpenRequests: 2,
    createdAt: '2026-08-28T08:20:00',
  };

  const request: MaintenanceRequest = {
    id: 4,
    title: 'Prensa sem pressão',
    description: 'A prensa perdeu pressão durante o segundo turno.',
    equipmentId: 2,
    equipmentName: 'Prensa hidráulica',
    sectorId: 1,
    sectorName: 'Usinagem',
    type: 'CORRECTIVE',
    urgency: 'HIGH',
    status: 'OPEN',
    technicianId: null,
    technicianName: null,
    createdAt: '2026-08-28T09:00:00',
    updatedAt: '2026-08-28T09:00:00',
  };

  const requestService = {
    list: vi.fn(() => of([request])),
    create: vi.fn(() => of(request)),
    assign: vi.fn(() => of({ ...request, technicianId: 3, technicianName: 'Ana Martins' })),
    updateStatus: vi.fn(() => of(request)),
  };

  const equipmentService = { list: vi.fn(() => of([equipment])) };
  const sectorService = { list: vi.fn(() => of([sector])) };
  const technicianService = { list: vi.fn(() => of([technician])) };

  beforeEach(async () => {
    vi.clearAllMocks();
    requestService.list.mockReturnValue(of([request]));
    requestService.create.mockReturnValue(of(request));
    requestService.assign.mockReturnValue(
      of({ ...request, technicianId: 3, technicianName: 'Ana Martins' }),
    );
    equipmentService.list.mockReturnValue(of([equipment]));
    sectorService.list.mockReturnValue(of([sector]));
    technicianService.list.mockReturnValue(of([technician]));

    await TestBed.configureTestingModule({
      imports: [MaintenanceRequestComponent],
      providers: [
        { provide: MaintenanceRequestService, useValue: requestService },
        { provide: EquipmentService, useValue: equipmentService },
        { provide: SectorService, useValue: sectorService },
        { provide: TechnicianService, useValue: technicianService },
      ],
    }).compileComponents();
  });

  it('validates the critical opening fields before calling the API', () => {
    const fixture = TestBed.createComponent(MaintenanceRequestComponent);
    const component = fixture.componentInstance as any;
    fixture.detectChanges();
    component.createForm.patchValue({ title: 'No', description: 'Curta' });

    component.submitRequest();

    expect(requestService.create).not.toHaveBeenCalled();
    expect(component.createForm.controls.title.touched).toBe(true);
    expect(component.createForm.controls.description.touched).toBe(true);
  });

  it('sends a normalized payload when opening a valid request', () => {
    const fixture = TestBed.createComponent(MaintenanceRequestComponent);
    const component = fixture.componentInstance as any;
    fixture.detectChanges();
    component.createForm.setValue({
      title: '  Prensa sem pressão  ',
      description: '  A prensa perdeu pressão durante o segundo turno.  ',
      sectorId: 1,
      equipmentId: 2,
      type: 'CORRECTIVE',
      urgency: 'HIGH',
    });

    component.submitRequest();

    expect(requestService.create).toHaveBeenCalledWith({
      title: 'Prensa sem pressão',
      description: 'A prensa perdeu pressão durante o segundo turno.',
      sectorId: 1,
      equipmentId: 2,
      type: 'CORRECTIVE',
      urgency: 'HIGH',
    });
  });

  it('shows the RN-001 message returned by a conflicting assignment', () => {
    requestService.assign.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 409,
            error: {
              code: 'HIGH_URGENCY_LIMIT',
              message: 'O técnico já possui dois chamados de urgência Alta abertos.',
            },
          }),
      ),
    );
    const fixture = TestBed.createComponent(MaintenanceRequestComponent);
    const component = fixture.componentInstance as any;
    fixture.detectChanges();
    component.openAssignment(request);
    component.assignmentForm.controls.technicianId.setValue(3);

    component.assignTechnician();

    expect(component.actionErrorMessage()).toBe(
      'O técnico já possui dois chamados de urgência Alta abertos.',
    );
  });

  it('closes the assignment editor before changing the request status', () => {
    const fixture = TestBed.createComponent(MaintenanceRequestComponent);
    const component = fixture.componentInstance as any;
    fixture.detectChanges();
    component.openAssignment(request);

    component.changeStatus(request, 'CLOSED');

    expect(component.assignmentRequestId()).toBeNull();
    expect(requestService.updateStatus).toHaveBeenCalledWith(request.id, 'CLOSED');
  });
});
