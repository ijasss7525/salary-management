# How AI tools were used

The assessment asks for agentic AI to be used intentionally. This file records how: the prompts, what the agent was told to hold to, where its output had to be corrected, and how correctness was checked.

**Tool:** Claude (Anthropic), used as an agentic coding assistant with a shell, the GitHub repo and a browser. Requirements, design choices and review are recorded below and in the other docs.

## 1. Prompts

**Prompt 1: the brief**
> Here is the Incubyte Salary Management assessment (full text pasted). Build the complete solution for the Java/Angular role: Spring Boot backend, Angular UI, SQLite, a seed script for 10,000 employees, meaningful tests, incremental commits, a deployment and a video demo. Write the one-page requirements document first. Then walk me through the code so I understand every part.

**Prompt 2: working rules the agent followed** (summarised from the session)
- Requirements doc first, committed before any code.
- Keep it simple and correct rather than impressive: "good engineering judgment, not the most complex system".
- Small commits that each leave the build working, with messages that explain *why*.
- No currency mixing: statistics only within one currency; anything converted must be labelled approximate.
- Every rule gets a fast, deterministic test at the lowest layer where it lives.
- Nothing is "done" until CI is green and the running app has been clicked through.

## 2. Where the AI's first attempt was wrong or needed a decision

| Situation | What happened | Resolution |
|---|---|---|
| Build environment | The agent's sandbox could not reach Maven Central, so it could not compile Java locally. | GitHub Actions became the compiler and test runner from the first commit; nothing was merged on an assumption of "it should compile". The backend passed on its first CI run, and every later push was verified the same way. |
| SQLite types | SQLite has no `DATE` type, and date handling differs between drivers. | An explicit `LocalDate ↔ ISO text` converter, plus a repository test that round-trips dates and decimals through real SQLite. |
| Angular zoneless tests | The first component tests used `fixture.whenStable()`, which waits for open HTTP requests, so they hung until timeout. | A small `settle()` helper (`TestBed.tick()`) is used instead. All 15 frontend tests run in about 1 s. |
| Demo exposed UX bugs | Recording the demo showed that (a) the "email already in use" banner stayed visible after the email was fixed, and (b) the header said "1 people". | Both fixed, each with a test (commit `fix(frontend): clear stale save error…`). |
| Spring `Page` JSON | Returning Spring Data's `Page` directly gives an unstable JSON shape (Spring warns about it). | Added its own `PageResponse` record. |
| Currency in stats | A naive "average salary" across all employees would mix INR with USD. | Stats are grouped by country; only the overview converts to USD, through static rates. |

## 3. How correctness was verified

1. **33 backend tests** (unit, web slice, JPA slice on real SQLite, full end-to-end) and **15 frontend tests**, run in CI on every push.
2. **The Docker image is built and smoke-tested in CI**: the container starts, seeds 10,000 rows and serves both the API and the UI.
3. **Manual end-to-end run** of the CI-built jar, with timings recorded (see DECISIONS.md → Performance).
4. **Scripted demo** (`docs/demo/record_demo.py`, Playwright) drives the real UI: search, filter, sort, validation, duplicate (409), create, edit, delete, insights. It doubles as a smoke test.

## 4. My review notes

<!-- Jaspreet: after the walkthrough, add your own notes here: what you would change, what you'd do next. -->
