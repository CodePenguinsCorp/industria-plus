import { DatePipe } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { Technician, TechnicianPayload } from '../../core/models';
import { matchesSearch } from '../../core/search';
import { ApiErrorService } from '../../core/services/api-error.service';
import { TechnicianService } from '../../core/services/technician.service';

@Component({
  selector: 'app-technician',
  imports: [DatePipe, ReactiveFormsModule],
  templateUrl: './technician.component.html',
  styleUrl: './technician.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TechnicianComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly formBuilder = inject(FormBuilder);
  private readonly technicianService = inject(TechnicianService);
  private readonly apiErrorService = inject(ApiErrorService);

  protected readonly form = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(120)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(254)]],
    specialty: ['', [Validators.maxLength(120)]],
  });
  protected readonly searchControl = this.formBuilder.nonNullable.control('');

  protected readonly technicians = signal<Technician[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly isSaving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly successMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.loadTechnicians();
  }

  protected submit(): void {
    this.clearFeedback();

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMessage.set('Revise os campos destacados antes de cadastrar o técnico.');
      return;
    }

    const rawValue = this.form.getRawValue();
    const payload: TechnicianPayload = {
      name: rawValue.name.trim(),
      email: rawValue.email.trim().toLocaleLowerCase('pt-BR'),
      specialty: rawValue.specialty.trim() || undefined,
    };

    this.isSaving.set(true);
    this.technicianService
      .create(payload)
      .pipe(
        finalize(() => this.isSaving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (created) => {
          this.technicians.update((technicians) => [created, ...technicians]);
          this.form.reset({ name: '', email: '', specialty: '' });
          this.successMessage.set(`Técnico “${created.name}” cadastrado com sucesso.`);
        },
        error: (error: unknown) => {
          this.errorMessage.set(
            this.apiErrorService.toMessage(error, 'Não foi possível cadastrar o técnico.'),
          );
        },
      });
  }

  protected capacityClass(technician: Technician): string {
    if (technician.highUrgencyOpenRequests >= 2) {
      return 'capacity-pill--full';
    }

    return technician.highUrgencyOpenRequests === 1
      ? 'capacity-pill--attention'
      : 'capacity-pill--available';
  }

  protected get filteredTechnicians(): Technician[] {
    const query = this.searchControl.value.trim();
    if (!query) {
      return this.technicians();
    }

    return this.technicians().filter((technician) =>
      matchesSearch(`${technician.name} ${technician.email} ${technician.specialty ?? ''}`, query),
    );
  }

  private loadTechnicians(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.technicianService
      .list()
      .pipe(
        finalize(() => this.isLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (technicians) => this.technicians.set(technicians),
        error: (error: unknown) => {
          this.errorMessage.set(
            this.apiErrorService.toMessage(error, 'Não foi possível carregar os técnicos.'),
          );
        },
      });
  }

  private clearFeedback(): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);
  }
}
