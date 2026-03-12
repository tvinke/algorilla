// Positive: array.includes() inside a loop -- should be flagged
function processItems(items, targets) {
  for (const item of items) {
    if (targets.includes(item.name)) {
      console.log(item);
    }
  }
}
