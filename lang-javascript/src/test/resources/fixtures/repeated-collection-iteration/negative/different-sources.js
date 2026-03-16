function mergeLists(users, orders) {
    const names = [];
    for (const user of users) {
        names.push(user.name);
    }

    const totals = [];
    for (const order of orders) {
        totals.push(order.amount);
    }

    return { names, totals };
}
