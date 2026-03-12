// Negative: new Set() variable -- should NOT be flagged (O(1) lookup)
function processItems(items) {
  const allowed = new Set(["admin", "editor", "viewer"]);
  for (const item of items) {
    if (allowed.has(item.role)) {
      console.log(item);
    }
  }
}
