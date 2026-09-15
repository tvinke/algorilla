import java.util.List;

class FlatMapMonoSource {
    Mono<Pair> combine(Container raw, List<Other> others) {
        Mono<Container> container = Mono.just(raw);
        return container.flatMap(c -> others.stream().map(o -> new Pair(c, o)));
    }
}
