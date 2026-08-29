import { Routes } from '@angular/router';
import { EquipmentComponent } from './features/equipment/equipment.component';
import { MaintenanceRequestComponent } from './features/maintenance-request/maintenance-request.component';
import { SectorComponent } from './features/sector/sector.component';
import { TechnicianComponent } from './features/technician/technician.component';
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
