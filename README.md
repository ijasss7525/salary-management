# ACME Salary Management

[![CI](https://github.com/ijasss7525/salary-management/actions/workflows/ci.yml/badge.svg)](https://github.com/ijasss7525/salary-management/actions/workflows/ci.yml)

A web app that replaces ACME's salary spreadsheets. It lets the **HR Manager** maintain salaries for 10,000 employees across 8 countries and answer *"how does the org pay people?"*

- **Live app:** https://acme-salary-management.onrender.com
  (free hosting: after 15 idle minutes the first request takes about a minute to wake the server. Data resets to the 10,000-employee seed on each restart.)
- **Video demo (67 s):** [docs/demo/demo.mp4](docs/demo/demo.mp4)

| Employees | Pay insights |
|---|---|
| Search, filter by country/department/title, sort, page; add, edit, delete with validation | Org overview, pay by country (local currency), pay by job title with range bars and pay-band flags |

## Documents

| | |
|---|---|
| [docs/REQUIREMENTS.md](docs/REQUIREMENTS.md) | One-page requirements: goal, scope, features, what was left out and why. Written first. |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Diagram, packages, API, data model |
| [docs/DECISIONS.md](docs/DECISIONS.md) | Product and technical trade-offs, performance numbers, testing strategy |
| [docs/AI_WORKFLOW.md](docs/AI_WORKFLOW.md) | How AI tools were used: prompts, corrections, verification |

## Tech

**Backend:** Java 21, Spring Boot 3.5 (Web, Data JPA, Validation), SQLite, JUnit 5, Mockito, AssertJ
**Frontend:** Angular 22 (standalone, signals, `rxResource`, zoneless, OnPush), Angular Material, Vitest
**Delivery:** GitHub Actions (tests, Docker smoke test, runnable jar), Docker, Render

## Run it locally

Prerequisites: Java 21, Maven 3.9, Node 22.

```bash
# API on http://localhost:8080 (creates salary.db and seeds 10,000 employees on first start)
cd backend
mvn spring-boot:run

# UI on http://localhost:4200 (proxies /api to :8080)
cd frontend
npm ci
npm start
```

Or as one container (UI and API on port 8080):

```bash
docker build -t salary-management .
docker run -p 8080:8080 salary-management
```

Or with the jar that CI builds on every push: download `salary-management.jar` from the [Latest build](https://github.com/ijasss7525/salary-management/releases/tag/build-latest) release and run `java -jar salary-management.jar`.

## Tests

```bash
cd backend && mvn verify                       # 33 tests: unit, web slice, JPA on real SQLite, end-to-end
cd frontend && npx ng test --watch=false       # 15 tests
```

## Seed data

`SeedRunner` inserts 10,000 employees the first time the app starts with an empty table (JDBC batch, about 0.3 s). Restarts never re-seed or overwrite edits. The data is reproducible (fixed random seed). Salaries come from a role's US base, scaled by a country factor with ±15–20% spread, then converted to local currency.

Configuration: `app.seed.enabled`, `app.seed.count`, `SALARY_DB_PATH`, `PORT`.

## Re-recording the demo

With the app running on `:8080`: `python3 docs/demo/record_demo.py` (needs Playwright for Python). It drives the real UI and doubles as a smoke test.
