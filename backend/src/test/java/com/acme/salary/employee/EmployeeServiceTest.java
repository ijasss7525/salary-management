package com.acme.salary.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.acme.salary.common.ConflictException;
import com.acme.salary.common.NotFoundException;
import com.acme.salary.country.Country;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class EmployeeServiceTest {

    private final EmployeeRepository repository = mock(EmployeeRepository.class);
    private final EmployeeService service = new EmployeeService(repository);

    private static EmployeeRequest request(String code, String email) {
        return new EmployeeRequest(code, "  Asha Rao ", email, "Software Engineer", "Engineering", Country.IN,
                new BigDecimal("2400000"), LocalDate.of(2024, 4, 1));
    }

    @Test
    void createNormalisesInputBeforeSaving() {
        when(repository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeResponse created = service.create(request(" emp10001 ", " Asha.Rao@ACME.com "));

        assertThat(created.employeeCode()).isEqualTo("EMP10001");
        assertThat(created.email()).isEqualTo("asha.rao@acme.com");
        assertThat(created.fullName()).isEqualTo("Asha Rao");
        assertThat(created.currency()).isEqualTo("INR");
    }

    @Test
    void createRejectsDuplicateEmail() {
        when(repository.existsByEmailIgnoreCase("asha.rao@acme.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("EMP10001", "asha.rao@acme.com")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("asha.rao@acme.com");
        verify(repository, never()).save(any());
    }

    @Test
    void createRejectsDuplicateEmployeeCode() {
        when(repository.existsByEmployeeCodeIgnoreCase("EMP10001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("EMP10001", "asha.rao@acme.com")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("EMP10001");
    }

    @Test
    void updateChangesTheExistingEmployee() {
        Employee existing = new Employee("EMP00001", "Old Name", "old@acme.com", "QA Engineer", "Engineering",
                Country.US, new BigDecimal("90000"), LocalDate.of(2020, 1, 1));
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        EmployeeResponse updated = service.update(1L, request("EMP00001", "asha.rao@acme.com"));

        assertThat(updated.fullName()).isEqualTo("Asha Rao");
        assertThat(updated.country()).isEqualTo("IN");
        assertThat(existing.getSalary()).isEqualByComparingTo("2400000");
    }

    @Test
    void updateOfUnknownEmployeeIsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, request("EMP1", "a@acme.com")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteOfUnknownEmployeeIsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(NotFoundException.class);
    }
}
