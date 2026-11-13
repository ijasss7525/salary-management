import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ApiService, toParams } from './api.service';
import { EmployeeQuery } from './models';

const query: EmployeeQuery = {
  search: 'asha',
  country: '',
  department: 'Engineering',
  jobTitle: '',
  page: 2,
  size: 50,
  sortBy: 'salary',
  direction: 'desc',
};

describe('toParams', () => {
  it('drops empty filters and keeps the rest', () => {
    expect(toParams(query).toString()).toBe(
      'search=asha&department=Engineering&page=2&size=50&sortBy=salary&direction=desc',
    );
  });
});

describe('ApiService', () => {
  let api: ApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    api = TestBed.inject(ApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('sends the employee query as URL parameters', () => {
    api.listEmployees(query).subscribe();
    const req = http.expectOne((r) => r.url === '/api/employees');
    expect(req.request.params.get('sortBy')).toBe('salary');
    expect(req.request.params.has('country')).toBe(false);
    req.flush({ content: [], page: 2, size: 50, totalElements: 0, totalPages: 0 });
  });

  it('updates an employee with PUT on its id', () => {
    api.updateEmployee(7, {} as never).subscribe();
    expect(http.expectOne('/api/employees/7').request.method).toBe('PUT');
  });

  it('asks for job-title insights of one country', () => {
    api.jobTitleInsights('IN').subscribe();
    http.expectOne('/api/insights/countries/IN/job-titles').flush([]);
  });
});
