import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CountryInsight,
  Employee,
  EmployeeQuery,
  EmployeeRequest,
  JobTitleInsight,
  Meta,
  Overview,
  Page,
} from './models';

/** Thin, typed wrapper around the REST API. Components never build URLs themselves. */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);

  listEmployees(query: EmployeeQuery): Observable<Page<Employee>> {
    return this.http.get<Page<Employee>>('/api/employees', { params: toParams(query) });
  }

  createEmployee(request: EmployeeRequest): Observable<Employee> {
    return this.http.post<Employee>('/api/employees', request);
  }

  updateEmployee(id: number, request: EmployeeRequest): Observable<Employee> {
    return this.http.put<Employee>(`/api/employees/${id}`, request);
  }

  deleteEmployee(id: number): Observable<void> {
    return this.http.delete<void>(`/api/employees/${id}`);
  }

  meta(): Observable<Meta> {
    return this.http.get<Meta>('/api/meta');
  }

  overview(): Observable<Overview> {
    return this.http.get<Overview>('/api/insights/overview');
  }

  countryInsights(): Observable<CountryInsight[]> {
    return this.http.get<CountryInsight[]>('/api/insights/countries');
  }

  jobTitleInsights(country: string): Observable<JobTitleInsight[]> {
    return this.http.get<JobTitleInsight[]>(`/api/insights/countries/${country}/job-titles`);
  }
}

/** Only send filters that are actually set, so URLs stay short and the API sees "no filter" as absent. */
export function toParams(query: EmployeeQuery): HttpParams {
  let params = new HttpParams();
  for (const [key, value] of Object.entries(query)) {
    if (value !== '' && value !== null && value !== undefined) {
      params = params.set(key, String(value));
    }
  }
  return params;
}
