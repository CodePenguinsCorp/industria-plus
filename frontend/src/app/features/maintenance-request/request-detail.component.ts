import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import { HistoryEntry, MaintenanceRequest, ReplacedPart } from '../../core/models';
import { MaintenanceRequestService } from '../../core/services/maintenance-request.service';
import { HistoryService } from '../../core/services/history.service';
import { AuthService } from '../../core/services/auth.service';
import { ApiErrorService } from '../../core/services/api-error.service';
import { HistoryListComponent } from '../history/history-list.component';
@Component({
  selector: 'app-request-detail',
  imports: [RouterLink, ReactiveFormsModule, HistoryListComponent],
  template: ` <section class="page-stack">
    <header class="page-hero">
      <div class="page-hero__copy">
        <span class="page-eyebrow">Central de manutenção</span>
        <h1 class="page-title">Atendimento {{ request() ? '#' + request()!.id : '' }}</h1>
        <p class="page-subtitle">Histórico e descrição dos serviços realizados.</p>
      </div>
      <a class="button button--secondary button--small" routerLink="/chamados">Voltar</a>
    </header>
    @if (error()) {
      <div class="alert alert--error" role="alert">{{ error() }}</div>
    }
    @if (success()) {
      <div class="alert alert--success" role="status">{{ success() }}</div>
    }
    @if (request(); as item) {
      <article class="panel">
        <div class="section-head">
          <h2 class="section-title">{{ item.title }}</h2>
          <span class="pill">{{ statusLabel(item.status) }}</span>
        </div>
        <p>{{ item.description }}</p>
        <p><strong>Responsável:</strong> {{ item.technicianName || 'Não atribuído' }}</p>
        <a
          class="button button--ghost button--small"
          [routerLink]="['/equipamentos', item.equipmentId]"
          >{{ item.equipmentName }} · Ver equipamento</a
        >
      </article>
      @if (item.status !== 'CLOSED' && auth.canAttend(item.technicianId)) {
        <article class="panel">
          <h2 class="section-title">Registrar serviço realizado</h2>
          <form class="form-stack" [formGroup]="form" (ngSubmit)="save()">
            <div class="field">
              <label for="service-description">Descrição do serviço</label
              ><textarea
                id="service-description"
                formControlName="description"
                rows="4"
                maxlength="2000"
                placeholder="Descreva o diagnóstico e o que foi realizado."
                required
              ></textarea>
            </div>
            <div class="parts-row">
              <div class="field">
                <label for="part-name">Peça trocada (opcional)</label
                ><input id="part-name" formControlName="partName" maxlength="120" />
              </div>
              <div class="field">
                <label for="part-quantity">Quantidade</label
                ><input
                  id="part-quantity"
                  type="number"
                  formControlName="quantity"
                  min="1"
                  max="100000"
                />
              </div>
              <button
                class="button button--secondary button--small"
                type="button"
                (click)="addPart()"
              >
                Adicionar peça
              </button>
            </div>
            @if (parts().length > 0) {
              <ul>
                @for (part of parts(); track $index) {
                  <li>
                    {{ part.quantity }} × {{ part.name }}
                    <button
                      class="button button--ghost button--small"
                      type="button"
                      (click)="removePart($index)"
                    >
                      Remover
                    </button>
                  </li>
                }
              </ul>
            }
            <div class="action-row">
              <button class="button button--primary" type="submit" [disabled]="saving()">
                {{ saving() ? 'Salvando...' : 'Salvar atendimento' }}
              </button>
            </div>
          </form>
        </article>
      }
      <article class="panel">
        <h2 class="section-title">Histórico de atendimento</h2>
        <app-history-list [entries]="history()" />
      </article>
    } @else if (!error()) {
      <div class="loading-state" role="status">Carregando atendimento...</div>
    }
  </section>`,
})
export class RequestDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly requests = inject(MaintenanceRequestService);
  private readonly service = inject(HistoryService);
  private readonly errors = inject(ApiErrorService);
  readonly auth = inject(AuthService);
  readonly request = signal<MaintenanceRequest | null>(null);
  readonly history = signal<HistoryEntry[]>([]);
  readonly parts = signal<ReplacedPart[]>([]);
  readonly error = signal<string | null>(null);
  readonly success = signal<string | null>(null);
  readonly saving = signal(false);
  readonly form = inject(FormBuilder).nonNullable.group({
    description: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(2000)]],
    partName: [''],
    quantity: [1],
  });
  ngOnInit() {
    this.route.paramMap.subscribe((params) => {
      const id = Number(params.get('id'));
      this.request.set(null);
      this.history.set([]);
      this.parts.set([]);
      this.form.reset();
      this.error.set(null);
      forkJoin({ request: this.requests.detail(id), history: this.service.request(id) }).subscribe({
        next: (data) => {
          this.request.set(data.request);
          this.history.set(data.history);
        },
        error: (error: unknown) =>
          this.error.set(this.errors.toMessage(error, 'Não foi possível carregar o atendimento.')),
      });
    });
  }
  addPart(): boolean {
    const { partName, quantity } = this.form.getRawValue();
    if (
      !partName.trim() ||
      !Number.isInteger(quantity) ||
      quantity < 1 ||
      quantity > 100000 ||
      this.parts().length >= 50
    ) {
      this.error.set(
        'Informe uma peça e quantidade inteira positiva. Limite de 50 peças por atendimento.',
      );
      return false;
    }
    this.parts.update((parts) => [...parts, { name: partName.trim(), quantity }]);
    this.form.patchValue({ partName: '', quantity: 1 });
    this.error.set(null);
    return true;
  }
  removePart(index: number) {
    this.parts.update((parts) => parts.filter((_, i) => i !== index));
  }
  save() {
    const item = this.request();
    if (
      !item ||
      this.form.controls.description.invalid ||
      this.form.controls.description.value.trim().length < 3
    ) {
      this.error.set('Descreva o serviço realizado com ao menos 3 caracteres.');
      return;
    }
    if (this.form.controls.partName.value.trim() && !this.addPart()) return;
    this.saving.set(true);
    this.error.set(null);
    this.service
      .intervene(item.id, this.form.controls.description.value.trim(), this.parts())
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: (entry) => {
          this.history.update((history) => [entry, ...history]);
          this.parts.set([]);
          this.form.reset();
          this.success.set('Serviço e peças registrados no histórico.');
        },
        error: (error: unknown) =>
          this.error.set(this.errors.toMessage(error, 'Não foi possível registrar o serviço.')),
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
