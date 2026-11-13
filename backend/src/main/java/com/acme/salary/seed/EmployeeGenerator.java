package com.acme.salary.seed;

import com.acme.salary.country.Country;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Generates realistic, reproducible employees. The same seed always produces the same people,
 * which keeps demos and tests deterministic.
 *
 * <p>Salaries start from a US base for the role, are scaled by a country cost-of-labour factor and a
 * +/-15-20% individual spread, then converted into the local currency and rounded to a whole hundred.
 */
public final class EmployeeGenerator {

    public record SeedEmployee(String employeeCode, String fullName, String email, String jobTitle,
                               String department, Country country, BigDecimal salary, LocalDate hireDate) {
    }

    private record Role(String title, String department, int usBaseUsd, int weight) {
    }

    private record CountryProfile(Country country, double labourFactor, int weight) {
    }

    private static final List<Role> ROLES = List.of(
            new Role("Software Engineer", "Engineering", 120_000, 22),
            new Role("Senior Software Engineer", "Engineering", 160_000, 14),
            new Role("Engineering Manager", "Engineering", 195_000, 4),
            new Role("QA Engineer", "Engineering", 95_000, 7),
            new Role("Data Analyst", "Data", 95_000, 6),
            new Role("Product Manager", "Product", 150_000, 5),
            new Role("Product Designer", "Design", 120_000, 4),
            new Role("Sales Executive", "Sales", 85_000, 10),
            new Role("Account Manager", "Sales", 95_000, 6),
            new Role("Customer Support Specialist", "Support", 55_000, 9),
            new Role("HR Generalist", "People", 75_000, 3),
            new Role("Accountant", "Finance", 80_000, 4),
            new Role("Operations Analyst", "Operations", 78_000, 6));

    private static final List<CountryProfile> COUNTRIES = List.of(
            new CountryProfile(Country.IN, 0.25, 35),
            new CountryProfile(Country.US, 1.00, 20),
            new CountryProfile(Country.GB, 0.75, 10),
            new CountryProfile(Country.DE, 0.80, 10),
            new CountryProfile(Country.CA, 0.80, 7),
            new CountryProfile(Country.AU, 0.80, 6),
            new CountryProfile(Country.SG, 0.75, 6),
            new CountryProfile(Country.AE, 0.70, 6));

    private static final String[] FIRST_NAMES = {
            "Aarav", "Aditi", "Amit", "Ananya", "Arjun", "Diya", "Ishaan", "Kavya", "Neha", "Priya", "Rahul", "Riya",
            "Rohan", "Sanya", "Vikram", "Zoya", "James", "Mary", "John", "Patricia", "Robert", "Jennifer", "Michael",
            "Linda", "David", "Emily", "Oliver", "Amelia", "George", "Isla", "Harry", "Ava", "Lukas", "Mia", "Leon",
            "Hannah", "Felix", "Lea", "Noah", "Emma", "Liam", "Chloe", "Ethan", "Olivia", "Wei", "Mei", "Jun", "Hui",
            "Omar", "Fatima", "Ahmed", "Layla", "Yusuf", "Mariam", "Carlos", "Sofia", "Mateo", "Lucia", "Kenji",
            "Yuki"};

    private static final String[] LAST_NAMES = {
            "Sharma", "Verma", "Singh", "Patel", "Gupta", "Reddy", "Iyer", "Nair", "Kapoor", "Mehta", "Smith",
            "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis", "Wilson", "Taylor", "Clark", "Müller",
            "Schmidt", "Schneider", "Fischer", "Weber", "Wagner", "Tan", "Lim", "Lee", "Ng", "Wong", "Khan",
            "Hassan", "Ali", "Rahman", "Haddad", "Garcia", "Martinez", "Lopez", "Silva", "Tanaka", "Sato", "Martin",
            "Thompson", "White", "Harris", "Walker", "Young", "King", "Wright"};

    private static final LocalDate EARLIEST_HIRE = LocalDate.of(2012, 1, 1);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final Random random;
    private final LocalDate today;

    public EmployeeGenerator(long seed, LocalDate today) {
        this.random = new Random(seed);
        this.today = today;
    }

    public List<SeedEmployee> generate(int count) {
        List<SeedEmployee> employees = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            employees.add(next(i));
        }
        return employees;
    }

    private SeedEmployee next(int index) {
        String first = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        String last = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        Role role = pick(ROLES, ROLES.stream().mapToInt(Role::weight).toArray());
        CountryProfile profile = pick(COUNTRIES, COUNTRIES.stream().mapToInt(CountryProfile::weight).toArray());

        String code = String.format(Locale.ROOT, "EMP%05d", index);
        String email = (ascii(first) + "." + ascii(last) + "." + index + "@acme.com").toLowerCase(Locale.ROOT);

        double spread = 0.85 + random.nextDouble() * 0.35; // 0.85 .. 1.20
        double salaryUsd = role.usBaseUsd() * profile.labourFactor() * spread;
        BigDecimal local = BigDecimal.valueOf(salaryUsd)
                .divide(profile.country().usdRate(), 0, RoundingMode.HALF_UP)
                .divide(HUNDRED, 0, RoundingMode.HALF_UP)
                .multiply(HUNDRED);

        long daysRange = today.toEpochDay() - EARLIEST_HIRE.toEpochDay();
        LocalDate hireDate = EARLIEST_HIRE.plusDays((long) (random.nextDouble() * daysRange));

        return new SeedEmployee(code, first + " " + last, email, role.title(), role.department(),
                profile.country(), local, hireDate);
    }

    private <T> T pick(List<T> items, int[] weights) {
        int total = 0;
        for (int w : weights) {
            total += w;
        }
        int roll = random.nextInt(total);
        for (int i = 0; i < items.size(); i++) {
            roll -= weights[i];
            if (roll < 0) {
                return items.get(i);
            }
        }
        return items.get(items.size() - 1);
    }

    private static String ascii(String name) {
        return name.replace("ü", "ue");
    }
}
