import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ASHA, META } from '../testing';
import { EmployeeList } from './employee-list';

/** Run pending effects and change detection. (whenStable would wait for the open HTTP requests forever.) */
async function settle(): Promise<void> {
  TestBed.tick();
  await Promise.resolve();
  TestBed.tick();
}

describe('EmployeeList', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [EmployeeList],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });

  async function render() {
    const fixture = TestBed.createComponent(EmployeeList);
    fixture.detectChanges();
    await settle();
    http.expectOne('/api/meta').flush(META);
    const req = http.expectOne((r) => r.url === '/api/employees');
    req.flush({ content: [ASHA], page: 0, size: 20, totalElements: 1, totalPages: 1 });
    await settle();
    return { fixture, req, el: fixture.nativeElement as HTMLElement };
  }

  it('loads the first page sorted by name and shows salaries in local currency', async () => {
    const { req, el } = await render();

    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('sortBy')).toBe('fullName');
    expect(el.textContent).toContain('Asha Rao');
    expect(el.textContent).toContain('₹24,00,000');
    expect(el.textContent).toContain('1 people');
  });

  it('filters by country and goes back to the first page', async () => {
    const { fixture } = await render();
    const component = fixture.componentInstance as unknown as { setCountry(v: string): void; onPage(e: object): void };

    component.onPage({ pageIndex: 3, pageSize: 20, length: 100 });
    await settle();
    http.expectOne((r) => r.url === '/api/employees').flush({ content: [], page: 3, size: 20, totalElements: 100, totalPages: 5 });

    component.setCountry('IN');
    await settle();
    const req = http.expectOne((r) => r.url === '/api/employees');
    expect(req.request.params.get('country')).toBe('IN');
    expect(req.request.params.get('page')).toBe('0');
    req.flush({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 });
  });
});
