import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ApiService } from '../core/api.service';
import { CountryInsight, JobTitleInsight } from '../core/models';
import { MoneyPipe } from '../core/money.pipe';

/**
 * "How does the org pay people?" Org-wide overview first, then pay by country (each in its own currency),
 * then pay per job title inside the chosen country with a min-median-max range bar and pay-band flags.
 */
@Component({
  selector: 'app-insights-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DecimalPipe, MoneyPipe, MatButtonToggleModule, MatIconModule, MatProgressBarModule, MatTooltipModule],
  templateUrl: './insights-page.html',
  styleUrl: './insights-page.scss',
})
export class InsightsPage {
  private readonly api = inject(ApiService);

  protected readonly overview = rxResource({ stream: () => this.api.overview() });
  protected readonly countries = rxResource({
    stream: () => this.api.countryInsights(),
    defaultValue: [] as CountryInsight[],
  });

  protected readonly selected = signal<string | null>(null);

  /** Default to the largest country until the user picks one. */
  protected readonly activeCountry = computed<CountryInsight | undefined>(() => {
    const list = this.countries.value();
    return list.find((c) => c.country === this.selected()) ?? list[0];
  });

  protected readonly jobTitles = rxResource({
    params: () => this.activeCountry()?.country,
    stream: ({ params }) => this.api.jobTitleInsights(params),
    defaultValue: [] as JobTitleInsight[],
  });

  /** Highest salary in the country, used to scale the range bars so roles can be compared at a glance. */
  private readonly scaleMax = computed(() =>
    Math.max(1, ...this.jobTitles.value().map((j) => j.stats.max)),
  );

  protected readonly flagged = computed(() =>
    this.jobTitles.value().reduce((sum, j) => sum + j.belowBand + j.aboveBand, 0),
  );

  protected select(country: string): void {
    this.selected.set(country);
  }

  protected percent(value: number): number {
    return (value / this.scaleMax()) * 100;
  }
}
