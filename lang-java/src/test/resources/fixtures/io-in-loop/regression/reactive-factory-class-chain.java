class ReactiveFactoryClassChain {
    // ReactiveSecurityContextHolder.getContext() returns Mono — .map().flatMap() is a
    // reactive chain, not iteration. IO call inside flatMap should NOT be flagged.
    Object loadUser(SecurityService service, ReactiveClient client) {
        return ReactiveSecurityContextHolder.getContext()
            .map(ctx -> ctx.getAuthentication().getName())
            .flatMap(username -> client.fetch(username));
    }

    // Same with .filter() intermediate
    Object loadFiltered(SecurityService service, ReactiveClient client) {
        return ReactiveSecurityContextHolder.getContext()
            .filter(ctx -> ctx.getAuthentication() != null)
            .flatMap(ctx -> client.fetch(ctx.getAuthentication().getName()));
    }

    static class ReactiveSecurityContextHolder {
        static Mono<SecurityContext> getContext() { return null; }
    }

    interface Mono<T> {
        <R> Mono<R> map(Mapper<T, R> mapper);
        Mono<T> filter(Predicate<T> predicate);
        <R> Mono<R> flatMap(Mapper<T, Mono<R>> mapper);
    }

    interface Mapper<T, R> { R apply(T value); }
    interface Predicate<T> { boolean test(T value); }
    interface SecurityService { Object fetchUser(String name); }
    interface ReactiveClient { Object fetch(String id); }
    interface SecurityContext { Authentication getAuthentication(); }
    interface Authentication { String getName(); }
}
