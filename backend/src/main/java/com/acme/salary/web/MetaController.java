package com.acme.salary.web;

import com.acme.salary.country.Country;
import com.acme.salary.employee.EmployeeRepository;
import java.util.Arrays;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Values for the UI's dropdowns: supported countries and the departments/job titles already in use. */
@RestController
public class MetaController {

    public record CountryOption(String code, String name, String currency) {
    }

    public record Meta(List<CountryOption> countries, List<String> departments, List<String> jobTitles) {
    }

    private final EmployeeRepository repository;

    public MetaController(EmployeeRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/meta")
    @Transactional(readOnly = true)
    public Meta meta() {
        List<CountryOption> countries = Arrays.stream(Country.values())
                .map(c -> new CountryOption(c.name(), c.displayName(), c.currency()))
                .toList();
        return new Meta(countries, repository.findDistinctDepartments(), repository.findDistinctJobTitles());
    }
}
