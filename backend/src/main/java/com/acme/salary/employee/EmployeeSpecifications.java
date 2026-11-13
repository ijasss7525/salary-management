package com.acme.salary.employee;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

/** Turns an {@link EmployeeFilter} into a SQL WHERE clause, so filtering happens in the database. */
final class EmployeeSpecifications {

    private EmployeeSpecifications() {
    }

    static Specification<Employee> matching(EmployeeFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hasText(filter.search())) {
                String pattern = "%" + filter.search().trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern),
                        cb.like(cb.lower(root.get("employeeCode")), pattern)));
            }
            if (filter.country() != null) {
                predicates.add(cb.equal(root.get("country"), filter.country()));
            }
            if (hasText(filter.department())) {
                predicates.add(cb.equal(root.get("department"), filter.department().trim()));
            }
            if (hasText(filter.jobTitle())) {
                predicates.add(cb.equal(root.get("jobTitle"), filter.jobTitle().trim()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
