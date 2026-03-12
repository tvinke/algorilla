// Negative: TypeScript typed Set -- should NOT be flagged
function filterActive(users: User[], activeIds: Set<string>): User[] {
  const result: User[] = [];
  for (const user of users) {
    if (activeIds.has(user.id)) {
      result.push(user);
    }
  }
  return result;
}
