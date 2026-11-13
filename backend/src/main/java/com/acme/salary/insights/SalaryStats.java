package com.acme.salary.insights;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;

/**
 * Summary statistics for a group of salaries that are all in the same currency.
 *
 * <p>Median is reported next to the average because salary data is skewed: a few very high
 * salaries pull the average up, while the median stays the "typical" salary.
 */
public record SalaryStats(long headcount, BigDecimal min, BigDecimal median, BigDecimal average, BigDecimal max,
                          BigDecimal total) {

    public static final SalaryStats EMPTY =
            new SalaryStats(0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

    public static SalaryStats of(Collection<BigDecimal> salaries) {
        if (salaries.isEmpty()) {
            return EMPTY;
        }
        List<BigDecimal> sorted = salaries.stream().sorted().toList();
        int n = sorted.size();
        BigDecimal total = sorted.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal median = n % 2 == 1
                ? sorted.get(n / 2)
                : sorted.get(n / 2 - 1).add(sorted.get(n / 2)).divide(BigDecimal.TWO, 2, RoundingMode.HALF_UP);
        BigDecimal average = total.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
        return new SalaryStats(n, sorted.get(0), median, average, sorted.get(n - 1), total);
    }
}
