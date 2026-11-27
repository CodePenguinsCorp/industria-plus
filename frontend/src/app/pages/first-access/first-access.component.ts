import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { ApiErrorService } from '../../core/services/api-error.service';
import { AuthService } from '../../core/services/auth.service';

const MAX_PASSWORD_BYTES = 72;

function passwordRules(group: AbstractControl): ValidationErrors | null {
  const current = group.get('currentPassword')?.value as string;
  const next = group.get('newPassword')?.value as string;
  const confirmation = group.get('confirmPassword')?.value as string;
  const errors: ValidationErrors = {};
  if (next && new TextEncoder().encode(next).length > MAX_PASSWORD_BYTES) errors['tooLong'] = true;
  if (next && current && next === current) errors['sameAsCurrent'] = true;
  if (confirmation && next !== confirmation) errors['mismatch'] = true;
  return Object.keys(errors).length ? errors : null;
}

/** Troca obrigatória da senha provisória definida pelo administrador. */
@Component({
  selector: 'app-first-access',
  imports: [ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: ` <main class="login-page">
    <section class="page-stack login-card">
      <header class="page-hero">
        <div class="page-hero__copy">
          <span class="page-eyebrow">Primeiro acesso</span>
          <h1 class="page-title">Crie sua senha</h1>
          <p class="page-subtitle">
            Olá, {{ auth.user()?.name }}. Sua senha atual é provisória; defina uma senha pessoal
            para continuar.
          </p>
        </div>
      </header>
      <article class="panel">
        <h2 class="section-title">Nova senha</h2>
        @if (error()) {
          <div class="alert alert--error" role="alert">{{ error() }}</div>
        }
        <form class="form-stack" [formGroup]="form" (ngSubmit)="submit()" novalidate>
          <div class="field">
            <label for="current-password">Senha provisória</label
            ><input
              id="current-password"
              type="password"
              formControlName="currentPassword"
              autocomplete="current-password"
            />
          </div>
          <div class="field">
            <label for="new-password">Nova senha</label
            ><input
              id="new-password"
              type="password"
              formControlName="newPassword"
              autocomplete="new-password"
              maxlength="72"
            /><small class="field-hint"
              >De 12 caracteres a 72 bytes, diferente da senha provisória.</small
            >
          </div>
          <div class="field">
            <label for="confirm-password">Confirme a nova senha</label
            ><input
              id="confirm-password"
              type="password"
              formControlName="confirmPassword"
              autocomplete="new-password"
              maxlength="72"
            />
            @if (form.hasError('mismatch') && form.controls.confirmPassword.touched) {
              <small class="field-error">As senhas não conferem.</small>
            }
          </div>
          <button class="button button--primary" type="submit" [disabled]="saving()">
            {{ saving() ? 'Salvando...' : 'Salvar e continuar' }}
          </button>
          <button class="button button--ghost" type="button" (click)="auth.logout()">Sair</button>
        </form>
      </article>
    </section>
  </main>`,
  styles: `
    .login-page {
      min-height: 100dvh;
      display: grid;
      place-items: center;
      padding: 24px;
    }
    .login-card {
      width: 100%;
      max-width: 540px;
    }
  `,
})
export class FirstAccessComponent {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly errors = inject(ApiErrorService);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly form = inject(FormBuilder).nonNullable.group(
    {
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(12)]],
      confirmPassword: ['', Validators.required],
    },
    { validators: passwordRules },
  );

  submit(): void {
    this.error.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error.set(this.validationMessage());
      return;
    }
    const { currentPassword, newPassword } = this.form.getRawValue();
    this.saving.set(true);
    this.auth
      .changePassword(currentPassword, newPassword)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.form.reset();
          void this.router.navigateByUrl('/');
        },
        error: (error: unknown) =>
          this.error.set(this.errors.toMessage(error, 'Não foi possível salvar a nova senha.')),
      });
  }

  private validationMessage(): string {
    const controls = this.form.controls;
    if (controls.currentPassword.invalid) return 'Informe a senha provisória.';
    if (controls.newPassword.invalid) return 'A nova senha precisa ter ao menos 12 caracteres.';
    if (this.form.hasError('tooLong')) return 'A nova senha deve ter no máximo 72 bytes.';
    if (this.form.hasError('sameAsCurrent'))
      return 'A nova senha deve ser diferente da senha provisória.';
    if (this.form.hasError('mismatch') || controls.confirmPassword.invalid)
      return 'A confirmação não confere com a nova senha.';
    return 'Revise os campos.';
  }
}
