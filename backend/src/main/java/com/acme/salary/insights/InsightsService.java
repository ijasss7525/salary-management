package com.acme.salary.insights;

import com.acme.salary.country.Country;
import com.acme.salary.employee.EmployeeRepository;
import com.acme.salary.insights.InsightDtos.CountryInsight;
import com.acme.salary.insights.InsightDtos.JobTitleInsight;
import com.acme.salary.insights.InsightDtos.Overview;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Answers "how does the org pay people?".
 *
 * <p>Rows are loaded once (4 small columns x 10,000 rows is well under a megabyte) and grouped in Java.
 * That keeps the median calculation simple and testable; SQLite has no MEDIAN function. If the data grew
 * by 100x, the min/avg/max part would move into SQL GROUP BY queries (see docs/DECISIONS.md).
 */
@Service
public class InsightsService {

    /** Salaries more than 20% away from the role's median in that country are flagged for review. */
    public static final BigDecimal BAND_TOLERANCE = new BigDecimal("0.20");

    private final EmployeeRepository repository;

    public InsightsService(EmployeeRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Overview overview() {
        List<SalaryRow> rows = repository.findAllSalaryRows();
        BigDecimal totalUsd = rows.stream()
                .map(r -> r.country().toUsd(r.salary()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(0, RoundingMode.HALF_UP);
        BigDecimal averageUsd = rows.isEmpty()
                ? BigDecimal.ZERO
                : totalUsd.divide(BigDecimal.valueOf(rows.size()), 0, RoundingMode.HALF_UP);
        int countries = (int) rows.stream().map(SalaryRow::country).distinct().count();
        int jobTitles = (int) rows.stream().map(SalaryRow::jobTitle).distinct().count();
        return new Overview(rows.size(), countries, jobTitles, totalUsd, averageUsd);
    }

    /** One line per country, largest headcount first. */
    @Transactional(readOnly = true)
    public List<CountryInsight> byCountry() {
        Map<Country, List<BigDecimal>> salariesByCountry = repository.findAllSalaryRows().stream()
                .collect(Collectors.groupingBy(SalaryRow::country,
                        Collectors.mapping(SalaryRow::salary, Collectors.toList())));

        return salariesByCountry.entrySet().stream()
                .map(e -> {
                    SalaryStats stats = SalaryStats.of(e.getValue());
                    Country c = e.getKey();
                    return new CountryInsight(c.name(), c.displayName(), c.currency(),
                            c.toUsd(stats.total()).setScale(0, RoundingMode.HALF_UP), stats);
                })
                .sorted(Comparator.comparingLong((CountryInsight i) -> i.stats().headcount()).reversed()
                        .thenComparing(CountryInsight::countryName))
                .toList();
    }

    /** Pay per job title inside one country, so every number shares one currency. */
    @Transactional(readOnly = true)
    public List<JobTitleInsight> byJobTitle(Country country) {
        Map<String, List<SalaryRow>> rowsByTitle = repository.findSalaryRowsByCountry(country).stream()
                .collect(Collectors.groupingBy(SalaryRow::jobTitle));

        return rowsByTitle.values().stream()
                .map(InsightsService::toJobTitleInsight)
                .sorted(Comparator.comparing(JobTitleInsight::department).thenComparing(JobTitleInsight::jobTitle))
                .toList();
    }

    static JobTitleInsight toJobTitleInsight(List<SalaryRow> rows) {
        List<BigDecimal> salaries = rows.stream().map(SalaryRow::salary).toList();
        SalaryStats stats = SalaryStats.of(salaries);
        BigDecimal lower = stats.median().multiply(BigDecimal.ONE.subtract(BAND_TOLERANCE));
        BigDecimal upper = stats.median().multiply(BigDecimal.ONE.add(BAND_TOLERANCE));
        long below = salaries.stream().filter(s -> s.compareTo(lower) < 0).count();
        long above = salaries.stream().filter(s -> s.compareTo(upper) > 0).count();
        SalaryRow first = rows.get(0);
        return new JobTitleInsight(first.jobTitle(), first.department(), stats, below, above);
    }
}
