const formatters = new Map<string, Intl.NumberFormat>();

/**
 * Formats an amount in its own currency with no decimals, e.g. "₹24,00,000" or "$130,000".
 * Annual salaries don't need cents, and showing the currency symbol prevents comparing INR with USD by eye.
 */
export function formatMoney(amount: number | null | undefined, currency: string): string {
  if (amount === null || amount === undefined) {
    return '–';
  }
  const locale = currency === 'INR' ? 'en-IN' : 'en-US';
  const key = `${locale}|${currency}`;
  let formatter = formatters.get(key);
  if (!formatter) {
    formatter = new Intl.NumberFormat(locale, {
      style: 'currency',
      currency,
      maximumFractionDigits: 0,
    });
    formatters.set(key, formatter);
  }
  return formatter.format(amount);
}
