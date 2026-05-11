import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { UsuariosComponent } from './usuarios/usuarios.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { AuditoriaComponent } from './auditoria/auditoria.component';
import { authGuard, adminGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'usuarios', component: UsuariosComponent, canActivate: [authGuard, adminGuard] },
  { path: 'auditoria', component: AuditoriaComponent, canActivate: [authGuard, adminGuard] },
];
