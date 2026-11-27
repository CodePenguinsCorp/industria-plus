import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { ApiErrorService } from '../../core/services/api-error.service';
@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  template: ` <main class="login-page">
    <section class="page-stack login-card">
      <header class="page-hero">
        <div class="page-hero__copy">
          <span class="page-eyebrow">Central de manutenção</span>
          <h1 class="page-title">Industria Plus</h1>
          <p class="page-subtitle">Entre para acompanhar os equipamentos e atendimentos.</p>
        </div>
      </header>
      <article class="panel">
        <h2 class="section-title">Acessar sistema</h2>
        @if (error()) {
          <div class="alert alert--error" role="alert">{{ error() }}</div>
        }
        <form class="form-stack" [formGroup]="form" (ngSubmit)="submit()">
          <div class="field">
            <label for="login-email">E-mail</label
            ><input
              id="login-email"
              type="email"
              formControlName="email"
              autocomplete="username"
              required
            />
          </div>
          <div class="field">
            <label for="login-password">Senha</label
            ><input
              id="login-password"
              type="password"
              formControlName="password"
              autocomplete="current-password"
              required
            />
          </div>
          <button class="button button--primary" type="submit" [disabled]="loading()">
            {{ loading() ? 'Entrando...' : 'Entrar' }}
          </button>
        </form>
        <p class="field-hint">
          Primeiro acesso: entre com a senha provisória recebida do administrador. Em seguida, o
          sistema pedirá que você crie sua própria senha.
        </p>
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
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly errors = inject(ApiErrorService);
  readonly form = inject(FormBuilder).nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error.set('Informe seu e-mail e sua senha.');
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    const { email, password } = this.form.getRawValue();
    this.auth
      .login(email, password)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (user) => {
          this.form.controls.password.reset();
          void this.router.navigateByUrl(user.mustChangePassword ? '/primeiro-acesso' : '/');
        },
        error: (error: unknown) =>
          this.error.set(this.errors.toMessage(error, 'Não foi possível entrar.')),
      });
  }
}
