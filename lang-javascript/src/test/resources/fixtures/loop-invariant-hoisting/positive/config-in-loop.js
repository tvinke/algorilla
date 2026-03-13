function processItems(items, config) {
    const results = [];
    for (const item of items) {
        const timeout = config.getTimeout();
        results.push(timeout);
    }
    return results;
}
