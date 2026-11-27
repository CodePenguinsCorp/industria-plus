import { DatePipe, DecimalPipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, DestroyRef, Input, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
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
  readonly error = signal<string | null>(null);
  readonly success = signal<string | null>(null);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly reliabilityForm = this.fb.nonNullable.group({
    startDate: ['', Validators.required],
    endDate: ['', Validators.required],
    operatingHours: [0, [Validators.required, Validators.min(0.01)]],
  });

  ngOnInit() {
    this.http
      .get<Reliability>(this.url('reliability'))
      .pipe(
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (reliability) => {
          this.reliability.set(reliability);
          if (reliability.startDate && reliability.endDate && reliability.operatingHours !== null) {
            this.reliabilityForm.setValue({
              startDate: reliability.startDate,
              endDate: reliability.endDate,
              operatingHours: reliability.operatingHours,
            });
          }
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

  private url(endpoint: string) {
    return `/api/equipments/${this.equipmentId}/${endpoint}`;
  }
}
