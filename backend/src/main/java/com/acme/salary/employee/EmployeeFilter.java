package com.acme.salary.employee;

import com.acme.salary.country.Country;

/** Optional filters for the employee directory. Any null/blank value means "don't filter on this". */
public record EmployeeFilter(String search, Country country, String department, String jobTitle) {
}
