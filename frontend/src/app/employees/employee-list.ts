import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { rxResource, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { ApiService } from '../core/api.service';
import { Employee, EmployeeQuery, Page } from '../core/models';
import { MoneyPipe } from '../core/money.pipe';
import { ConfirmDialog, ConfirmData } from '../shared/confirm-dialog';
import { EmployeeFormData, EmployeeFormDialog } from './employee-form-dialog';

const EMPTY_PAGE: Page<Employee> = { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 };

/**
 * The employee directory. Every filter, sort and page change becomes one API call; the browser never
 * holds more than one page of the 10,000 employees.
 */
@Component({
  selector: 'app-employee-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatProgressBarModule,
    MoneyPipe,
  ],
  templateUrl: './employee-list.html',
  styleUrl: './employee-list.scss',
})
export class EmployeeList {
  private readonly api = inject(ApiService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly columns = ['employeeCode', 'fullName', 'jobTitle', 'department', 'country', 'salary', 'hireDate', 'actions'];
  protected readonly searchControl = new FormControl('', { nonNullable: true });

  protected readonly search = signal('');
  protected readonly country = signal('');
  protected readonly department = signal('');
  protected readonly jobTitle = signal('');
  protected readonly page = signal(0);
  protected readonly size = signal(20);
  protected readonly sortBy = signal('fullName');
  protected readonly direction = signal<'asc' | 'desc'>('asc');

  protected readonly query = computed<EmployeeQuery>(() => ({
    search: this.search(),
    country: this.country(),
    department: this.department(),
    jobTitle: this.jobTitle(),
    page: this.page(),
    size: this.size(),
    sortBy: this.sortBy(),
    direction: this.direction(),
  }));

  protected readonly employees = rxResource({
    params: () => this.query(),
    stream: ({ params }) => this.api.listEmployees(params),
    defaultValue: EMPTY_PAGE,
  });

  protected readonly meta = rxResource({
    stream: () => this.api.meta(),
    defaultValue: { countries: [], departments: [], jobTitles: [] },
  });

  protected readonly hasFilters = computed(
    () => !!(this.search() || this.country() || this.department() || this.jobTitle()),
  );

  constructor() {
    // Wait for the user to stop typing before searching, instead of one request per keystroke.
    this.searchControl.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed())
      .subscribe((value) => this.applyFilter(() => this.search.set(value.trim())));
  }

  protected setCountry(value: string): void {
    this.applyFilter(() => this.country.set(value));
  }

  protected setDepartment(value: string): void {
    this.applyFilter(() => this.department.set(value));
  }

  protected setJobTitle(value: string): void {
    this.applyFilter(() => this.jobTitle.set(value));
  }

  protected clearFilters(): void {
    this.searchControl.setValue('', { emitEvent: false });
    this.applyFilter(() => {
      this.search.set('');
      this.country.set('');
      this.department.set('');
      this.jobTitle.set('');
    });
  }

  protected onSort(sort: Sort): void {
    this.sortBy.set(sort.direction ? sort.active : 'fullName');
    this.direction.set(sort.direction === 'desc' ? 'desc' : 'asc');
    this.page.set(0);
  }

  protected onPage(event: PageEvent): void {
    this.page.set(event.pageIndex);
    this.size.set(event.pageSize);
  }

  protected add(): void {
    this.openForm({ meta: this.meta.value() }, 'Employee added');
  }

  protected edit(employee: Employee): void {
    this.openForm({ employee, meta: this.meta.value() }, 'Changes saved');
  }

  protected remove(employee: Employee): void {
    const data: ConfirmData = {
      title: 'Delete employee?',
      message: `${employee.fullName} (${employee.employeeCode}) will be removed permanently.`,
      confirmLabel: 'Delete',
    };
    this.dialog
      .open(ConfirmDialog, { data })
      .afterClosed()
      .subscribe((confirmed) => {
        if (!confirmed) {
          return;
        }
        this.api.deleteEmployee(employee.id).subscribe({
          next: () => {
            this.snackBar.open(`${employee.fullName} deleted`, 'OK', { duration: 3000 });
            this.employees.reload();
          },
          error: () => this.snackBar.open('Could not delete. Please try again.', 'OK', { duration: 4000 }),
        });
      });
  }

  private openForm(data: EmployeeFormData, message: string): void {
    this.dialog
      .open(EmployeeFormDialog, { data, autoFocus: 'first-tabbable' })
      .afterClosed()
      .subscribe((saved?: Employee) => {
        if (saved) {
          this.snackBar.open(`${message}: ${saved.fullName}`, 'OK', { duration: 3000 });
          this.employees.reload();
          this.meta.reload();
        }
      });
  }

  /** Any filter change goes back to page 1, otherwise the user can land on an empty page. */
  private applyFilter(change: () => void): void {
    change();
    this.page.set(0);
  }
}
