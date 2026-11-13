package com.acme.salary.seed;

import com.acme.salary.seed.EmployeeGenerator.SeedEmployee;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds the database with {@code app.seed.count} employees on start-up, but only when the table is empty,
 * so restarting the app never duplicates or overwrites HR's edits.
 *
 * <p>Uses a JDBC batch insert in one transaction: 10,000 rows insert in well under a second, whereas
 * saving entities one by one through JPA would issue 10,000 separate round trips.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class SeedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedRunner.class);
    private static final long SEED = 42L;
    private static final String INSERT = "INSERT INTO employee "
            + "(employee_code, full_name, email, job_title, department, country_code, salary, hire_date) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private final JdbcTemplate jdbc;
    private final int count;

    public SeedRunner(JdbcTemplate jdbc, @Value("${app.seed.count:10000}") int count) {
        this.jdbc = jdbc;
        this.count = count;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Long existing = jdbc.queryForObject("SELECT COUNT(*) FROM employee", Long.class);
        if (existing != null && existing > 0) {
            log.info("Seed skipped: {} employees already present", existing);
            return;
        }
        long start = System.currentTimeMillis();
        List<SeedEmployee> employees = new EmployeeGenerator(SEED, LocalDate.now()).generate(count);
        jdbc.batchUpdate(INSERT, employees, 1_000, (ps, e) -> {
            ps.setString(1, e.employeeCode());
            ps.setString(2, e.fullName());
            ps.setString(3, e.email());
            ps.setString(4, e.jobTitle());
            ps.setString(5, e.department());
            ps.setString(6, e.country().name());
            ps.setBigDecimal(7, e.salary());
            ps.setString(8, e.hireDate().toString());
        });
        log.info("Seeded {} employees in {} ms", employees.size(), System.currentTimeMillis() - start);
    }
}
