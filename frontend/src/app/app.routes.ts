import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'employees' },
  {
    path: 'employees',
    title: 'Employees · ACME Salary',
    loadComponent: () => import('./employees/employee-list').then((m) => m.EmployeeList),
  },
  {
    path: 'insights',
    title: 'Pay insights · ACME Salary',
    loadComponent: () => import('./insights/insights-page').then((m) => m.InsightsPage),
  },
  { path: '**', redirectTo: 'employees' },
];
