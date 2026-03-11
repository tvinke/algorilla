// Negative: TypeScript typed Map parameter -- should NOT be flagged
function resolveValues(keys: string[], lookup: Map<string, number>): number[] {
  const results: number[] = [];
  for (const key of keys) {
    const val = lookup.get(key);
    if (val !== undefined) {
      results.push(val);
    }
  }
  return results;
}
