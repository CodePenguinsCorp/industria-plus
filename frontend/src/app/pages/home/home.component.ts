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
import { RouterLink } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { Equipment, MaintenanceRequest, Sector, Technician, Urgency } from '../../core/models';
import { ApiErrorService } from '../../core/services/api-error.service';
import { EquipmentService } from '../../core/services/equipment.service';
import { MaintenanceRequestService } from '../../core/services/maintenance-request.service';
import { SectorService } from '../../core/services/sector.service';
import { TechnicianService } from '../../core/services/technician.service';

@Component({
  selector: 'app-home',
  imports: [DatePipe, RouterLink],
  templateUrl: './home.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HomeComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly equipmentService = inject(EquipmentService);
  private readonly sectorService = inject(SectorService);
  private readonly technicianService = inject(TechnicianService);
  private readonly requestService = inject(MaintenanceRequestService);
  private readonly apiErrorService = inject(ApiErrorService);

  protected readonly equipments = signal<Equipment[]>([]);
  protected readonly sectors = signal<Sector[]>([]);
  protected readonly technicians = signal<Technician[]>([]);
  protected readonly requests = signal<MaintenanceRequest[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly today = new Date();

  protected readonly openRequests = computed(() =>
    this.requests().filter((request) => request.status !== 'CLOSED'),
  );
  protected readonly highUrgencyRequests = computed(() =>
    this.openRequests().filter((request) => request.urgency === 'HIGH'),
  );
  protected readonly unassignedRequests = computed(() =>
    this.openRequests().filter((request) => request.technicianId === null),
  );
  protected readonly inProgressRequests = computed(() =>
    this.openRequests().filter((request) => request.status === 'IN_PROGRESS'),
  );
  protected readonly priorityRequests = computed(() => this.openRequests().slice(0, 5));
  protected readonly workloadTechnicians = computed(() =>
    [...this.technicians()]
      .sort((first, second) => second.highUrgencyOpenRequests - first.highUrgencyOpenRequests)
      .slice(0, 4),
  );

  ngOnInit(): void {
    this.loadDashboard();
  }

  protected urgencyLabel(urgency: Urgency): string {
    switch (urgency) {
      case 'HIGH':
        return 'Alta';
      case 'MEDIUM':
        return 'Média';
      default:
        return 'Baixa';
    }
  }

  protected capacityPercent(technician: Technician): number {
    return Math.min(100, (technician.highUrgencyOpenRequests / 2) * 100);
  }

  private loadDashboard(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    forkJoin({
      equipments: this.equipmentService.list(),
      sectors: this.sectorService.list(),
      technicians: this.technicianService.list(),
      requests: this.requestService.list(),
    })
      .pipe(
        finalize(() => this.isLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: ({ equipments, sectors, technicians, requests }) => {
          this.equipments.set(equipments);
          this.sectors.set(sectors);
          this.technicians.set(technicians);
          this.requests.set(requests);
        },
        error: (error: unknown) => {
          this.errorMessage.set(
            this.apiErrorService.toMessage(error, 'Não foi possível carregar a visão operacional.'),
          );
        },
      });
  }
}
