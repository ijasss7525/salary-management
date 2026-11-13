import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { ApiService } from '../core/api.service';
import { ApiProblem, Employee, EmployeeRequest, Meta } from '../core/models';

export interface EmployeeFormData {
  employee?: Employee;
  meta: Meta;
}

const today = () => new Date().toISOString().slice(0, 10);

@Component({
  selector: 'app-employee-form-dialog',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule],
  templateUrl: './employee-form-dialog.html',
  styleUrl: './employee-form-dialog.scss',
})
export class EmployeeFormDialog {
  private readonly api = inject(ApiService);
  private readonly dialogRef = inject(MatDialogRef<EmployeeFormDialog, Employee>);
  protected readonly data = inject<EmployeeFormData>(MAT_DIALOG_DATA);

  protected readonly isEdit = !!this.data.employee;
  protected readonly saving = signal(false);
  protected readonly serverError = signal<string | null>(null);
  protected readonly maxDate = today();

  protected readonly form = inject(NonNullableFormBuilder).group({
    employeeCode: [this.data.employee?.employeeCode ?? '', [Validators.required, Validators.maxLength(20)]],
    fullName: [this.data.employee?.fullName ?? '', [Validators.required, Validators.maxLength(120)]],
    email: [this.data.employee?.email ?? '', [Validators.required, Validators.email]],
    jobTitle: [this.data.employee?.jobTitle ?? '', [Validators.required, Validators.maxLength(80)]],
    department: [this.data.employee?.department ?? '', [Validators.required, Validators.maxLength(80)]],
    country: [this.data.employee?.country ?? '', Validators.required],
    salary: [this.data.employee?.salary ?? (null as unknown as number), [Validators.required, Validators.min(1)]],
    hireDate: [this.data.employee?.hireDate ?? today(), Validators.required],
  });

  private readonly country = toSignal(this.form.controls.country.valueChanges, {
    initialValue: this.form.controls.country.value,
  });

  /** The salary field shows the currency of the selected country, so HR always knows what they are typing. */
  protected readonly currency = computed(
    () => this.data.meta.countries.find((c) => c.code === this.country())?.currency ?? '',
  );

  constructor() {
    // Once the user edits the form, an earlier "already in use" message no longer applies.
    this.form.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => this.serverError.set(null));
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.serverError.set(null);
    const request: EmployeeRequest = this.form.getRawValue();
    const call = this.data.employee
      ? this.api.updateEmployee(this.data.employee.id, request)
      : this.api.createEmployee(request);

    call.subscribe({
      next: (saved) => this.dialogRef.close(saved),
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        this.showServerError(err.error as ApiProblem | null, err.status);
      },
    });
  }

  /** Field errors from the API are attached to the matching inputs; anything else is shown as a banner. */
  private showServerError(problem: ApiProblem | null, status: number): void {
    const fieldErrors = problem?.errors ?? {};
    for (const [field, message] of Object.entries(fieldErrors)) {
      const control = this.form.get(field);
      control?.setErrors({ server: message });
      control?.markAsTouched();
    }
    if (Object.keys(fieldErrors).length === 0) {
      this.serverError.set(problem?.detail ?? `Could not save (HTTP ${status}). Please try again.`);
    }
  }
}
