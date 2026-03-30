class ReactiveChainFlatMap {
    // client.fetch() returns Mono<T> — flatMap is 1-to-1, not iteration
    Mono<ContentWrapper> update(ReactiveClient client, String name) {
        return client.fetch(Snapshot.class, name)
            .flatMap(snapshot -> client.fetch(Snapshot.class, snapshot.getBase()));
    }
}
