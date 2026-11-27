import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { AppUser, UserRole } from '../../core/models';
import { ApiErrorService } from '../../core/services/api-error.service';
import { UserService } from '../../core/services/user.service';

const PASSWORD_HINT = 'De 12 caracteres a 72 bytes.';

@Component({
  selector: 'app-users',
  imports: [ReactiveFormsModule],
  template: ` <section class="page-stack">
    <header class="page-hero">
      <div class="page-hero__copy">
        <span class="page-eyebrow">Administração</span>
        <h1 class="page-title">Usuários</h1>
        <p class="page-subtitle">Acessos de administradores, gestores e técnicos.</p>
      </div>
    </header>
    @if (error()) {
      <div class="alert alert--error" role="alert">{{ error() }}</div>
    }
    @if (success()) {
      <div class="alert alert--success" role="status">{{ success() }}</div>
    }
    <article class="panel">
      <h2 class="section-title">Novo acesso</h2>
      <form class="form-stack" [formGroup]="form" (ngSubmit)="save()">
        <div class="form-grid form-grid--two">
          <div class="field">
            <label for="user-name">Nome</label
            ><input id="user-name" formControlName="name" maxlength="120" autocomplete="name" />
          </div>
          <div class="field">
            <label for="user-email">E-mail</label
            ><input
              id="user-email"
              type="email"
              formControlName="email"
              maxlength="254"
              autocomplete="off"
            />
          </div>
        </div>
        <div class="form-grid form-grid--two">
          <div class="field">
            <label for="user-role">Perfil</label
            ><select id="user-role" formControlName="role">
              <option value="MANAGER">Gestor</option>
              <option value="TECHNICIAN">Técnico</option>
              <option value="ADMIN">Administrador</option>
            </select>
          </div>
          <div class="field">
            <label for="user-password">Senha provisória</label
            ><input
              id="user-password"
              type="password"
              formControlName="password"
              autocomplete="new-password"
              minlength="12"
              maxlength="72"
            /><small class="field-hint"
              >De 12 caracteres a 72 bytes. No primeiro acesso, o usuário deverá criar a própria
              senha.</small
            >
          </div>
        </div>
        @if (form.controls.role.value === 'TECHNICIAN') {
          <div class="form-grid form-grid--two">
            <div class="field">
              <label for="user-manager">Gestor vinculado</label
              ><select id="user-manager" formControlName="managerId">
                <option [ngValue]="0">Selecione</option>
                @for (manager of managers(); track manager.id) {
                  <option [ngValue]="manager.id">{{ manager.name }} · {{ manager.email }}</option>
                }</select
              ><small class="field-hint">
                @if (managers().length === 0) {
                  Cadastre primeiro um acesso com o perfil Gestor.
                } @else {
                  O técnico ficará sob a responsabilidade desse gestor.
                }
              </small>
            </div>
            <div class="field">
              <label for="user-specialty">Especialidade (opcional)</label
              ><input
                id="user-specialty"
                formControlName="specialty"
                maxlength="120"
                placeholder="Ex.: Mecânica hidráulica"
                autocomplete="off"
              />
            </div>
          </div>
        }
        <button class="button button--primary" type="submit" [disabled]="saving()">
          {{ saving() ? 'Cadastrando...' : 'Cadastrar acesso' }}
        </button>
      </form>
    </article>
    <article class="panel">
      <h2 class="section-title">Acessos cadastrados</h2>
      <div class="table-shell">
        <table class="data-table">
          <thead>
            <tr>
              <th>Nome</th>
              <th>E-mail</th>
              <th>Perfil</th>
              <th>Gestor vinculado</th>
              <th>Senha</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            @for (user of users(); track user.id) {
              <tr>
                <td>{{ user.name }}</td>
                <td>{{ user.email }}</td>
                <td>{{ roleLabel(user.role) }}</td>
                <td>{{ user.role === 'TECHNICIAN' ? user.managerName || 'Sem gestor' : '—' }}</td>
                <td>
                  <span
                    class="pill"
                    [class.pill--pending]="user.mustChangePassword"
                    [class.pill--ok]="!user.mustChangePassword"
                  >
                    {{ user.mustChangePassword ? 'Provisória' : 'Definida' }}
                  </span>
                </td>
                <td>
                  @if (resettingId() === user.id) {
                    <form
                      class="reset-form"
                      [formGroup]="resetForm"
                      (ngSubmit)="resetPassword(user)"
                    >
                      <label class="visually-hidden" [for]="'reset-' + user.id"
                        >Nova senha provisória de {{ user.name }}</label
                      >
                      <input
                        [id]="'reset-' + user.id"
                        type="password"
                        formControlName="password"
                        autocomplete="new-password"
                        maxlength="72"
                        placeholder="Senha provisória"
                      />
                      <button
                        class="button button--primary button--small"
                        type="submit"
                        [disabled]="saving()"
                      >
                        Salvar
                      </button>
                      <button
                        class="button button--ghost button--small"
                        type="button"
                        (click)="cancelReset()"
                      >
                        Cancelar
                      </button>
                    </form>
                  } @else {
                    <button
                      class="button button--ghost button--small"
                      type="button"
                      (click)="startReset(user)"
                    >
                      Definir senha provisória
                    </button>
                  }
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    </article>
  </section>`,
  styles: `
    .pill--ok {
      color: #166534;
      background: #dcfce7;
    }
    .pill--pending {
      color: #9a3412;
      background: #ffedd5;
    }
    .reset-form {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      align-items: center;
    }
    .reset-form input {
      min-width: 160px;
      min-height: 34px;
      padding: 6px 10px;
      border: 1px solid var(--border-strong);
      border-radius: 8px;
    }
  `,
})
export class UsersComponent implements OnInit {
  private readonly userService = inject(UserService);
  private readonly errors = inject(ApiErrorService);
  private readonly formBuilder = inject(FormBuilder);
  readonly users = signal<AppUser[]>([]);
  readonly managers = computed(() => this.users().filter((user) => user.role === 'MANAGER'));
  readonly error = signal<string | null>(null);
  readonly success = signal<string | null>(null);
  readonly saving = signal(false);
  readonly resettingId = signal<number | null>(null);
  readonly form = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(120)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(12), Validators.maxLength(72)]],
    role: ['MANAGER' as UserRole],
    managerId: [0],
    specialty: ['', [Validators.maxLength(120)]],
  });
  readonly resetForm = this.formBuilder.nonNullable.group({
    password: ['', [Validators.required, Validators.minLength(12), Validators.maxLength(72)]],
  });
  ngOnInit() {
    this.load();
  }
  private load() {
    this.userService.list().subscribe({
      next: (data) => this.users.set(data),
      error: (error: unknown) =>
        this.error.set(this.errors.toMessage(error, 'Não foi possível carregar os usuários.')),
    });
  }
  save() {
    this.clearFeedback();
    const value = this.form.getRawValue();
    const technician = value.role === 'TECHNICIAN';
    if (this.form.invalid || (technician && value.managerId < 1)) {
      this.form.markAllAsTouched();
      this.error.set(
        technician && value.managerId < 1
          ? 'Selecione o gestor vinculado ao técnico.'
          : 'Revise os campos. A senha provisória precisa ter ao menos 12 caracteres.',
      );
      return;
    }
    this.saving.set(true);
    this.userService
      .create({
        name: value.name.trim(),
        email: value.email.trim(),
        password: value.password,
        role: value.role,
        managerId: technician ? value.managerId : null,
        specialty: technician ? value.specialty.trim() || null : null,
      })
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: (created) => {
          this.form.reset();
          this.success.set(
            `Acesso de “${created.name}” cadastrado. Informe a senha provisória ao titular; ela deverá ser trocada no primeiro acesso.`,
          );
          this.load();
        },
        error: (error: unknown) =>
          this.error.set(this.errors.toMessage(error, 'Não foi possível cadastrar o acesso.')),
      });
  }
  startReset(user: AppUser) {
    this.clearFeedback();
    this.resetForm.reset();
    this.resettingId.set(user.id);
  }
  cancelReset() {
    this.resettingId.set(null);
    this.resetForm.reset();
  }
  resetPassword(user: AppUser) {
    this.clearFeedback();
    if (this.resetForm.invalid) {
      this.error.set(`A senha provisória precisa ter ao menos 12 caracteres. ${PASSWORD_HINT}`);
      return;
    }
    this.saving.set(true);
    this.userService
      .resetPassword(user.id, this.resetForm.getRawValue().password)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: (updated) => {
          this.cancelReset();
          this.users.update((users) =>
            users.map((item) => (item.id === updated.id ? updated : item)),
          );
          this.success.set(
            `Senha provisória de “${updated.name}” definida. Ela deverá ser trocada no próximo acesso.`,
          );
        },
        error: (error: unknown) =>
          this.error.set(this.errors.toMessage(error, 'Não foi possível definir a senha.')),
      });
  }
  roleLabel(role: UserRole) {
    return { ADMIN: 'Administrador', MANAGER: 'Gestor', TECHNICIAN: 'Técnico' }[role];
  }
  private clearFeedback() {
    this.error.set(null);
    this.success.set(null);
  }
}
