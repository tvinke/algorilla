import java.util.function.Predicate
import java.util.regex.Pattern

class PredicateMatchesRegression {

    // Predicate.matches() is not a regex operation
    fun filterWithPredicate(items: List<String>, predicate: Predicate<String>) {
        for (item in items) {
            if (predicate.matches(item)) {
                println(item)
            }
        }
    }

    // Pattern.matcher().matches() — already compiled
    fun matchWithPattern(items: List<String>, pattern: Pattern) {
        for (item in items) {
            val matcher = pattern.matcher(item)
            if (matcher.matches()) {
                println(item)
            }
        }
    }
}
