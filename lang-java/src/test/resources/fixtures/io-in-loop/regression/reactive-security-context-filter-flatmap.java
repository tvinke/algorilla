class ReactiveSecurityContextFilterFlatMap {
    Mono<User> load(SecurityService service) {
        return ReactiveSecurityContextHolder.getContext()
            .filter(ctx -> ctx.getAuthentication() != null)
            .flatMap(ctx -> service.fetchUser(ctx.getAuthentication().getName()));
    }

    static class ReactiveSecurityContextHolder {
        static Mono<SecurityContext> getContext() {
            return null;
        }
    }

    interface Mono<T> {
        Mono<T> filter(Predicate<T> predicate);

        <R> Mono<R> flatMap(Mapper<T, Mono<R>> mapper);
    }

    interface Predicate<T> {
        boolean test(T value);
    }

    interface Mapper<T, R> {
        R apply(T value);
    }

    interface SecurityService {
        Mono<User> fetchUser(String username);
    }

    interface SecurityContext {
        Authentication getAuthentication();
    }

    interface Authentication {
        String getName();
    }

    static class User {}
}
