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
import { Sector, SectorPayload } from '../../core/models';
import { ApiErrorService } from '../../core/services/api-error.service';
import { SectorService } from '../../core/services/sector.service';

@Component({
  selector: 'app-sector',
  imports: [DatePipe, ReactiveFormsModule],
  templateUrl: './sector.component.html',
  styleUrl: './sector.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SectorComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly formBuilder = inject(FormBuilder);
  private readonly sectorService = inject(SectorService);
  private readonly apiErrorService = inject(ApiErrorService);

  protected readonly form = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    description: ['', [Validators.maxLength(255)]],
  });

  protected readonly sectors = signal<Sector[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly isSaving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly successMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.loadSectors();
  }

  protected submit(): void {
    this.clearFeedback();

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMessage.set('Revise os campos destacados antes de cadastrar o setor.');
      return;
    }

    const rawValue = this.form.getRawValue();
    const payload: SectorPayload = {
      name: rawValue.name.trim(),
      description: rawValue.description.trim() || undefined,
    };

    this.isSaving.set(true);
    this.sectorService
      .create(payload)
      .pipe(
        finalize(() => this.isSaving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (created) => {
          this.sectors.update((sectors) => [created, ...sectors]);
          this.form.reset({ name: '', description: '' });
          this.successMessage.set(`Setor “${created.name}” cadastrado com sucesso.`);
        },
        error: (error: unknown) => {
          this.errorMessage.set(
            this.apiErrorService.toMessage(error, 'Não foi possível cadastrar o setor.'),
          );
        },
      });
  }

  protected get descriptionLength(): number {
    return this.form.controls.description.value.length;
  }

  private loadSectors(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.sectorService
      .list()
      .pipe(
        finalize(() => this.isLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (sectors) => this.sectors.set(sectors),
        error: (error: unknown) => {
          this.errorMessage.set(
            this.apiErrorService.toMessage(error, 'Não foi possível carregar os setores.'),
          );
        },
      });
  }

  private clearFeedback(): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);
  }
}
