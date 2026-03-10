function greet(name: string, age: number) {
    console.log(name);
}

function lookupUser(users: Set<User>, id: string) {
    return users.has(id);
}

const processItems = (items: Map<string, Item>, filter: boolean) => {
    for (const [key, val] of items) {
        console.log(key);
    }
};

class UserService {
    findById(id: string, cache: Map<string, User>) {
        return cache.get(id);
    }

    constructor(db: Database) {
        this.db = db;
    }
}

function noTypes(a, b, c) {
    return a + b + c;
}

const arrowNoTypes = (x, y) => x + y;

function withDefaults(name = "world", count = 1) {
    return name.repeat(count);
}

function withRest(...args: string[]) {
    return args.join(", ");
}
