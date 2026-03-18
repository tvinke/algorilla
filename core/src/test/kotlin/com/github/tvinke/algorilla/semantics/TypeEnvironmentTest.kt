package com.github.tvinke.algorilla.semantics

import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.InferredType
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
            type.simpleName shouldBe "Set"
            type.fullTypeName shouldBe "Set<String>"
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
            type.simpleName shouldBe "HashMap"
            type.fullTypeName shouldBe "HashMap<String, Object>"
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
            env.typeOf("items")!!.simpleName shouldBe "Set"
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
            env.typeOf("items")!!.simpleName shouldBe "HashSet"
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

    @Nested
    inner class CrossFileReturnTypes {
        @Test
        fun `should infer CROSS_FILE_RETURN from qualified method`() {
            val context =
                TypeContext(
                    globalMethodReturnTypes = mapOf("UserService.getAllOrders" to "List"),
                )
            val env =
                TypeEnvironment.build(
                    fn(
                        children =
                            listOf(
                                varDecl("orders", children = listOf(functionCall("getAllOrders", qualifiedTarget = "UserService"))),
                            ),
                    ),
                    context,
                    Language.JAVA,
                    registry,
                )
            val type = env.typeOf("orders")!!
            type.simpleName shouldBe "List"
            type.source shouldBe TypeSource.CROSS_FILE_RETURN
        }

        @Test
        fun `same-file return type takes priority over cross-file`() {
            val context =
                TypeContext(
                    localMethodReturnTypes = mapOf("getAllOrders" to "Set"),
                    globalMethodReturnTypes = mapOf("UserService.getAllOrders" to "List"),
                )
            val env =
                TypeEnvironment.build(
                    fn(children = listOf(varDecl("orders", children = listOf(functionCall("getAllOrders"))))),
                    context,
                    Language.JAVA,
                    registry,
                )
            // Same-file wins (FACTORY source, checked before CROSS_FILE_RETURN)
            env.typeOf("orders")!!.simpleName shouldBe "Set"
        }

        @Test
        fun `ambiguous cross-file method names are excluded`() {
            val context =
                TypeContext(
                    globalMethodReturnTypes =
                        mapOf(
                            "UserService.getAll" to "List",
                            "OrderService.getAll" to "Set",
                        ),
                )
            val env =
                TypeEnvironment.build(
                    fn(children = listOf(varDecl("items", children = listOf(functionCall("getAll"))))),
                    context,
                    Language.JAVA,
                    registry,
                )
            // No qualified target, 2 methods named "getAll" → ambiguous, skip
            env.typeOf("items").shouldBeNull()
        }
    }

    @Nested
    inner class HierarchyResolution {
        @Test
        fun `class implementing Set is O1`() {
            val context =
                TypeContext(
                    classHierarchy = mapOf("UserCache" to setOf("Set")),
                )
            val env =
                TypeEnvironment.build(
                    fn(parameters = listOf(param("cache", "UserCache"))),
                    context,
                    Language.JAVA,
                    registry,
                )
            env.isO1("cache") shouldBe true
        }

        @Test
        fun `class extending ArrayList is a collection but not O1`() {
            val context =
                TypeContext(
                    classHierarchy = mapOf("OrderList" to setOf("ArrayList")),
                )
            val env =
                TypeEnvironment.build(
                    fn(parameters = listOf(param("orders", "OrderList"))),
                    context,
                    Language.JAVA,
                    registry,
                )
            env.isO1("orders") shouldBe false
            env.isCollection("orders") shouldBe true
        }

        @Test
        fun `class implementing Serializable is neither O1 nor collection`() {
            val context =
                TypeContext(
                    classHierarchy = mapOf("Foo" to setOf("Serializable")),
                )
            val env =
                TypeEnvironment.build(
                    fn(parameters = listOf(param("foo", "Foo"))),
                    context,
                    Language.JAVA,
                    registry,
                )
            env.isO1("foo") shouldBe false
            env.isCollection("foo") shouldBe false
        }

        @Test
        fun `transitive hierarchy resolves through multiple levels`() {
            val context =
                TypeContext(
                    classHierarchy =
                        mapOf(
                            "MyCache" to setOf("AbstractCache"),
                            "AbstractCache" to setOf("HashMap"),
                        ),
                )
            val env =
                TypeEnvironment.build(
                    fn(parameters = listOf(param("c", "MyCache"))),
                    context,
                    Language.JAVA,
                    registry,
                )
            env.isO1("c") shouldBe true
        }
    }

    @Nested
    inner class GenericTypeArguments {
        @Test
        fun `typeArguments parses single generic`() {
            val type = InferredType("List", TypeSource.DECLARED, fullTypeName = "List<Order>")
            type.typeArguments shouldBe listOf("Order")
        }

        @Test
        fun `typeArguments parses Map generics`() {
            val type = InferredType("Map", TypeSource.DECLARED, fullTypeName = "Map<String, List<Order>>")
            type.typeArguments shouldBe listOf("String", "List<Order>")
        }

        @Test
        fun `typeArguments returns empty for non-generic type`() {
            val type = InferredType("String", TypeSource.DECLARED)
            type.typeArguments shouldBe emptyList()
        }

        @Test
        fun `fullTypeName preserved through parameter inference`() {
            val env =
                TypeEnvironment.build(
                    fn(parameters = listOf(param("ids", "Set<String>"))),
                    emptyMap(),
                    Language.JAVA,
                    registry,
                )
            val type = env.typeOf("ids")!!
            type.simpleName shouldBe "Set"
            type.fullTypeName shouldBe "Set<String>"
            type.typeArguments shouldBe listOf("String")
        }
    }

    @Nested
    inner class FlowTyping {
        @Test
        fun `branch narrowing overrides base type`() {
            val context =
                TypeContext(
                    fieldTypes = mapOf("x" to "Object"),
                    branchNarrowings = mapOf("x" to "Set"),
                )
            val env = TypeEnvironment.build(fn(), context, Language.JAVA, registry)
            // Within the narrowed branch, x is Set (O1)
            env.isO1("x") shouldBe true
        }

        @Test
        fun `without narrowing, Object is not O1`() {
            val context = TypeContext(fieldTypes = mapOf("x" to "Object"))
            val env = TypeEnvironment.build(fn(), context, Language.JAVA, registry)
            env.isO1("x") shouldBe false
        }
    }
}
