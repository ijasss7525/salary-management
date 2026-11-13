import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { FormControl, FormGroup } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { vi } from 'vitest';
import { ASHA, META } from '../testing';
import { EmployeeFormData, EmployeeFormDialog } from './employee-form-dialog';

// The form is protected in the component; tests reach it through this view of the instance.
type DialogInternals = {
  form: FormGroup<{ country: FormControl; salary: FormControl; email: FormControl }>;
  serverError: () => string | null;
  currency: () => string;
  save(): void;
};

describe('EmployeeFormDialog', () => {
  let http: HttpTestingController;
  const close = vi.fn();

  function create(data: EmployeeFormData) {
    TestBed.configureTestingModule({
      imports: [EmployeeFormDialog],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: MAT_DIALOG_DATA, useValue: data },
        { provide: MatDialogRef, useValue: { close } },
      ],
    });
    http = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(EmployeeFormDialog);
    fixture.detectChanges();
    return fixture.componentInstance as unknown as DialogInternals;
  }

  beforeEach(() => close.mockReset());

  it('does not call the API while the form is invalid', () => {
    const dialog = create({ meta: META });

    dialog.save();

    http.expectNone('/api/employees');
    expect(dialog.form.touched).toBe(true);
  });

  it('shows the currency of the chosen country next to the salary', () => {
    const dialog = create({ meta: META });

    dialog.form.controls.country.setValue('US');

    expect(dialog.currency()).toBe('USD');
  });

  it('updates an existing employee and closes with the saved result', () => {
    const dialog = create({ employee: ASHA, meta: META });
    dialog.form.controls.salary.setValue(2600000);

    dialog.save();

    const req = http.expectOne('/api/employees/1');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body.salary).toBe(2600000);
    req.flush({ ...ASHA, salary: 2600000 });
    expect(close).toHaveBeenCalledWith({ ...ASHA, salary: 2600000 });
  });

  it('puts server field errors on the matching inputs', () => {
    const dialog = create({ employee: ASHA, meta: META });

    dialog.save();
    http.expectOne('/api/employees/1').flush(
      { status: 400, detail: 'Some fields are invalid', errors: { email: 'must be a well-formed email address' } },
      { status: 400, statusText: 'Bad Request' },
    );

    expect(dialog.form.controls.email.getError('server')).toBe('must be a well-formed email address');
    expect(close).not.toHaveBeenCalled();
  });

  it('shows a conflict as a banner', () => {
    const dialog = create({ employee: ASHA, meta: META });

    dialog.save();
    http.expectOne('/api/employees/1').flush(
      { status: 409, detail: 'Email asha.rao.1@acme.com is already in use' },
      { status: 409, statusText: 'Conflict' },
    );

    expect(dialog.serverError()).toBe('Email asha.rao.1@acme.com is already in use');
  });
});
