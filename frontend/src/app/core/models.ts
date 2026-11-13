/** Shapes returned by the Spring Boot API. Salaries are annual gross in the employee's local currency. */

export interface Employee {
  id: number;
  employeeCode: string;
  fullName: string;
  email: string;
  jobTitle: string;
  department: string;
  country: string;
  countryName: string;
  currency: string;
  salary: number;
  hireDate: string;
}

export type EmployeeRequest = Omit<Employee, 'id' | 'countryName' | 'currency'>;

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface EmployeeQuery {
  search: string;
  country: string;
  department: string;
  jobTitle: string;
  page: number;
  size: number;
  sortBy: string;
  direction: 'asc' | 'desc';
}

export interface CountryOption {
  code: string;
  name: string;
  currency: string;
}

export interface Meta {
  countries: CountryOption[];
  departments: string[];
  jobTitles: string[];
}

export interface SalaryStats {
  headcount: number;
  min: number;
  median: number;
  average: number;
  max: number;
  total: number;
}

export interface Overview {
  headcount: number;
  countries: number;
  jobTitles: number;
  totalPayrollUsd: number;
  averageSalaryUsd: number;
}

export interface CountryInsight {
  country: string;
  countryName: string;
  currency: string;
  totalUsd: number;
  stats: SalaryStats;
}

export interface JobTitleInsight {
  jobTitle: string;
  department: string;
  stats: SalaryStats;
  belowBand: number;
  aboveBand: number;
}

/** RFC 7807 problem details returned by the API on errors. */
export interface ApiProblem {
  status: number;
  detail?: string;
  errors?: Record<string, string>;
}
