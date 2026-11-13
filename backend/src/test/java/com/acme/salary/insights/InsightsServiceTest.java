package com.acme.salary.insights;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.acme.salary.country.Country;
import com.acme.salary.employee.EmployeeRepository;
import com.acme.salary.insights.InsightDtos.CountryInsight;
import com.acme.salary.insights.InsightDtos.JobTitleInsight;
import com.acme.salary.insights.InsightDtos.Overview;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class InsightsServiceTest {

    private final EmployeeRepository repository = mock(EmployeeRepository.class);
    private final InsightsService service = new InsightsService(repository);

    private static SalaryRow row(Country country, String title, int salary) {
        return new SalaryRow(country, title, "Engineering", BigDecimal.valueOf(salary));
    }

    @Test
    void overviewConvertsEveryCountryToUsdBeforeAdding() {
        when(repository.findAllSalaryRows()).thenReturn(List.of(
                row(Country.US, "Software Engineer", 100_000),
                row(Country.IN, "Software Engineer", 2_500_000))); // 2.5M INR * 0.012 = 30,000 USD

        Overview overview = service.overview();

        assertThat(overview.headcount()).isEqualTo(2);
        assertThat(overview.countries()).isEqualTo(2);
        assertThat(overview.jobTitles()).isEqualTo(1);
        assertThat(overview.totalPayrollUsd()).isEqualByComparingTo("130000");
        assertThat(overview.averageSalaryUsd()).isEqualByComparingTo("65000");
    }

    @Test
    void overviewOfEmptyOrgIsAllZeros() {
        when(repository.findAllSalaryRows()).thenReturn(List.of());

        Overview overview = service.overview();

        assertThat(overview.headcount()).isZero();
        assertThat(overview.averageSalaryUsd()).isEqualByComparingTo("0");
    }

    @Test
    void byCountryKeepsLocalCurrencyAndSortsByHeadcount() {
        when(repository.findAllSalaryRows()).thenReturn(List.of(
                row(Country.US, "Software Engineer", 100_000),
                row(Country.IN, "Software Engineer", 2_000_000),
                row(Country.IN, "Software Engineer", 3_000_000)));

        List<CountryInsight> result = service.byCountry();

        assertThat(result).extracting(CountryInsight::country).containsExactly("IN", "US");
        CountryInsight india = result.get(0);
        assertThat(india.currency()).isEqualTo("INR");
        assertThat(india.stats().median()).isEqualByComparingTo("2500000");
        assertThat(india.totalUsd()).isEqualByComparingTo("60000");
    }

    @Test
    void byJobTitleFlagsSalariesMoreThanTwentyPercentFromTheMedian() {
        when(repository.findSalaryRowsByCountry(Country.US)).thenReturn(List.of(
                row(Country.US, "QA Engineer", 70_000),   // 30% below median 100k -> below band
                row(Country.US, "QA Engineer", 100_000),
                row(Country.US, "QA Engineer", 110_000),  // within 20%
                row(Country.US, "QA Engineer", 125_000),  // 25% above -> above band
                row(Country.US, "QA Engineer", 90_000)));

        List<JobTitleInsight> result = service.byJobTitle(Country.US);

        assertThat(result).hasSize(1);
        JobTitleInsight qa = result.get(0);
        assertThat(qa.stats().median()).isEqualByComparingTo("100000");
        assertThat(qa.belowBand()).isEqualTo(1);
        assertThat(qa.aboveBand()).isEqualTo(1);
    }
}
