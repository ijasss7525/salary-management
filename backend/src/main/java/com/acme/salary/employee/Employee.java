package com.acme.salary.employee;

import com.acme.salary.country.Country;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/** One employee and their current annual gross salary, in the local currency of their country. */
@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_code", nullable = false, unique = true)
    private String employeeCode;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "job_title", nullable = false)
    private String jobTitle;

    @Column(nullable = false)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(name = "country_code", nullable = false)
    private Country country;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal salary;

    @Convert(converter = LocalDateStringConverter.class)
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    protected Employee() {
        // for JPA
    }

    public Employee(String employeeCode, String fullName, String email, String jobTitle, String department,
                    Country country, BigDecimal salary, LocalDate hireDate) {
        update(employeeCode, fullName, email, jobTitle, department, country, salary, hireDate);
    }

    public final void update(String employeeCode, String fullName, String email, String jobTitle, String department,
                             Country country, BigDecimal salary, LocalDate hireDate) {
        this.employeeCode = employeeCode;
        this.fullName = fullName;
        this.email = email;
        this.jobTitle = jobTitle;
        this.department = department;
        this.country = country;
        this.salary = salary;
        this.hireDate = hireDate;
    }

    public Long getId() {
        return id;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getDepartment() {
        return department;
    }

    public Country getCountry() {
        return country;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }
}
