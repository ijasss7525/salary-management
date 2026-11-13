# Decisions, trade-offs and performance

## Product decisions

**1. Salaries stay in local currency.** HR records salaries in the currency people are paid in. Converting everything to USD would make every number on screen slightly wrong and change daily. So per-country and per-role statistics are always in one local currency, and only the org-wide overview converts to USD — using a **static reference rate table** in `Country.java`, labelled "≈ USD" in the UI.
*Trade-off:* the USD overview is approximate. Live FX was left out on purpose (external dependency, numbers that move, harder to test).

**2. Median next to average.** Pay data is skewed; a few senior salaries pull the average up. The median is what a "typical" person earns, so it is visually emphasised. A unit test (`medianResistsOutliersWhileAverageDoesNot`) pins this behaviour.

**3. Pay-band flags.** For each role in a country, people paid more than 20% below or above the role's median are counted. This turns "how does the org pay people?" into an action list: *who should HR review first?* The 20% tolerance is a named constant (`InsightsService.BAND_TOLERANCE`).

**4. Country owns the currency.** Currency is not a separate field the user can get wrong; it is derived from the country (`Country` enum). Unsupported countries are rejected with 400.

**5. HR's employee code is kept and unique**, and emails are normalised to lower case so `A@acme.com` and `a@acme.com` cannot both exist. The service checks duplicates to give a clear message; database `UNIQUE` constraints are the safety net for races.

**What was left out and why** is in [REQUIREMENTS.md](REQUIREMENTS.md).

## Technical decisions

| Decision | Why | Trade-off / when to revisit |
|---|---|---|
| **Spring Boot 3.5 + Java 21** | Matches the role; mature validation, JPA, testing slices. | — |
| **SQLite** (as suggested) | Zero setup, single file, fast for 10k rows. | Single writer. One pooled connection avoids `database is locked`. For many concurrent HR users → PostgreSQL (only the dialect/URL change; JPA code stays). |
| **Schema in `schema.sql`, Hibernate `ddl-auto=none`** | Explicit, reviewable DDL incl. indexes and `CHECK (salary > 0)`. | A migration tool (Flyway) once the schema evolves. |
| **Dates as ISO text** | SQLite has no DATE type; ISO strings sort correctly. Converter keeps `LocalDate` in Java. | — |
| **Filtering/paging in SQL (JPA Specifications)** | The browser never receives 10,000 rows. | — |
| **Sort-field whitelist + page size cap (100)** | Bad input can't cause errors or large queries. | — |
| **Insights computed in Java from a 4-column projection** | SQLite has no `MEDIAN`; loading 10k × 4 small columns is < 1 MB and ~10–30 ms. Pure functions are easy to unit-test. | At 100× the data: min/avg/max/count via SQL `GROUP BY`, median via window functions or a pre-aggregated table refreshed on write. |
| **Errors as RFC 7807 Problem Details** with per-field `errors` | The UI maps field errors onto the matching inputs. | — |
| **Single deployable (API serves the Angular build)** | One URL, no CORS, simple hosting. | Separate CDN hosting for the UI if traffic grows. |
| **Angular: signals + `rxResource`, OnPush, zoneless** | Current Angular best practice; each filter change = exactly one request; previous request cancelled automatically. | — |
| **Angular Material** | Accessible, consistent components; matches "component library of your choice". | — |
| **Icons self-hosted** (`material-icons` npm) | No third-party CDN call from an HR tool. | — |

## Performance considerations

- **Seeding:** JDBC batch insert (batches of 1,000) in one transaction → 10,000 rows in **~250–350 ms** on start-up. Saving through JPA one by one would issue 10,000 round trips.
- **Directory queries:** indexes on `country_code`, `department`, `job_title`, `full_name`; paged with a stable secondary sort on `id` so rows never repeat or vanish between pages.
- **Search** is debounced (300 ms) in the UI and matches name, email or code with case-insensitive `LIKE`. With 10k rows a leading-wildcard `LIKE` scans the table in a few milliseconds; at 1M rows this would move to SQLite FTS5 or PostgreSQL trigram indexes.
- **Measured locally** with 10,000 employees (warm JVM): directory page 13–20 ms, insights by country ≈ 37 ms, overview ≈ 25 ms, job titles for one country ≈ 50 ms.
- **Frontend:** routes are lazy-loaded; initial bundle ≈ 93 kB gzipped.

## Testing strategy

Fast, deterministic tests at the level where each rule lives:

| Layer | Tests | What they prove |
|---|---|---|
| Pure logic | `SalaryStatsTest`, `EmployeeGeneratorTest` | Median/average maths, rounding, outlier behaviour; seed is reproducible, unique and valid |
| Service (Mockito) | `EmployeeServiceTest`, `InsightsServiceTest` | Normalisation, duplicate rules, not-found, USD conversion, pay-band flags |
| Web slice | `EmployeeControllerTest` (`@WebMvcTest`) | HTTP contract: 400 with field errors, 404, 409, paging clamps, filter binding |
| Persistence slice | `EmployeeRepositoryTest` (`@DataJpaTest`, real in-memory SQLite) | Specifications, projections, date/decimal round-trip on the real database |
| End-to-end | `ApplicationIntegrationTest` | Whole app on a seeded DB: list, insights, create → duplicate → update → delete |
| Frontend (Vitest) | 5 spec files | API params, money formatting, list paging/filter reset, form validation & server-error mapping, insights selection |

The seed uses a fixed random seed and tests use fixed dates, so nothing depends on the clock or on luck. CI runs all of it on every push, plus a Docker build + container smoke test.
