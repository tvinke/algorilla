package com.github.tvinke.algorilla.engine

/**
 * Detects frameworks from import statements. Maps import package prefixes
 * to framework names for use in suggestions and output labeling.
 */
public object FrameworkDetector {
    private val IMPORT_PREFIX_TO_FRAMEWORK: Map<String, String> =
        mapOf(
            "org.springframework" to "Spring",
            "javax.persistence" to "JPA",
            "jakarta.persistence" to "JPA",
            "org.hibernate" to "Hibernate",
            "io.quarkus" to "Quarkus",
            "io.smallrye.mutiny" to "SmallRye Mutiny",
            "reactor.core" to "Project Reactor",
            "io.reactivex" to "RxJava",
            "io.micronaut" to "Micronaut",
            "io.grpc" to "gRPC",
            "io.r2dbc" to "R2DBC",
            "org.jooq" to "jOOQ",
            "kotlinx.coroutines" to "Kotlin Coroutines",
            "io.ktor" to "Ktor",
        )

    /**
     * Given a set of import strings, returns detected framework names.
     */
    public fun detect(imports: Set<String>): Set<String> {
        val detected = mutableSetOf<String>()
        for (imp in imports) {
            for ((prefix, framework) in IMPORT_PREFIX_TO_FRAMEWORK) {
                if (imp.contains(prefix)) {
                    detected.add(framework)
                }
            }
        }
        return detected
    }
}
