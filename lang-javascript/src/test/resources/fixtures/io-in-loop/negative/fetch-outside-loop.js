async function loadAllUsers() {
    const response = await fetch('/api/users');
    const users = await response.json();
    for (const user of users) {
        console.log(user.name);
    }
    return users;
}
