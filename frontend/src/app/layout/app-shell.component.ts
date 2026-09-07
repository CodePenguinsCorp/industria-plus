import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-shell',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './app-shell.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppShellComponent {
  protected readonly title = 'Industria Plus';
  protected readonly currentYear = new Date().getFullYear();

  protected readonly navigation = [
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
    },
  ];
}
