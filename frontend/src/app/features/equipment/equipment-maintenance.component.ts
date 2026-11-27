import { DatePipe, DecimalPipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, DestroyRef, Input, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import { ApiErrorService } from '../../core/services/api-error.service';
import { AuthService } from '../../core/services/auth.service';

interface Reliability {
  startDate: string | null;
  endDate: string | null;
  operatingHours: number | null;
  failureCount: number;
  mtbfHours: number | null;
  state: 'INSUFFICIENT_DATA' | 'NO_FAILURES' | 'CALCULATED';
}
interface PreventivePlan {
  id: number;
  title: string;
  description: string;
  intervalDays: number;
  nextDueDate: string;
  enabled: boolean;
}

@Component({
  selector: 'app-equipment-maintenance',
  imports: [ReactiveFormsModule, DatePipe, DecimalPipe],
  templateUrl: './equipment-maintenance.component.html',
})
export class EquipmentMaintenanceComponent implements OnInit {
  @Input({ required: true }) equipmentId!: number;
  readonly auth = inject(AuthService);
  private readonly http = inject(HttpClient);
  private readonly errors = inject(ApiErrorService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);
  readonly reliability = signal<Reliability | null>(null);
  readonly plan = signal<PreventivePlan | null>(null);
  readonly error = signal<string | null>(null);
  readonly success = signal<string | null>(null);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly reliabilityForm = this.fb.nonNullable.group({
    startDate: ['', Validators.required],
    endDate: ['', Validators.required],
    operatingHours: [0, [Validators.required, Validators.min(0.01)]],
  });
  readonly planForm = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(160)]],
    description: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(2000)]],
    intervalDays: [30, [Validators.required, Validators.min(1), Validators.max(3650)]],
    nextDueDate: ['', Validators.required],
    enabled: [true],
  });

  ngOnInit() {
    forkJoin({
      reliability: this.http.get<Reliability>(this.url('reliability')),
      plan: this.http.get<PreventivePlan | null>(this.url('preventive-plan')),
    })
      .pipe(
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: ({ reliability, plan }) => {
          this.reliability.set(reliability);
          if (reliability.startDate && reliability.endDate && reliability.operatingHours !== null) {
            this.reliabilityForm.setValue({
              startDate: reliability.startDate,
              endDate: reliability.endDate,
              operatingHours: reliability.operatingHours,
            });
          }
          this.plan.set(plan);
          if (plan) this.planForm.patchValue(plan);
        },
        error: (error: unknown) =>
          this.error.set(this.errors.toMessage(error, 'Não foi possível carregar a manutenção.')),
      });
  }

  saveReliability() {
    this.error.set(null);
    this.success.set(null);
    if (!this.auth.canManage() || this.saving()) return;
    if (this.reliabilityForm.invalid) {
      this.reliabilityForm.markAllAsTouched();
      this.error.set('Informe o período e as horas reais de operação.');
      return;
    }
    this.saving.set(true);
    this.http
      .put<Reliability>(this.url('reliability'), this.reliabilityForm.getRawValue())
      .pipe(
        finalize(() => this.saving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (data) => {
          this.reliability.set(data);
          this.success.set('Medição de operação salva.');
        },
        error: (error: unknown) =>
          this.error.set(this.errors.toMessage(error, 'Não foi possível salvar a medição.')),
      });
  }

  savePlan() {
    this.error.set(null);
    this.success.set(null);
    if (!this.auth.canManage() || this.saving()) return;
    if (this.planForm.invalid) {
      this.planForm.markAllAsTouched();
      this.error.set('Preencha o título, a descrição, a periodicidade e a próxima data.');
      return;
    }
    this.saving.set(true);
    this.http
      .put<PreventivePlan>(this.url('preventive-plan'), this.planForm.getRawValue())
      .pipe(
        finalize(() => this.saving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (data) => {
          this.plan.set(data);
          this.planForm.patchValue(data);
          this.success.set(
            'Plano salvo. Preventivas vencidas são geradas automaticamente na fila de chamados.',
          );
        },
        error: (error: unknown) =>
          this.error.set(this.errors.toMessage(error, 'Não foi possível salvar o plano.')),
      });
  }

  private url(endpoint: string) {
    return `/api/equipments/${this.equipmentId}/${endpoint}`;
  }
}
