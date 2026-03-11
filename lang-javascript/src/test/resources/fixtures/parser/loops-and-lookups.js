function processItems(items) {
    items.forEach(item => {
        console.log(item);
    });

    for (let i = 0; i < items.length; i++) {
        console.log(items[i]);
    }

    while (items.length > 0) {
        items.pop();
    }
}

const filterActive = (users) => {
    return users.filter(u => u.active);
};

function searchItem(items, target) {
    if (items.includes(target)) {
        return items.find(i => i.id === target);
    }
    return items.indexOf(target);
}

function sortAndPick(items) {
    items.sort((a, b) => a - b);
    return items[0];
}

function filterBySmallSet(items) {
    return items.filter(item => ['active', 'pending', 'shipped'].includes(item.status));
}

function filterByVariable(items, codes) {
    return items.filter(item => codes.includes(item.code));
}
