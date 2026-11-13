package com.acme.salary.insights;

import com.acme.salary.country.Country;
import java.math.BigDecimal;

/** The slice of an employee the insights need: where they work, what they do, what they earn. */
public record SalaryRow(Country country, String jobTitle, String department, BigDecimal salary) {
}
