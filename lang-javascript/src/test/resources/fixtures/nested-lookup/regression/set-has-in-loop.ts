function filterAllowed(items: string[], allowed: Set<string>): string[] {
  const result: string[] = [];
  for (const item of items) {
    if (allowed.has(item)) {
      result.push(item);
    }
  }
  return result;
}
