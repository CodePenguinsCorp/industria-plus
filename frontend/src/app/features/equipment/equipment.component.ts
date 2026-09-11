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
import { RouterLink } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { Equipment, EquipmentPayload, Sector } from '../../core/models';
import { matchesSearch } from '../../core/search';
import { ApiErrorService } from '../../core/services/api-error.service';
import { EquipmentService } from '../../core/services/equipment.service';
import { SectorService } from '../../core/services/sector.service';

@Component({
  selector: 'app-equipment',
  imports: [DatePipe, ReactiveFormsModule, RouterLink],
  templateUrl: './equipment.component.html',
  styleUrl: './equipment.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EquipmentComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly formBuilder = inject(FormBuilder);
  private readonly equipmentService = inject(EquipmentService);
  private readonly sectorService = inject(SectorService);
  private readonly apiErrorService = inject(ApiErrorService);

  protected readonly form = this.formBuilder.nonNullable.group({
    assetTag: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(120)]],
    sectorId: [0, [Validators.required, Validators.min(1)]],
    description: ['', [Validators.maxLength(500)]],
  });
  protected readonly searchControl = this.formBuilder.nonNullable.control('');

  protected readonly equipments = signal<Equipment[]>([]);
  protected readonly sectors = signal<Sector[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly isSaving = signal(false);
  protected readonly deletingId = signal<number | null>(null);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly successMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.loadData();
  }

  protected submit(): void {
    this.clearFeedback();

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMessage.set('Revise os campos destacados antes de cadastrar o equipamento.');
      return;
    }

    const rawValue = this.form.getRawValue();
    const payload: EquipmentPayload = {
      assetTag: rawValue.assetTag.trim(),
      name: rawValue.name.trim(),
      sectorId: rawValue.sectorId,
      description: rawValue.description.trim() || undefined,
    };

    this.isSaving.set(true);
    this.equipmentService
      .create(payload)
      .pipe(
        finalize(() => this.isSaving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (created) => {
          this.equipments.update((equipments) => [created, ...equipments]);
          this.form.reset({ assetTag: '', name: '', sectorId: rawValue.sectorId, description: '' });
          this.successMessage.set(`Equipamento “${created.name}” cadastrado com sucesso.`);
        },
        error: (error: unknown) => {
          this.errorMessage.set(
            this.apiErrorService.toMessage(error, 'Não foi possível cadastrar o equipamento.'),
          );
        },
      });
  }

  protected get descriptionLength(): number {
    return this.form.controls.description.value.length;
  }

  protected get filteredEquipments(): Equipment[] {
    const query = this.searchControl.value.trim();
    if (!query) {
      return this.equipments();
    }

    return this.equipments().filter((equipment) =>
      matchesSearch(
        `${equipment.assetTag} ${equipment.name} ${equipment.sectorName} ${equipment.description ?? ''}`,
        query,
      ),
    );
  }

  protected deleteEquipment(equipment: Equipment): void {
    this.clearFeedback();
    if (!window.confirm(`Excluir o equipamento “${equipment.name}”?`)) {
      return;
    }

    this.deletingId.set(equipment.id);
    this.equipmentService
      .delete(equipment.id)
      .pipe(
        finalize(() => this.deletingId.set(null)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.equipments.update((equipments) =>
            equipments.filter((item) => item.id !== equipment.id),
          );
          this.successMessage.set(`Equipamento “${equipment.name}” excluído com sucesso.`);
        },
        error: (error: unknown) => {
          this.errorMessage.set(
            this.apiErrorService.toMessage(error, 'Não foi possível excluir o equipamento.'),
          );
        },
      });
  }

  private loadData(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    forkJoin({
      equipments: this.equipmentService.list(),
      sectors: this.sectorService.list(),
    })
      .pipe(
        finalize(() => this.isLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: ({ equipments, sectors }) => {
          this.equipments.set(equipments);
          this.sectors.set(sectors);

          if (sectors.length === 1) {
            this.form.controls.sectorId.setValue(sectors[0].id);
          }
        },
        error: (error: unknown) => {
          this.errorMessage.set(
            this.apiErrorService.toMessage(error, 'Não foi possível carregar os equipamentos.'),
          );
        },
      });
  }

  private clearFeedback(): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);
  }
}
