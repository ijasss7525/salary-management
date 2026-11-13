package com.acme.salary.employee;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeResponse(
        Long id,
        String employeeCode,
        String fullName,
        String email,
        String jobTitle,
        String department,
        String country,
        String countryName,
        String currency,
        BigDecimal salary,
        LocalDate hireDate) {

    static EmployeeResponse from(Employee e) {
        return new EmployeeResponse(
                e.getId(),
                e.getEmployeeCode(),
                e.getFullName(),
                e.getEmail(),
                e.getJobTitle(),
                e.getDepartment(),
                e.getCountry().name(),
                e.getCountry().displayName(),
                e.getCountry().currency(),
                e.getSalary(),
                e.getHireDate());
    }
}
