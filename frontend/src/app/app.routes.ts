import { Routes } from '@angular/router';
import { AppShellComponent } from './layout/app-shell.component';
import { HomeComponent } from './pages/home/home.component';

export const routes: Routes = [
  {
    path: '',
    component: AppShellComponent,
    children: [
      {
        path: '',
        component: HomeComponent,
        title: 'Industria Plus | Inicio',
      },
    ],
  },
  {
    path: '**',
    redirectTo: '',
  },
];
