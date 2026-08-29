import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-shell',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './app-shell.component.html',
  styleUrl: './app-shell.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppShellComponent {
  protected readonly title = 'Industria Plus';

  protected readonly navigation = [
    { label: 'Visão geral', route: '/', exact: true },
    { label: 'Chamados', route: '/chamados', exact: false },
    { label: 'Equipamentos', route: '/equipamentos', exact: false },
    { label: 'Setores', route: '/setores', exact: false },
    { label: 'Técnicos', route: '/tecnicos', exact: false },
  ];
}
