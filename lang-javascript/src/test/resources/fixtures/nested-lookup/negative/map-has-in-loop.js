// Negative: new Map() variable -- should NOT be flagged (O(1) lookup)
function lookupUsers(ids, userMap) {
  const cache = new Map();
  for (const id of ids) {
    if (cache.has(id)) {
      continue;
    }
    cache.set(id, userMap.get(id));
  }
}
