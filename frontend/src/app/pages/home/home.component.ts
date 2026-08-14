import { DatePipe, NgClass } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  inject,
  signal,
} from '@angular/core';
import { finalize } from 'rxjs';
import { HealthStatus } from '../../core/models';
import { HealthService } from '../../core/services/health.service';

@Component({
  selector: 'app-home',
  imports: [DatePipe, NgClass],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HomeComponent {
  private readonly destroyRef = inject(DestroyRef);
  private readonly healthService = inject(HealthService);

  protected readonly apiStatus = signal<HealthStatus | null>(null);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly isLoading = signal(false);
  protected readonly stackSummary = computed(() => this.apiStatus()?.stack ?? []);
  protected readonly backendBadgeClass = computed(() =>
    this.apiStatus()?.status === 'UP' ? 'is-success' : 'is-pending',
  );

  protected readonly kickoffCards = [
    {
      title: 'Parque fabril',
      description: 'Cadastro e consulta de equipamentos e dos setores onde estao instalados.',
    },
    {
      title: 'Chamados priorizados',
      description: 'Abertura e acompanhamento de chamados com nivel de urgencia.',
    },
    {
      title: 'Atribuicao segura',
      description:
        'Distribuicao para tecnicos com limite de dois chamados de alta urgencia abertos.',
    },
  ];

  protected readonly nextSteps = [
    'refinar os campos e estados dos cadastros e chamados',
    'implementar equipamentos e setores',
    'implementar abertura e priorizacao de chamados',
    'implementar atribuicao de tecnicos com a RN-001 no backend',
    'automatizar os gates tecnicos na pipeline de CI',
  ];

  constructor() {
    this.loadStatus();
  }

  protected reloadStatus(): void {
    this.loadStatus();
  }

  private loadStatus(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.healthService
      .getStatus()
      .pipe(
        finalize(() => this.isLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (status) => this.apiStatus.set(status),
        error: () => {
          this.apiStatus.set(null);
          this.errorMessage.set('Nao foi possivel consultar o backend agora.');
        },
      });
  }
}
