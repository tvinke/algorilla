async function loadUserProfiles(userIds) {
    const profiles = [];
    for (const id of userIds) {
        const response = await fetch(`/api/users/${id}`);
        profiles.push(await response.json());
    }
    return profiles;
}
