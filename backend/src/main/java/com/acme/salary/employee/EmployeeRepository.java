package com.acme.salary.employee;

import com.acme.salary.country.Country;
import com.acme.salary.insights.SalaryRow;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByEmployeeCodeIgnoreCase(String employeeCode);

    boolean existsByEmployeeCodeIgnoreCaseAndIdNot(String employeeCode, Long id);

    @Query("select distinct e.department from Employee e order by e.department")
    List<String> findDistinctDepartments();

    @Query("select distinct e.jobTitle from Employee e order by e.jobTitle")
    List<String> findDistinctJobTitles();

    /** Only the four columns the insights need, so 10,000 rows stay cheap to load. */
    @Query("select new com.acme.salary.insights.SalaryRow(e.country, e.jobTitle, e.department, e.salary) from Employee e")
    List<SalaryRow> findAllSalaryRows();

    @Query("select new com.acme.salary.insights.SalaryRow(e.country, e.jobTitle, e.department, e.salary) "
            + "from Employee e where e.country = :country")
    List<SalaryRow> findSalaryRowsByCountry(Country country);
}
