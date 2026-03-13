function processItems(items, validator) {
    const results = [];
    for (const item of items) {
        const ok = validator.validate(item);
        if (ok) {
            results.push(item);
        }
    }
    return results;
}
