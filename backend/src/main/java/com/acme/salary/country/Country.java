package com.acme.salary.country;

import java.math.BigDecimal;

/**
 * Countries where ACME employs people. Each country pays in one local currency.
 *
 * <p>{@code usdRate} is a static reference rate (1 unit of local currency in USD) used only for
 * org-wide totals. It is deliberately static: the numbers stay reproducible and testable, and the
 * UI labels USD totals as approximate. Live FX is listed as out of scope in docs/REQUIREMENTS.md.
 */
public enum Country {
    IN("India", "INR", "0.012"),
    US("United States", "USD", "1.00"),
    GB("United Kingdom", "GBP", "1.27"),
    DE("Germany", "EUR", "1.08"),
    CA("Canada", "CAD", "0.73"),
    AU("Australia", "AUD", "0.66"),
    SG("Singapore", "SGD", "0.74"),
    AE("United Arab Emirates", "AED", "0.272");

    private final String displayName;
    private final String currency;
    private final BigDecimal usdRate;

    Country(String displayName, String currency, String usdRate) {
        this.displayName = displayName;
        this.currency = currency;
        this.usdRate = new BigDecimal(usdRate);
    }

    public String displayName() {
        return displayName;
    }

    public String currency() {
        return currency;
    }

    public BigDecimal usdRate() {
        return usdRate;
    }

    public BigDecimal toUsd(BigDecimal localAmount) {
        return localAmount.multiply(usdRate);
    }
}
