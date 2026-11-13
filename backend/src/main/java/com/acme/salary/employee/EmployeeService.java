package com.acme.salary.employee;

import com.acme.salary.common.ConflictException;
import com.acme.salary.common.NotFoundException;
import com.acme.salary.common.PageResponse;
import java.util.Locale;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> search(EmployeeFilter filter, Pageable pageable) {
        return PageResponse.of(
                repository.findAll(EmployeeSpecifications.matching(filter), pageable),
                EmployeeResponse::from);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse get(long id) {
        return EmployeeResponse.from(find(id));
    }

    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        EmployeeRequest r = normalize(request);
        if (repository.existsByEmployeeCodeIgnoreCase(r.employeeCode())) {
            throw new ConflictException("Employee code " + r.employeeCode() + " is already in use");
        }
        if (repository.existsByEmailIgnoreCase(r.email())) {
            throw new ConflictException("Email " + r.email() + " is already in use");
        }
        Employee saved = repository.save(new Employee(r.employeeCode(), r.fullName(), r.email(), r.jobTitle(),
                r.department(), r.country(), r.salary(), r.hireDate()));
        return EmployeeResponse.from(saved);
    }

    @Transactional
    public EmployeeResponse update(long id, EmployeeRequest request) {
        EmployeeRequest r = normalize(request);
        Employee employee = find(id);
        if (repository.existsByEmployeeCodeIgnoreCaseAndIdNot(r.employeeCode(), id)) {
            throw new ConflictException("Employee code " + r.employeeCode() + " is already in use");
        }
        if (repository.existsByEmailIgnoreCaseAndIdNot(r.email(), id)) {
            throw new ConflictException("Email " + r.email() + " is already in use");
        }
        employee.update(r.employeeCode(), r.fullName(), r.email(), r.jobTitle(), r.department(), r.country(),
                r.salary(), r.hireDate());
        return EmployeeResponse.from(employee);
    }

    @Transactional
    public void delete(long id) {
        repository.delete(find(id));
    }

    private Employee find(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee " + id + " not found"));
    }

    /** Trim text fields and lower-case the email so "A@acme.com " and "a@acme.com" count as the same person. */
    private static EmployeeRequest normalize(EmployeeRequest r) {
        return new EmployeeRequest(
                r.employeeCode().trim().toUpperCase(Locale.ROOT),
                r.fullName().trim(),
                r.email().trim().toLowerCase(Locale.ROOT),
                r.jobTitle().trim(),
                r.department().trim(),
                r.country(),
                r.salary(),
                r.hireDate());
    }
}
