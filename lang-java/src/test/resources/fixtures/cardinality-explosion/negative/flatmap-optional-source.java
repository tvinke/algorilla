import java.util.Optional;
import java.util.List;

class FlatMapOptionalSource {
    List<Pair> combine(Container raw, List<Other> others) {
        Optional<Container> container = Optional.ofNullable(raw);
        return container.flatMap(c -> others.stream().map(o -> new Pair(c, o)));
    }
}
