import { DatePipe } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import {
  Equipment,
  MaintenanceRequest,
  MaintenanceRequestFilters,
  MaintenanceRequestPayload,
  MaintenanceRequestStatus,
  MaintenanceType,
  Sector,
  Technician,
  Urgency,
} from '../../core/models';
import { ApiErrorService } from '../../core/services/api-error.service';
import { EquipmentService } from '../../core/services/equipment.service';
import { MaintenanceRequestService } from '../../core/services/maintenance-request.service';
import { SectorService } from '../../core/services/sector.service';
import { TechnicianService } from '../../core/services/technician.service';

type StatusFilter = 'ALL' | MaintenanceRequestStatus;
type UrgencyFilter = 'ALL' | Urgency;

@Component({
  selector: 'app-maintenance-request',
  imports: [DatePipe, ReactiveFormsModule, RouterLink],
  templateUrl: './maintenance-request.component.html',
  styleUrl: './maintenance-request.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MaintenanceRequestComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly formBuilder = inject(FormBuilder);
  private readonly requestService = inject(MaintenanceRequestService);
  private readonly equipmentService = inject(EquipmentService);
  private readonly sectorService = inject(SectorService);
  private readonly technicianService = inject(TechnicianService);
  private readonly apiErrorService = inject(ApiErrorService);

  protected readonly urgencyOptions: Array<{ value: Urgency; label: string }> = [
    { value: 'LOW', label: 'Baixa' },
    { value: 'MEDIUM', label: 'Média' },
    { value: 'HIGH', label: 'Alta' },
  ];

  protected readonly typeOptions: Array<{ value: MaintenanceType; label: string }> = [
    { value: 'CORRECTIVE', label: 'Corretiva' },
    { value: 'PREVENTIVE', label: 'Preventiva' },
  ];

  protected readonly statusFilterOptions: Array<{ value: StatusFilter; label: string }> = [
    { value: 'ALL', label: 'Todos os status' },
    { value: 'OPEN', label: 'Aberto' },
    { value: 'IN_PROGRESS', label: 'Em andamento' },
    { value: 'CLOSED', label: 'Encerrado' },
  ];

  protected readonly urgencyFilterOptions: Array<{ value: UrgencyFilter; label: string }> = [
    { value: 'ALL', label: 'Todas as urgências' },
    ...this.urgencyOptions,
  ];

  protected readonly createForm = this.formBuilder.nonNullable.group({
    title: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(160)]],
    description: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(2000)]],
    sectorId: [0, [Validators.required, Validators.min(1)]],
    equipmentId: [0, [Validators.required, Validators.min(1)]],
    type: ['CORRECTIVE' as MaintenanceType, [Validators.required]],
    urgency: ['MEDIUM' as Urgency, [Validators.required]],
  });

  protected readonly filterForm = this.formBuilder.nonNullable.group({
    status: ['ALL' as StatusFilter],
    urgency: ['ALL' as UrgencyFilter],
  });

  protected readonly assignmentForm = this.formBuilder.nonNullable.group({
    technicianId: [0, [Validators.required, Validators.min(1)]],
  });

  protected readonly requests = signal<MaintenanceRequest[]>([]);
  protected readonly equipments = signal<Equipment[]>([]);
  protected readonly sectors = signal<Sector[]>([]);
  protected readonly technicians = signal<Technician[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly isListLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly actionRequestId = signal<number | null>(null);
  protected readonly assignmentRequestId = signal<number | null>(null);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly actionErrorMessage = signal<string | null>(null);
  protected readonly successMessage = signal<string | null>(null);

  protected readonly openCount = computed(
    () => this.requests().filter((request) => request.status !== 'CLOSED').length,
  );
  protected readonly highOpenCount = computed(
    () =>
      this.requests().filter((request) => request.urgency === 'HIGH' && request.status !== 'CLOSED')
        .length,
  );
  protected readonly unassignedCount = computed(
    () =>
      this.requests().filter(
        (request) => request.status !== 'CLOSED' && request.technicianId === null,
      ).length,
  );

  ngOnInit(): void {
    this.observeEquipmentSelection();
    this.loadInitialData();
  }

  protected get availableEquipments(): Equipment[] {
    const sectorId = this.createForm.controls.sectorId.value;
    return this.equipments().filter((equipment) => equipment.sectorId === sectorId);
  }

  protected get selectedAssignmentRequest(): MaintenanceRequest | null {
    const requestId = this.assignmentRequestId();
    return this.requests().find((request) => request.id === requestId) ?? null;
  }

  protected get selectedTechnician(): Technician | null {
    const technicianId = this.assignmentForm.controls.technicianId.value;
    return this.technicians().find((technician) => technician.id === technicianId) ?? null;
  }

  protected get assignmentCapacityWarning(): string | null {
    const request = this.selectedAssignmentRequest;
    const technician = this.selectedTechnician;

    if (
      !request ||
      !technician ||
      request.urgency !== 'HIGH' ||
      request.technicianId === technician.id ||
      technician.highUrgencyOpenRequests < 2
    ) {
      return null;
    }

    return `${technician.name} já aparece com ${technician.highUrgencyOpenRequests}/2 chamados de urgência Alta abertos. A API fará a validação definitiva.`;
  }

  protected get descriptionLength(): number {
    return this.createForm.controls.description.value.length;
  }

  protected submitRequest(): void {
    this.clearFeedback();

    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      this.errorMessage.set('Revise os campos destacados antes de abrir o chamado.');
      return;
    }

    const rawValue = this.createForm.getRawValue();
    const payload: MaintenanceRequestPayload = {
      title: rawValue.title.trim(),
      description: rawValue.description.trim(),
      sectorId: rawValue.sectorId,
      equipmentId: rawValue.equipmentId,
      type: rawValue.type,
      urgency: rawValue.urgency,
    };

    this.isSaving.set(true);
    this.requestService
      .create(payload)
      .pipe(
        finalize(() => this.isSaving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (created) => {
          this.createForm.reset({
            title: '',
            description: '',
            sectorId: 0,
            equipmentId: 0,
            type: 'CORRECTIVE',
            urgency: 'MEDIUM',
          });
          this.successMessage.set(`Chamado #${created.id} aberto com sucesso.`);
          this.loadRequests();
        },
        error: (error: unknown) => {
          this.errorMessage.set(
            this.apiErrorService.toMessage(error, 'Não foi possível abrir o chamado.'),
          );
        },
      });
  }

  protected applyFilters(): void {
    this.closeAssignment();
    this.loadRequests();
  }

  protected clearFilters(): void {
    this.filterForm.setValue({ status: 'ALL', urgency: 'ALL' });
    this.applyFilters();
  }

  protected openAssignment(request: MaintenanceRequest): void {
    this.actionErrorMessage.set(null);
    this.assignmentRequestId.set(request.id);
    this.assignmentForm.reset({ technicianId: request.technicianId ?? 0 });
  }

  protected closeAssignment(): void {
    if (this.actionRequestId() !== null) {
      return;
    }

    this.assignmentRequestId.set(null);
    this.assignmentForm.reset({ technicianId: 0 });
    this.actionErrorMessage.set(null);
  }

  protected assignTechnician(): void {
    const request = this.selectedAssignmentRequest;
    this.actionErrorMessage.set(null);

    if (!request || this.assignmentForm.invalid) {
      this.assignmentForm.markAllAsTouched();
      this.actionErrorMessage.set('Selecione um técnico para continuar.');
      return;
    }

    const technicianId = this.assignmentForm.controls.technicianId.value;
    this.actionRequestId.set(request.id);
    this.requestService
      .assign(request.id, technicianId)
      .pipe(
        finalize(() => this.actionRequestId.set(null)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (updated) => {
          this.assignmentRequestId.set(null);
          this.assignmentForm.reset({ technicianId: 0 });
          this.successMessage.set(`Chamado #${updated.id} atribuído com sucesso.`);
          this.loadRequests();
          this.refreshTechnicians();
        },
        error: (error: unknown) => {
          this.actionErrorMessage.set(
            this.apiErrorService.toMessage(
              error,
              'Não foi possível atribuir o chamado ao técnico.',
            ),
          );
        },
      });
  }

  protected changeStatus(request: MaintenanceRequest, status: MaintenanceRequestStatus): void {
    this.clearFeedback();
    this.closeAssignment();
    this.actionRequestId.set(request.id);

    this.requestService
      .updateStatus(request.id, status)
      .pipe(
        finalize(() => this.actionRequestId.set(null)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (updated) => {
          this.successMessage.set(
            status === 'IN_PROGRESS'
              ? `Chamado #${updated.id} iniciado.`
              : `Chamado #${updated.id} encerrado.`,
          );
          this.loadRequests();
          this.refreshTechnicians();
        },
        error: (error: unknown) => {
          this.errorMessage.set(
            this.apiErrorService.toMessage(error, 'Não foi possível alterar o status do chamado.'),
          );
        },
      });
  }

  protected urgencyLabel(urgency: Urgency): string {
    return this.urgencyOptions.find((option) => option.value === urgency)?.label ?? urgency;
  }

  protected statusLabel(status: MaintenanceRequestStatus): string {
    return this.statusFilterOptions.find((option) => option.value === status)?.label ?? status;
  }

  protected typeLabel(type: MaintenanceType): string {
    return this.typeOptions.find((option) => option.value === type)?.label ?? type;
  }

  protected technicianOptionLabel(technician: Technician): string {
    return `${technician.name} — Alta ${technician.highUrgencyOpenRequests}/2`;
  }

  private observeEquipmentSelection(): void {
    this.createForm.controls.sectorId.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((sectorId) => {
        const equipmentId = this.createForm.controls.equipmentId.value;
        const stillAvailable = this.equipments().some(
          (equipment) => equipment.id === equipmentId && equipment.sectorId === sectorId,
        );

        if (!stillAvailable && equipmentId !== 0) {
          this.createForm.controls.equipmentId.setValue(0, { emitEvent: false });
        }
      });

    this.createForm.controls.equipmentId.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((equipmentId) => {
        const equipment = this.equipments().find((item) => item.id === equipmentId);
        if (equipment && this.createForm.controls.sectorId.value !== equipment.sectorId) {
          this.createForm.controls.sectorId.setValue(equipment.sectorId, { emitEvent: false });
        }
      });
  }

  private loadInitialData(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    forkJoin({
      requests: this.requestService.list(),
      equipments: this.equipmentService.list(),
      sectors: this.sectorService.list(),
      technicians: this.technicianService.list(),
    })
      .pipe(
        finalize(() => this.isLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: ({ requests, equipments, sectors, technicians }) => {
          this.requests.set(requests);
          this.equipments.set(equipments);
          this.sectors.set(sectors);
          this.technicians.set(technicians);
        },
        error: (error: unknown) => {
          this.errorMessage.set(
            this.apiErrorService.toMessage(
              error,
              'Não foi possível carregar os dados dos chamados.',
            ),
          );
        },
      });
  }

  private loadRequests(): void {
    this.isListLoading.set(true);
    this.errorMessage.set(null);

    this.requestService
      .list(this.currentFilters())
      .pipe(
        finalize(() => this.isListLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (requests) => this.requests.set(requests),
        error: (error: unknown) => {
          this.errorMessage.set(
            this.apiErrorService.toMessage(error, 'Não foi possível carregar os chamados.'),
          );
        },
      });
  }

  private refreshTechnicians(): void {
    this.technicianService
      .list()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: (technicians) => this.technicians.set(technicians) });
  }

  private currentFilters(): MaintenanceRequestFilters {
    const { status, urgency } = this.filterForm.getRawValue();
    return {
      status: status === 'ALL' ? undefined : status,
      urgency: urgency === 'ALL' ? undefined : urgency,
    };
  }

  private clearFeedback(): void {
    this.errorMessage.set(null);
    this.actionErrorMessage.set(null);
    this.successMessage.set(null);
  }
}
