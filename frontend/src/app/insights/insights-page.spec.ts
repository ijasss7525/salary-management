import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CountryInsight, JobTitleInsight } from '../core/models';
import { InsightsPage } from './insights-page';

const stats = (median: number) => ({ headcount: 10, min: median * 0.7, median, average: median, max: median * 1.4, total: median * 10 });

const COUNTRIES: CountryInsight[] = [
  { country: 'IN', countryName: 'India', currency: 'INR', totalUsd: 300000, stats: stats(2500000) },
  { country: 'US', countryName: 'United States', currency: 'USD', totalUsd: 1300000, stats: stats(130000) },
];

const TITLES: JobTitleInsight[] = [
  { jobTitle: 'Software Engineer', department: 'Engineering', stats: stats(130000), belowBand: 1, aboveBand: 2 },
];

/** Run pending effects and change detection. (whenStable would wait for the open HTTP requests forever.) */
async function settle(): Promise<void> {
  TestBed.tick();
  await Promise.resolve();
  TestBed.tick();
}

describe('InsightsPage', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [InsightsPage],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });

  it('shows the overview, defaults to the largest country, and switches country on click', async () => {
    const fixture = TestBed.createComponent(InsightsPage);
    fixture.detectChanges();
    await settle();
    http.expectOne('/api/insights/overview').flush({
      headcount: 10000, countries: 8, jobTitles: 13, totalPayrollUsd: 900000000, averageSalaryUsd: 90000,
    });
    http.expectOne('/api/insights/countries').flush(COUNTRIES);
    await settle();
    http.expectOne('/api/insights/countries/IN/job-titles').flush(TITLES);
    await settle();

    const el = fixture.nativeElement as HTMLElement;
    expect(el.textContent).toContain('10,000');
    expect(el.textContent).toContain('Pay by job title in India');
    expect(el.textContent).toContain('3 people outside');

    const usRow = el.querySelectorAll<HTMLTableRowElement>('[data-testid="country-table"] tbody tr')[1];
    usRow.click();
    await settle();
    http.expectOne('/api/insights/countries/US/job-titles').flush(TITLES);
    await settle();
    expect(el.textContent).toContain('Pay by job title in United States');
    expect(el.textContent).toContain('$130,000');
  });
});
