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
import {
  Equipment,
  HealthStatus,
  MaintenanceRequest,
  Sector,
  Technician,
  Urgency,
} from '../../core/models';
import { ApiErrorService } from '../../core/services/api-error.service';
import { EquipmentService } from '../../core/services/equipment.service';
import { HealthService } from '../../core/services/health.service';
import { MaintenanceRequestService } from '../../core/services/maintenance-request.service';
import { SectorService } from '../../core/services/sector.service';
import { TechnicianService } from '../../core/services/technician.service';

@Component({
  selector: 'app-home',
  imports: [DatePipe, RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HomeComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly equipmentService = inject(EquipmentService);
  private readonly sectorService = inject(SectorService);
  private readonly technicianService = inject(TechnicianService);
  private readonly requestService = inject(MaintenanceRequestService);
  private readonly healthService = inject(HealthService);
  private readonly apiErrorService = inject(ApiErrorService);

  protected readonly equipments = signal<Equipment[]>([]);
  protected readonly sectors = signal<Sector[]>([]);
  protected readonly technicians = signal<Technician[]>([]);
  protected readonly requests = signal<MaintenanceRequest[]>([]);
  protected readonly healthStatus = signal<HealthStatus | null>(null);
  protected readonly isLoading = signal(true);
  protected readonly isHealthLoading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly healthError = signal<string | null>(null);

  protected readonly openRequests = computed(() =>
    this.requests().filter((request) => request.status !== 'CLOSED'),
  );
  protected readonly highUrgencyRequests = computed(() =>
    this.openRequests().filter((request) => request.urgency === 'HIGH'),
  );
  protected readonly unassignedRequests = computed(() =>
    this.openRequests().filter((request) => request.technicianId === null),
  );
  protected readonly priorityRequests = computed(() => this.openRequests().slice(0, 4));

  protected readonly quickActions = [
    {
      title: 'Abrir chamado',
      description: 'Registre uma manutenção preventiva ou corretiva.',
      route: '/chamados',
      tone: 'rust',
    },
    {
      title: 'Cadastrar equipamento',
      description: 'Inclua um novo ativo no parque fabril.',
      route: '/equipamentos',
      tone: 'mint',
    },
    {
      title: 'Organizar setores',
      description: 'Estruture as áreas operacionais da fábrica.',
      route: '/setores',
      tone: 'dark',
    },
    {
      title: 'Gerenciar técnicos',
      description: 'Consulte a equipe e a carga de urgência Alta.',
      route: '/tecnicos',
      tone: 'copper',
    },
  ];

  ngOnInit(): void {
    this.loadDashboard();
    this.loadHealthStatus();
  }

  protected reloadHealthStatus(): void {
    this.loadHealthStatus();
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

  private loadHealthStatus(): void {
    this.isHealthLoading.set(true);
    this.healthError.set(null);

    this.healthService
      .getStatus()
      .pipe(
        finalize(() => this.isHealthLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (status) => this.healthStatus.set(status),
        error: () => {
          this.healthStatus.set(null);
          this.healthError.set('API indisponível para verificação.');
        },
      });
  }
}
