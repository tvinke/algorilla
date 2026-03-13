function collectNames(users) {
    const names = [];
    for (const user of users) {
        names.push(user.name);
    }
    return names;
}
