function filterBySubstring(keyword: string): boolean {
  const text: string = "hello world";
  return text.includes(keyword);
}

function checkAll(items: string[], keyword: string): void {
  for (let i = 0; i < items.length; i++) {
    const item: string = items[i];
    if (item.includes(keyword)) {
      console.log(item);
    }
  }
}
