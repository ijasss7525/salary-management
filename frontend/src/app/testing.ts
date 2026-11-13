import { Employee, Meta } from './core/models';

export const META: Meta = {
  countries: [
    { code: 'IN', name: 'India', currency: 'INR' },
    { code: 'US', name: 'United States', currency: 'USD' },
  ],
  departments: ['Engineering', 'Finance'],
  jobTitles: ['Accountant', 'Software Engineer'],
};

export const ASHA: Employee = {
  id: 1,
  employeeCode: 'EMP00001',
  fullName: 'Asha Rao',
  email: 'asha.rao.1@acme.com',
  jobTitle: 'Software Engineer',
  department: 'Engineering',
  country: 'IN',
  countryName: 'India',
  currency: 'INR',
  salary: 2400000,
  hireDate: '2024-04-01',
};
