function processOrders(orders) {
    let total = 0;
    for (const order of orders) {
        total += order.amount;
    }

    const labels = [];
    for (const order of orders) {
        labels.push(order.label);
    }

    return { total, labels };
}
