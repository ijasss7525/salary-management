package com.acme.salary.employee;

import com.acme.salary.common.PageResponse;
import com.acme.salary.country.Country;
import jakarta.validation.Valid;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> SORTABLE = Set.of(
            "employeeCode", "fullName", "email", "jobTitle", "department", "country", "salary", "hireDate");

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<EmployeeResponse> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Country country,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String jobTitle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fullName") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        return service.search(new EmployeeFilter(search, country, department, jobTitle),
                pageable(page, size, sortBy, direction));
    }

    @GetMapping("/{id}")
    public EmployeeResponse get(@PathVariable long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(@Valid @RequestBody EmployeeRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(@PathVariable long id, @Valid @RequestBody EmployeeRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        service.delete(id);
    }

    /** Clamp paging and only allow known sort fields, so bad input can't cause errors or huge queries. */
    static Pageable pageable(int page, int size, String sortBy, String direction) {
        String field = SORTABLE.contains(sortBy) ? sortBy : "fullName";
        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        // Secondary sort on id keeps paging stable when many rows share the same value.
        Sort sort = Sort.by(dir, field).and(Sort.by(Sort.Direction.ASC, "id"));
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE), sort);
    }
}
