import { Routes } from '@angular/router';
import { AuthComponent } from './auth/auth.component';

export const routes: Routes = [
  { path: 'auth', component: AuthComponent },
  { path: 'forgot-password', loadComponent: () => import('./forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent) },
  { path: 'dashboard', loadComponent: () => import('./dashboard/dashboard.component').then(m => m.DashboardComponent) },
  { path: 'reset-password', loadComponent: () => import('./reset-password/reset-password.component').then(m => m.ResetPasswordComponent) },
  { path: 'home', loadComponent: () => import('./home/home.component').then(m => m.HomeComponent ) },
    { path: 'update', loadComponent: () => import('./update/update.component').then(m => m.UpdateComponent ) },

  { path: '', redirectTo: 'auth', pathMatch: 'full' } // 👈 Page par défaut
];
