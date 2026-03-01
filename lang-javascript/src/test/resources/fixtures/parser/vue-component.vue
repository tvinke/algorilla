<template>
  <div>{{ message }}</div>
</template>

<script lang="ts">
function loadUsers(users) {
    users.forEach(u => {
        console.log(u.name);
    });

    if (users.includes("admin")) {
        return users.filter(u => u.role === "admin");
    }
}
</script>

<style scoped>
div { color: red; }
</style>
