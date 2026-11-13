import { Pipe, PipeTransform } from '@angular/core';
import { formatMoney } from './money';

/** Usage: {{ employee.salary | money: employee.currency }} */
@Pipe({ name: 'money' })
export class MoneyPipe implements PipeTransform {
  transform(amount: number | null | undefined, currency: string): string {
    return formatMoney(amount, currency);
  }
}
