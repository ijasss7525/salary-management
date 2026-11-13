import { formatMoney } from './money';

describe('formatMoney', () => {
  it('formats in the given currency without decimals', () => {
    expect(formatMoney(130000, 'USD')).toBe('$130,000');
    expect(formatMoney(85000.6, 'EUR')).toBe('€85,001');
  });

  it('uses Indian digit grouping for rupees', () => {
    expect(formatMoney(2400000, 'INR')).toBe('₹24,00,000');
  });

  it('shows a dash for missing values', () => {
    expect(formatMoney(null, 'USD')).toBe('–');
  });
});
