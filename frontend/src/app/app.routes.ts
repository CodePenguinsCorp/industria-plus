import { Routes } from '@angular/router';
import { EquipmentComponent } from './features/equipment/equipment.component';
import { MaintenanceRequestComponent } from './features/maintenance-request/maintenance-request.component';
import { SectorComponent } from './features/sector/sector.component';
import { TechnicianComponent } from './features/technician/technician.component';
import { AppShellComponent } from './layout/app-shell.component';
import { HomeComponent } from './pages/home/home.component';

import { adminGuard, authGuard, managerGuard, passwordChangeGuard } from './core/auth.guard';
import { FirstAccessComponent } from './pages/first-access/first-access.component';
import { LoginComponent } from './pages/login/login.component';
import { UsersComponent } from './features/users/users.component';
import { EquipmentDetailComponent } from './features/equipment/equipment-detail.component';
import { RequestDetailComponent } from './features/maintenance-request/request-detail.component';
export const routes: Routes = [
  { path: 'login', component: LoginComponent, title: 'Industria Plus | Login' },
  {
    path: 'primeiro-acesso',
    component: FirstAccessComponent,
    canActivate: [passwordChangeGuard],
    title: 'Industria Plus | Primeiro acesso',
  },
  {
    path: '',
    component: AppShellComponent,
    canActivate: [authGuard],
    canActivateChild: [authGuard],
    children: [
      {
        path: 'usuarios',
        component: UsersComponent,
        canActivate: [adminGuard],
        title: 'Industria Plus | Usuários',
      },
      {
        path: 'equipamentos/:id',
        component: EquipmentDetailComponent,
        title: 'Industria Plus | Detalhes do equipamento',
      },
      {
        path: 'chamados/:id',
        component: RequestDetailComponent,
        title: 'Industria Plus | Atendimento',
      },
      {
        path: '',
        component: HomeComponent,
        title: 'Industria Plus | Início',
      },
      {
        path: 'setores',
        component: SectorComponent,
        title: 'Industria Plus | Setores',
      },
      {
        path: 'equipamentos',
        component: EquipmentComponent,
        title: 'Industria Plus | Equipamentos',
      },
      {
        path: 'tecnicos',
        component: TechnicianComponent,
        canActivate: [managerGuard],
        title: 'Industria Plus | Técnicos',
      },
      {
        path: 'chamados',
        component: MaintenanceRequestComponent,
        title: 'Industria Plus | Chamados',
      },
    ],
  },
  {
    path: '**',
    redirectTo: '',
  },
];
