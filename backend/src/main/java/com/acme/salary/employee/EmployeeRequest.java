package com.acme.salary.employee;

import com.acme.salary.country.Country;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/** What the HR manager submits when adding or editing an employee. Salary is annual gross, local currency. */
public record EmployeeRequest(
        @NotBlank @Size(max = 20) String employeeCode,
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Email @Size(max = 160) String email,
        @NotBlank @Size(max = 80) String jobTitle,
        @NotBlank @Size(max = 80) String department,
        @NotNull Country country,
        @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal salary,
        @NotNull @PastOrPresent LocalDate hireDate) {
}
