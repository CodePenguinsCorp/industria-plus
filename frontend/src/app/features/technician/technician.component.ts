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
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { ManagerOption, Technician } from '../../core/models';
import { matchesSearch } from '../../core/search';
import { ApiErrorService } from '../../core/services/api-error.service';
import { AuthService } from '../../core/services/auth.service';
import { TechnicianService } from '../../core/services/technician.service';

/**
 * Equipe técnica. Técnicos são cadastrados em Usuários; nesta tela o administrador altera o
 * gestor vinculado e o gestor apenas visualiza.
 */
@Component({
  selector: 'app-technician',
  imports: [DatePipe, ReactiveFormsModule, RouterLink],
  templateUrl: './technician.component.html',
  styleUrl: './technician.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TechnicianComponent implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly technicianService = inject(TechnicianService);
  private readonly apiErrorService = inject(ApiErrorService);

  protected readonly searchControl = inject(FormBuilder).nonNullable.control('');

  protected readonly technicians = signal<Technician[]>([]);
  protected readonly managers = signal<ManagerOption[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly savingManagerId = signal<number | null>(null);
  protected readonly deletingId = signal<number | null>(null);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly successMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.load();
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
      matchesSearch(
        `${technician.name} ${technician.email} ${technician.specialty ?? ''} ${technician.managerName ?? ''}`,
        query,
      ),
    );
  }

  protected changeManager(technician: Technician, event: Event): void {
    const select = event.target as HTMLSelectElement;
    const managerId = Number(select.value);
    if (!this.auth.isAdmin() || !managerId || managerId === technician.managerId) {
      return;
    }

    this.clearFeedback();
    this.savingManagerId.set(technician.id);
    this.technicianService
      .updateManager(technician.id, managerId)
      .pipe(
        finalize(() => this.savingManagerId.set(null)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (updated) => {
          this.technicians.update((technicians) =>
            technicians.map((item) => (item.id === updated.id ? updated : item)),
          );
          this.successMessage.set(
            `Técnico “${updated.name}” vinculado ao gestor “${updated.managerName}”.`,
          );
        },
        error: (error: unknown) => {
          select.value = technician.managerId ? String(technician.managerId) : '';
          this.errorMessage.set(
            this.apiErrorService.toMessage(error, 'Não foi possível alterar o gestor vinculado.'),
          );
        },
      });
  }

  protected deleteTechnician(technician: Technician): void {
    this.clearFeedback();
    if (!window.confirm(`Excluir o técnico “${technician.name}” e o acesso dele?`)) {
      return;
    }

    this.deletingId.set(technician.id);
    this.technicianService
      .delete(technician.id)
      .pipe(
        finalize(() => this.deletingId.set(null)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.technicians.update((technicians) =>
            technicians.filter((item) => item.id !== technician.id),
          );
          this.successMessage.set(`Técnico “${technician.name}” excluído com sucesso.`);
        },
        error: (error: unknown) => {
          this.errorMessage.set(
            this.apiErrorService.toMessage(error, 'Não foi possível excluir o técnico.'),
          );
        },
      });
  }

  private load(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    forkJoin({
      technicians: this.technicianService.list(),
      managers: this.technicianService.managers(),
    })
      .pipe(
        finalize(() => this.isLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: ({ technicians, managers }) => {
          this.technicians.set(technicians);
          this.managers.set(managers);
        },
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
