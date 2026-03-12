function normalizeNames(names) {
    const result = [];
    for (const name of names) {
        result.push(name.toLowerCase());
    }
    return result;
}
