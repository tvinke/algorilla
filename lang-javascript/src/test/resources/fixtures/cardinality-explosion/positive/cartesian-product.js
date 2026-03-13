function buildPairs(users, roles) {
    const pairs = [];
    for (const user of users) {
        for (const role of roles) {
            pairs.push({ user, role });
        }
    }
    return pairs;
}
