class ReactiveSecurityContextFlatMap {
    Mono<User> load(SecurityService service) {
        return ReactiveSecurityContextHolder.getContext()
            .flatMap(ctx -> service.fetchUser(ctx.getAuthentication().getName()));
    }

    static class ReactiveSecurityContextHolder {
        static Mono<SecurityContext> getContext() {
            return null;
        }
    }

    interface Mono<T> {
        <R> Mono<R> flatMap(Mapper<T, Mono<R>> mapper);
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
