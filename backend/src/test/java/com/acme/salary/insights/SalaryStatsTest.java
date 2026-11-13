package com.acme.salary.insights;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class SalaryStatsTest {

    private static List<BigDecimal> salaries(int... values) {
        return java.util.Arrays.stream(values).mapToObj(BigDecimal::valueOf).toList();
    }

    @Test
    void emptyGroupHasZeroHeadcount() {
        assertThat(SalaryStats.of(List.of())).isEqualTo(SalaryStats.EMPTY);
    }

    @Test
    void oddCountUsesMiddleValueAsMedian() {
        SalaryStats stats = SalaryStats.of(salaries(300, 100, 200));

        assertThat(stats.headcount()).isEqualTo(3);
        assertThat(stats.min()).isEqualByComparingTo("100");
        assertThat(stats.median()).isEqualByComparingTo("200");
        assertThat(stats.max()).isEqualByComparingTo("300");
        assertThat(stats.total()).isEqualByComparingTo("600");
        assertThat(stats.average()).isEqualByComparingTo("200");
    }

    @Test
    void evenCountAveragesTheTwoMiddleValues() {
        SalaryStats stats = SalaryStats.of(salaries(100, 200, 300, 1000));

        assertThat(stats.median()).isEqualByComparingTo("250");
    }

    @Test
    void medianResistsOutliersWhileAverageDoesNot() {
        SalaryStats stats = SalaryStats.of(salaries(50, 50, 50, 50, 1_000));

        assertThat(stats.median()).isEqualByComparingTo("50");
        assertThat(stats.average()).isEqualByComparingTo("240");
    }

    @Test
    void averageIsRoundedToTwoDecimals() {
        SalaryStats stats = SalaryStats.of(salaries(1, 1, 2));

        assertThat(stats.average()).isEqualByComparingTo("1.33");
    }
}
