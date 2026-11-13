# Salary Management – Requirements (one page)

## Goal
Replace ACME's Excel-based salary tracking with a web application that lets the **HR Manager** maintain salary data for ~10,000 employees across several countries and quickly answer *"how does the org pay people?"*

## User
**HR Manager** – one trusted internal user. Comfortable with spreadsheets, not technical. Needs speed, accuracy and answers, not configuration.

## Problems with today's Excel process
1. Finding and editing one employee among 10,000 rows is slow and error-prone.
2. No validation – typos in salary, currency or country silently corrupt reports.
3. Answering pay questions ("What do engineers earn in India vs Germany?", "Who is paid outside the normal range for their role?") needs manual pivot tables every time.

## In scope (MVP)
| # | Feature | Why |
|---|---------|-----|
| 1 | **Employee directory** – server-side paged table, search by name / email / employee code, filter by country, department, job title, sort by any column | Find anyone in seconds among 10k rows |
| 2 | **Add / edit / delete employee** with validation (required fields, positive salary, valid email, unique employee code & email) | Data stays clean, unlike Excel |
| 3 | **Pay overview** – headcount, total annual payroll (in USD), number of countries | One-glance answer for leadership questions |
| 4 | **Pay by country** – headcount, min / median / average / max salary in the **local currency** | Comparing salaries in different currencies is misleading, so each country is shown in its own currency |
| 5 | **Pay by job title within a country** – same statistics per role, plus the pay range | Answers "what does a Software Engineer earn in India?" and makes outliers visible |
| 6 | **Seed script** generating 10,000 realistic, reproducible employees | Required for demo & performance testing |

## Key decisions
- **Salaries are stored as annual gross in the employee's local currency** (as HR records them). Org-wide totals are converted to USD using a **static, documented reference rate table** – good enough for an overview, clearly labelled as approximate.
- **Median shown next to average** – salaries are skewed; the median is the honest "typical" number.
- **Server-side paging, filtering and sorting** – the browser never loads 10,000 rows.

## Deliberately left out (and why)
| Left out | Reason |
|----------|--------|
| Authentication / roles | Single internal user for the MVP; would be added (SSO) before real production use. Keeps the exercise focused on the core problem. |
| Payroll processing, tax, payslips | Different product. The ask is *managing* salary data, not paying people. |
| Live currency conversion | Needs an external provider and makes numbers change daily; static rates are transparent and testable. |
| Salary history / audit trail | Valuable next step (raises, promotions), but adds data-model complexity beyond the time box. |
| Bulk Excel import / export | Very useful for migration – first item for the next iteration – but the seed script covers the data need for now. |
| Charts | Tables with median and range answer the questions precisely; charts are a polish item. |

## Success criteria
- Any employee can be found and edited in under 10 seconds.
- Insight pages answer the example questions without exporting to Excel.
- All pages respond in well under 1 second with 10,000 employees.
- Core logic covered by fast, deterministic unit tests.
