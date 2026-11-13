package com.acme.salary.employee;

import static org.assertj.core.api.Assertions.assertThat;

import com.acme.salary.country.Country;
import com.acme.salary.insights.SalaryRow;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/** Runs against real SQLite (in memory) to prove the queries, converters and filters work on the real database. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        repository.saveAll(List.of(
                employee("EMP1", "Asha Rao", "asha@acme.com", "Software Engineer", "Engineering", Country.IN, 2_400_000),
                employee("EMP2", "Ben Clark", "ben@acme.com", "Software Engineer", "Engineering", Country.US, 130_000),
                employee("EMP3", "Chen Lim", "chen@acme.com", "Accountant", "Finance", Country.SG, 90_000)));
    }

    private static Employee employee(String code, String name, String email, String title, String dept,
                                     Country country, int salary) {
        return new Employee(code, name, email, title, dept, country, BigDecimal.valueOf(salary),
                LocalDate.of(2023, 5, 17));
    }

    private Page<Employee> search(EmployeeFilter filter) {
        return repository.findAll(EmployeeSpecifications.matching(filter),
                PageRequest.of(0, 10, Sort.by("fullName")));
    }

    @Test
    void searchMatchesNameEmailOrCodeCaseInsensitively() {
        assertThat(search(new EmployeeFilter("ASHA", null, null, null)).getContent())
                .extracting(Employee::getFullName).containsExactly("Asha Rao");
        assertThat(search(new EmployeeFilter("emp2", null, null, null)).getContent())
                .extracting(Employee::getFullName).containsExactly("Ben Clark");
    }

    @Test
    void filtersCombine() {
        Page<Employee> page = search(new EmployeeFilter(null, Country.US, "Engineering", "Software Engineer"));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getEmail()).isEqualTo("ben@acme.com");
    }

    @Test
    void emptyFilterReturnsEveryoneSorted() {
        assertThat(search(new EmployeeFilter(null, null, " ", null)).getContent())
                .extracting(Employee::getFullName).containsExactly("Asha Rao", "Ben Clark", "Chen Lim");
    }

    @Test
    void datesAndSalariesRoundTripThroughSqlite() {
        Employee asha = search(new EmployeeFilter("asha", null, null, null)).getContent().get(0);

        assertThat(asha.getHireDate()).isEqualTo(LocalDate.of(2023, 5, 17));
        assertThat(asha.getSalary()).isEqualByComparingTo("2400000");
        assertThat(asha.getCountry()).isEqualTo(Country.IN);
    }

    @Test
    void salaryRowProjectionsAndDistinctValues() {
        List<SalaryRow> rows = repository.findSalaryRowsByCountry(Country.US);

        assertThat(rows).containsExactly(
                new SalaryRow(Country.US, "Software Engineer", "Engineering", rows.get(0).salary()));
        assertThat(repository.findAllSalaryRows()).hasSize(3);
        assertThat(repository.findDistinctDepartments()).containsExactly("Engineering", "Finance");
        assertThat(repository.existsByEmailIgnoreCase("ASHA@acme.com")).isTrue();
    }
}
