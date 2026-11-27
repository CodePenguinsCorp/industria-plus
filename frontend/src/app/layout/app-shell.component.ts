import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from '../core/services/auth.service';
@Component({
  selector: 'app-shell',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './app-shell.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppShellComponent {
  protected readonly auth = inject(AuthService);
  protected roleLabel() {
    return { ADMIN: 'Administrador', MANAGER: 'Gestor', TECHNICIAN: 'Técnico' }[
      this.auth.user()?.role ?? 'TECHNICIAN'
    ];
  }
  protected readonly title = 'Industria Plus';
  protected readonly currentYear = new Date().getFullYear();

  private readonly allNavigation = [
    { label: 'Visão geral', shortLabel: 'Início', icon: 'dashboard', route: '/', exact: true },
    {
      label: 'Chamados',
      shortLabel: 'Chamados',
      icon: 'requests',
      route: '/chamados',
      exact: false,
    },
    {
      label: 'Equipamentos',
      shortLabel: 'Ativos',
      icon: 'equipment',
      route: '/equipamentos',
      exact: false,
    },
    { label: 'Setores', shortLabel: 'Setores', icon: 'sectors', route: '/setores', exact: false },
    {
      label: 'Técnicos',
      shortLabel: 'Equipe',
      icon: 'technicians',
      route: '/tecnicos',
      exact: false,
      managersOnly: true,
    },
  ];

  /** A tela de Técnicos aparece para administrador (edição) e gestor (visualização). */
  protected readonly navigation = computed(() =>
    this.allNavigation.filter((item) => !item.managersOnly || this.auth.canManage()),
  );
  protected readonly navCount = computed(
    () => this.navigation().length + (this.auth.isAdmin() ? 1 : 0),
  );
}
