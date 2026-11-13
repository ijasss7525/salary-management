package com.acme.salary.insights;

import java.math.BigDecimal;

/** Response shapes for the insights API. */
public final class InsightDtos {

    private InsightDtos() {
    }

    /** Org-wide numbers. USD values use the static reference rates in {@code Country}, so they are approximate. */
    public record Overview(long headcount, int countries, int jobTitles, BigDecimal totalPayrollUsd,
                           BigDecimal averageSalaryUsd) {
    }

    /** Pay in one country, in that country's currency. */
    public record CountryInsight(String country, String countryName, String currency, BigDecimal totalUsd,
                                 SalaryStats stats) {
    }

    /**
     * Pay for one job title inside one country. {@code belowBand}/{@code aboveBand} count people paid more than
     * {@link InsightsService#BAND_TOLERANCE} below/above the median for that role: a quick list of who to review.
     */
    public record JobTitleInsight(String jobTitle, String department, SalaryStats stats, long belowBand,
                                  long aboveBand) {
    }
}
