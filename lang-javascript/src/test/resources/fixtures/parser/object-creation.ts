function serialize(data) {
    const mapper = new ObjectMapper();
    return mapper.writeValueAsString(data);
}

function createItems() {
    const item = new Item();
    const date = new Date();
    return [item, date];
}
