import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { EquipmentDetail } from '../../core/models';
import { HistoryService } from '../../core/services/history.service';
import { ApiErrorService } from '../../core/services/api-error.service';
import { HistoryListComponent } from '../history/history-list.component';
@Component({
  selector: 'app-equipment-detail',
  imports: [RouterLink, HistoryListComponent],
  template: ` <section class="page-stack">
    <header class="page-hero">
      <div class="page-hero__copy">
        <span class="page-eyebrow">Ativos industriais</span>
        <h1 class="page-title">{{ detail()?.equipment?.name || 'Detalhes do equipamento' }}</h1>
        <p class="page-subtitle">Chamados, intervenções e peças trocadas.</p>
      </div>
      <a class="button button--secondary button--small" routerLink="/equipamentos">Voltar</a>
    </header>
    @if (error()) {
      <div class="alert alert--error" role="alert">{{ error() }}</div>
    }
    @if (detail(); as data) {
      <article class="panel">
        <div class="section-head">
          <h2 class="section-title">Equipamento</h2>
          <span class="code-chip">{{ data.equipment.assetTag }}</span>
        </div>
        <p><strong>Setor:</strong> {{ data.equipment.sectorName }}</p>
        <p>{{ data.equipment.description || 'Sem descrição.' }}</p>
      </article>
      <article class="panel">
        <h2 class="section-title">Chamados ({{ data.requests.length }})</h2>
        @if (data.requests.length === 0) {
          <p class="field-hint">Nenhum chamado para este equipamento.</p>
        }
        <div class="table-shell">
          <table class="data-table">
            <thead>
              <tr>
                <th>Chamado</th>
                <th>Status</th>
                <th>Responsável</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              @for (request of data.requests; track request.id) {
                <tr>
                  <td>#{{ request.id }} · {{ request.title }}</td>
                  <td>{{ statusLabel(request.status) }}</td>
                  <td>{{ request.technicianName || 'Não atribuído' }}</td>
                  <td>
                    <a
                      class="button button--ghost button--small"
                      [routerLink]="['/chamados', request.id]"
                      >Ver atendimento</a
                    >
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      </article>
      <article class="panel">
        <h2 class="section-title">Intervenções ({{ interventions().length }})</h2>
        <app-history-list [entries]="interventions()" />
      </article>
      <article class="panel">
        <h2 class="section-title">Peças trocadas</h2>
        @if (replacements().length === 0) {
          <p class="field-hint">Nenhuma troca de peça registrada.</p>
        }
        <div class="table-shell">
          <table class="data-table">
            <thead>
              <tr>
                <th>Peça</th>
                <th>Quantidade</th>
                <th>Chamado</th>
              </tr>
            </thead>
            <tbody>
              @for (part of replacements(); track part.id) {
                <tr>
                  <td>{{ part.name }}</td>
                  <td>{{ part.quantity }}</td>
                  <td>
                    <a class="history-link" [routerLink]="['/chamados', part.requestId]"
                      >#{{ part.requestId }}</a
                    >
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      </article>
      <article class="panel">
        <h2 class="section-title">Histórico completo</h2>
        <app-history-list [entries]="data.history" />
      </article>
    } @else if (!error()) {
      <div class="loading-state" role="status">Carregando equipamento...</div>
    }
  </section>`,
})
export class EquipmentDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly service = inject(HistoryService);
  private readonly errors = inject(ApiErrorService);
  readonly detail = signal<EquipmentDetail | null>(null);
  readonly error = signal<string | null>(null);
  readonly interventions = computed(
    () => this.detail()?.history.filter((entry) => entry.eventType === 'INTERVENTION') ?? [],
  );
  readonly replacements = computed(() =>
    this.interventions().flatMap((entry) =>
      entry.parts.map((part) => ({ ...part, requestId: entry.requestId })),
    ),
  );
  ngOnInit() {
    this.route.paramMap.subscribe((params) => {
      this.detail.set(null);
      this.error.set(null);
      this.service.equipment(Number(params.get('id'))).subscribe({
        next: (data) => this.detail.set(data),
        error: (error: unknown) =>
          this.error.set(this.errors.toMessage(error, 'Não foi possível carregar o equipamento.')),
      });
    });
  }
  statusLabel(status: string) {
    return (
      (
        { OPEN: 'Aberto', IN_PROGRESS: 'Em andamento', CLOSED: 'Encerrado' } as Record<
          string,
          string
        >
      )[status] ?? status
    );
  }
}
