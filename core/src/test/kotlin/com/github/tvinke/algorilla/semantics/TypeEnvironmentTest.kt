package com.github.tvinke.algorilla.semantics

import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.ObjectCreation
import com.github.tvinke.algorilla.model.Parameter
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.model.TypeSource
import com.github.tvinke.algorilla.model.VariableDecl
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TypeEnvironmentTest {
    private val registry = LanguageSemanticsRegistry.loadDefaults()
    private val loc = SourceLocation("Test.java", 1, 1)

    private fun fn(
        parameters: List<Parameter> = emptyList(),
        children: List<com.github.tvinke.algorilla.model.IRNode> = emptyList(),
    ) = FunctionDecl(
        name = "test",
        qualifiedName = "TestClass.test",
        parameters = parameters,
        location = loc,
        children = children,
    )

    private fun param(
        name: String,
        typeName: String,
    ) = Parameter(name, typeName)

    private fun varDecl(
        name: String,
        typeName: String? = null,
        children: List<com.github.tvinke.algorilla.model.IRNode> = emptyList(),
    ) = VariableDecl(name, typeName, location = loc, children = children)

    private fun objectCreation(typeName: String) = ObjectCreation(typeName, loc, emptyList())

    private fun functionCall(
        name: String,
        qualifiedTarget: String? = null,
        arguments: List<com.github.tvinke.algorilla.model.IRNode> = emptyList(),
    ) = FunctionCall(name, qualifiedTarget, arguments, loc, emptyList())

    @Nested
    inner class TypeInference {
        @Test
        fun `should infer DECLARED from parameter type`() {
            val env =
                TypeEnvironment.build(
                    fn(parameters = listOf(param("ids", "Set<String>"))),
                    emptyMap(),
                    Language.JAVA,
                    registry,
                )
            val type = env.typeOf("ids")!!
            type.simpleName shouldBe "Set<String>"
            type.source shouldBe TypeSource.DECLARED
        }

        @Test
        fun `should infer DECLARED from field type`() {
            val env =
                TypeEnvironment.build(
                    fn(),
                    mapOf("cache" to "HashMap<String, Object>"),
                    Language.JAVA,
                    registry,
                )
            val type = env.typeOf("cache")!!
            type.simpleName shouldBe "HashMap<String, Object>"
            type.source shouldBe TypeSource.DECLARED
        }

        @Test
        fun `parameter should override field type`() {
            val env =
                TypeEnvironment.build(
                    fn(parameters = listOf(param("items", "Set<String>"))),
                    mapOf("items" to "List<String>"),
                    Language.JAVA,
                    registry,
                )
            env.typeOf("items")!!.simpleName shouldBe "Set<String>"
        }

        @Test
        fun `should infer CONSTRUCTOR from ObjectCreation`() {
            val env =
                TypeEnvironment.build(
                    fn(children = listOf(varDecl("map", children = listOf(objectCreation("HashMap"))))),
                    emptyMap(),
                    Language.JAVA,
                    registry,
                )
            val type = env.typeOf("map")!!
            type.simpleName shouldBe "HashMap"
            type.source shouldBe TypeSource.CONSTRUCTOR
        }

        @Test
        fun `should infer FACTORY from Set-of`() {
            val env =
                TypeEnvironment.build(
                    fn(
                        children =
                            listOf(
                                varDecl("ids", children = listOf(functionCall("of", qualifiedTarget = "Set"))),
                            ),
                    ),
                    emptyMap(),
                    Language.JAVA,
                    registry,
                )
            val type = env.typeOf("ids")!!
            type.source shouldBe TypeSource.FACTORY
        }

        @Test
        fun `should infer FACTORY from setOf in Kotlin`() {
            val env =
                TypeEnvironment.build(
                    fn(children = listOf(varDecl("ids", children = listOf(functionCall("setOf"))))),
                    emptyMap(),
                    Language.KOTLIN,
                    registry,
                )
            val type = env.typeOf("ids")!!
            type.simpleName shouldBe "Set"
            type.source shouldBe TypeSource.FACTORY
        }

        @Test
        fun `should infer NAME_HEURISTIC from method name suffix`() {
            val env =
                TypeEnvironment.build(
                    fn(children = listOf(varDecl("ids", children = listOf(functionCall("getAllowedSet"))))),
                    emptyMap(),
                    Language.JAVA,
                    registry,
                )
            val type = env.typeOf("ids")!!
            type.simpleName shouldBe "Set"
            type.source shouldBe TypeSource.NAME_HEURISTIC
        }

        @Test
        fun `should return null for unknown variable`() {
            val env = TypeEnvironment.build(fn(), emptyMap(), Language.JAVA, registry)
            env.typeOf("unknown").shouldBeNull()
        }
    }

    @Nested
    inner class ReassignmentGuard {
        @Test
        fun `should skip type for reassigned variable`() {
            val env =
                TypeEnvironment.build(
                    fn(
                        children =
                            listOf(
                                varDecl("items", "HashSet<String>"),
                                varDecl("items", "ArrayList<String>"),
                            ),
                    ),
                    emptyMap(),
                    Language.JAVA,
                    registry,
                )
            env.typeOf("items").shouldBeNull()
        }

        @Test
        fun `should keep type for single-assignment variable`() {
            val env =
                TypeEnvironment.build(
                    fn(children = listOf(varDecl("items", "HashSet<String>"))),
                    emptyMap(),
                    Language.JAVA,
                    registry,
                )
            env.typeOf("items")!!.simpleName shouldBe "HashSet<String>"
        }
    }

    @Nested
    inner class O1Queries {
        @Test
        fun `should return true for Set parameter`() {
            val env =
                TypeEnvironment.build(
                    fn(parameters = listOf(param("ids", "Set<String>"))),
                    emptyMap(),
                    Language.JAVA,
                    registry,
                )
            env.isO1("ids") shouldBe true
        }

        @Test
        fun `should return true for HashMap field`() {
            val env =
                TypeEnvironment.build(
                    fn(),
                    mapOf("cache" to "HashMap<String, Object>"),
                    Language.JAVA,
                    registry,
                )
            env.isO1("cache") shouldBe true
        }

        @Test
        fun `should return false for List parameter`() {
            val env =
                TypeEnvironment.build(
                    fn(parameters = listOf(param("items", "List<String>"))),
                    emptyMap(),
                    Language.JAVA,
                    registry,
                )
            env.isO1("items") shouldBe false
        }

        @Test
        fun `should NOT promote NAME_HEURISTIC Set to O1`() {
            val env =
                TypeEnvironment.build(
                    fn(children = listOf(varDecl("ids", children = listOf(functionCall("getCharacterSet"))))),
                    emptyMap(),
                    Language.JAVA,
                    registry,
                )
            // Type is inferred as "Set" from NAME_HEURISTIC, but isO1 must reject it
            env.typeOf("ids")!!.source shouldBe TypeSource.NAME_HEURISTIC
            env.isO1("ids") shouldBe false
        }

        @Test
        fun `should return false for unknown variable`() {
            val env = TypeEnvironment.build(fn(), emptyMap(), Language.JAVA, registry)
            env.isO1("unknown") shouldBe false
        }

        @Test
        fun `should strip this prefix`() {
            val env =
                TypeEnvironment.build(
                    fn(),
                    mapOf("cache" to "ConcurrentHashMap<String, Object>"),
                    Language.JAVA,
                    registry,
                )
            env.isO1("this.cache") shouldBe true
        }
    }

    @Nested
    inner class StringQueries {
        @Test
        fun `should return true for String parameter`() {
            val env =
                TypeEnvironment.build(
                    fn(parameters = listOf(param("name", "String"))),
                    emptyMap(),
                    Language.JAVA,
                    registry,
                )
            env.isString("name") shouldBe true
        }

        @Test
        fun `should return true for StringBuilder`() {
            val env =
                TypeEnvironment.build(
                    fn(children = listOf(varDecl("buf", children = listOf(objectCreation("StringBuilder"))))),
                    emptyMap(),
                    Language.JAVA,
                    registry,
                )
            env.isString("buf") shouldBe true
        }

        @Test
        fun `should return false for List`() {
            val env =
                TypeEnvironment.build(
                    fn(parameters = listOf(param("items", "List<String>"))),
                    emptyMap(),
                    Language.JAVA,
                    registry,
                )
            env.isString("items") shouldBe false
        }
    }

    @Nested
    inner class ListQueries {
        @Test
        fun `should return true for ArrayList`() {
            val env =
                TypeEnvironment.build(
                    fn(children = listOf(varDecl("items", children = listOf(objectCreation("ArrayList"))))),
                    emptyMap(),
                    Language.JAVA,
                    registry,
                )
            env.isList("items") shouldBe true
        }

        @Test
        fun `should return false for HashSet`() {
            val env =
                TypeEnvironment.build(
                    fn(children = listOf(varDecl("ids", children = listOf(objectCreation("HashSet"))))),
                    emptyMap(),
                    Language.JAVA,
                    registry,
                )
            env.isList("ids") shouldBe false
        }
    }

    @Nested
    inner class CollectionQueries {
        @Test
        fun `should return true for List`() {
            val env =
                TypeEnvironment.build(
                    fn(parameters = listOf(param("items", "List<String>"))),
                    emptyMap(),
                    Language.JAVA,
                    registry,
                )
            env.isCollection("items") shouldBe true
        }

        @Test
        fun `should return true for Set`() {
            val env =
                TypeEnvironment.build(
                    fn(parameters = listOf(param("ids", "Set<String>"))),
                    emptyMap(),
                    Language.JAVA,
                    registry,
                )
            env.isCollection("ids") shouldBe true
        }

        @Test
        fun `should return false for String`() {
            val env =
                TypeEnvironment.build(
                    fn(parameters = listOf(param("name", "String"))),
                    emptyMap(),
                    Language.JAVA,
                    registry,
                )
            env.isCollection("name") shouldBe false
        }
    }
}
