import java.util.function.Predicate

class PredicateMatchesRegression {

    // Predicate.matches() is not a regex operation
    void filterWithPredicate(List<String> items, Predicate<String> predicate) {
        for (String item : items) {
            if (predicate.matches(item)) {
                println(item)
            }
        }
    }
}
