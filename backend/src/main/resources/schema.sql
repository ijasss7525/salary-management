CREATE TABLE IF NOT EXISTS employee (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_code TEXT    NOT NULL UNIQUE,
    full_name     TEXT    NOT NULL,
    email         TEXT    NOT NULL UNIQUE,
    job_title     TEXT    NOT NULL,
    department    TEXT    NOT NULL,
    country_code  TEXT    NOT NULL,
    salary        NUMERIC NOT NULL CHECK (salary > 0),
    hire_date     TEXT    NOT NULL
);

-- Indexes for the filters and default sort used by the employee directory.
CREATE INDEX IF NOT EXISTS idx_employee_country    ON employee (country_code);
CREATE INDEX IF NOT EXISTS idx_employee_department ON employee (department);
CREATE INDEX IF NOT EXISTS idx_employee_job_title  ON employee (job_title);
CREATE INDEX IF NOT EXISTS idx_employee_full_name  ON employee (full_name);
