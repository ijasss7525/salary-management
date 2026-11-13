package com.acme.salary.seed;

import static org.assertj.core.api.Assertions.assertThat;

import com.acme.salary.seed.EmployeeGenerator.SeedEmployee;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class EmployeeGeneratorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 13);

    @Test
    void generatesTheRequestedNumberOfEmployees() {
        assertThat(new EmployeeGenerator(1, TODAY).generate(10_000)).hasSize(10_000);
    }

    @Test
    void sameSeedProducesTheSameEmployees() {
        List<SeedEmployee> first = new EmployeeGenerator(42, TODAY).generate(500);
        List<SeedEmployee> second = new EmployeeGenerator(42, TODAY).generate(500);

        assertThat(first).isEqualTo(second);
    }

    @Test
    void codesAndEmailsAreUnique() {
        List<SeedEmployee> employees = new EmployeeGenerator(7, TODAY).generate(10_000);

        assertThat(employees).extracting(SeedEmployee::employeeCode).doesNotHaveDuplicates();
        assertThat(employees).extracting(SeedEmployee::email).doesNotHaveDuplicates();
    }

    @Test
    void everyEmployeeIsValid() {
        List<SeedEmployee> employees = new EmployeeGenerator(7, TODAY).generate(2_000);

        assertThat(employees).allSatisfy(e -> {
            assertThat(e.salary()).isGreaterThan(BigDecimal.ZERO);
            assertThat(e.salary().remainder(BigDecimal.valueOf(100))).isEqualByComparingTo("0");
            assertThat(e.hireDate()).isBetween(LocalDate.of(2012, 1, 1), TODAY);
            assertThat(e.email()).matches("[a-z]+\\.[a-z]+\\.\\d+@acme\\.com");
        });
    }

    @Test
    void spreadsEmployeesAcrossAllCountries() {
        List<SeedEmployee> employees = new EmployeeGenerator(7, TODAY).generate(10_000);

        assertThat(employees).extracting(SeedEmployee::country).doesNotContainNull();
        assertThat(employees.stream().map(SeedEmployee::country).distinct().count()).isEqualTo(8);
    }
}
