package com.github.tvinke.algorilla.engine

import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LookupKind
import com.github.tvinke.algorilla.model.Parameter
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import com.github.tvinke.algorilla.util.findDescendants
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class MarkScalarLookupsTest {
    private val registry = LanguageSemanticsRegistry.loadDefaults()
    private val loc = SourceLocation("Test.java", 1, 1)

    private fun fileRoot(
        language: Language = Language.JAVA,
        children: List<com.github.tvinke.algorilla.model.IRNode>,
    ) = FileRoot(
        filePath = "Test.java",
        language = language,
        location = loc,
        children = children,
    )

    private fun function(
        name: String = "test",
        declaringClass: String? = "TestClass",
        parameters: List<Parameter> = emptyList(),
        children: List<com.github.tvinke.algorilla.model.IRNode> = emptyList(),
    ) = FunctionDecl(
        name = name,
        qualifiedName = "TestClass.$name",
        parameters = parameters,
        declaringClass = declaringClass,
        location = loc,
        children = children,
    )

    private fun lookupCall(
        targetVariable: String?,
        kind: LookupKind = LookupKind.CONTAINS,
    ) = LookupCall(
        kind = kind,
        targetVariable = targetVariable,
        isO1 = false,
        location = loc,
        children = emptyList(),
    )

    private fun param(
        name: String,
        typeName: String,
    ) = Parameter(name = name, typeName = typeName)

    private fun field(
        name: String,
        typeName: String,
    ) = VariableDecl(
        name = name,
        typeName = typeName,
        location = loc,
        children = emptyList(),
    )

    private fun varWithFactory(
        name: String,
        factoryName: String,
        qualifiedTarget: String? = null,
    ) = VariableDecl(
        name = name,
        typeName = null,
        location = loc,
        children =
            listOf(
                FunctionCall(
                    name = factoryName,
                    qualifiedTarget = qualifiedTarget,
                    location = loc,
                    children = emptyList(),
                ),
            ),
    )

    @Nested
    inner class O1TypePromotion {
        @Test
        fun `should promote Set parameter lookup to isO1`() {
            val fn =
                function(
                    parameters = listOf(param("ids", "Set<String>")),
                    children = listOf(lookupCall("ids")),
                )
            val root = fileRoot(children = listOf(fn))

            val result = markScalarLookups(mapOf("Test.java" to root), registry)

            val lookup =
                result.values
                    .first()
                    .findDescendants<LookupCall>()
                    .single()
            lookup.isO1 shouldBe true
            lookup.isScalar shouldBe false
        }

        @Test
        fun `should promote Map parameter lookup to isO1`() {
            val fn =
                function(
                    parameters = listOf(param("cache", "HashMap<String, Integer>")),
                    children = listOf(lookupCall("cache")),
                )
            val root = fileRoot(children = listOf(fn))

            val result = markScalarLookups(mapOf("Test.java" to root), registry)

            val lookup =
                result.values
                    .first()
                    .findDescendants<LookupCall>()
                    .single()
            lookup.isO1 shouldBe true
        }

        @Test
        fun `should promote Set field lookup to isO1`() {
            val fn =
                function(
                    children = listOf(lookupCall("allowedIds")),
                )
            val root =
                fileRoot(
                    children =
                        listOf(
                            field("allowedIds", "Set<String>"),
                            fn,
                        ),
                )

            val result = markScalarLookups(mapOf("Test.java" to root), registry)

            val lookup =
                result.values
                    .first()
                    .findDescendants<LookupCall>()
                    .single()
            lookup.isO1 shouldBe true
        }

        @Test
        fun `should promote this-prefixed Set field lookup to isO1`() {
            val fn =
                function(
                    children = listOf(lookupCall("this.allowedIds")),
                )
            val root =
                fileRoot(
                    children =
                        listOf(
                            field("allowedIds", "HashSet<String>"),
                            fn,
                        ),
                )

            val result = markScalarLookups(mapOf("Test.java" to root), registry)

            val lookup =
                result.values
                    .first()
                    .findDescendants<LookupCall>()
                    .single()
            lookup.isO1 shouldBe true
        }
    }

    @Nested
    inner class ScalarMarking {
        @Test
        fun `should mark String parameter lookup as scalar`() {
            val fn =
                function(
                    parameters = listOf(param("name", "String")),
                    children = listOf(lookupCall("name", LookupKind.INDEX_OF)),
                )
            val root = fileRoot(children = listOf(fn))

            val result = markScalarLookups(mapOf("Test.java" to root), registry)

            val lookup =
                result.values
                    .first()
                    .findDescendants<LookupCall>()
                    .single()
            lookup.isScalar shouldBe true
            lookup.isO1 shouldBe false
        }

        @Test
        fun `should leave List parameter lookup unchanged`() {
            val fn =
                function(
                    parameters = listOf(param("items", "List<String>")),
                    children = listOf(lookupCall("items")),
                )
            val root = fileRoot(children = listOf(fn))

            val result = markScalarLookups(mapOf("Test.java" to root), registry)

            val lookup =
                result.values
                    .first()
                    .findDescendants<LookupCall>()
                    .single()
            lookup.isO1 shouldBe false
            lookup.isScalar shouldBe false
        }

        @Test
        fun `should leave untyped variable lookup unchanged`() {
            val fn =
                function(
                    children = listOf(lookupCall("unknown")),
                )
            val root = fileRoot(children = listOf(fn))

            val result = markScalarLookups(mapOf("Test.java" to root), registry)

            val lookup =
                result.values
                    .first()
                    .findDescendants<LookupCall>()
                    .single()
            lookup.isO1 shouldBe false
            lookup.isScalar shouldBe false
        }
    }

    @Nested
    inner class CrossFileFieldTypeResolution {
        private fun crossFileLookup(
            fieldType: String,
            className: String,
        ): LookupCall {
            val fieldFile =
                fileRoot(
                    children =
                        listOf(
                            field("target", fieldType),
                            function(name = "dummy", declaringClass = className),
                        ),
                )
            val consumerFile =
                FileRoot(
                    filePath = "Consumer.java",
                    language = Language.JAVA,
                    location = loc,
                    children =
                        listOf(
                            function("process", className, children = listOf(lookupCall("this.target"))),
                        ),
                )
            val irTrees = mapOf("Fields.java" to fieldFile, "Consumer.java" to consumerFile)
            return markScalarLookups(irTrees, registry)["Consumer.java"]!!
                .findDescendants<LookupCall>()
                .single()
        }

        @Test
        fun `should resolve Set field type cross-file and promote to isO1`() {
            val lookup = crossFileLookup("Set<String>", "MyService")
            lookup.isO1 shouldBe true
        }

        @Test
        fun `should resolve List field type cross-file and leave as linear`() {
            val lookup = crossFileLookup("ArrayList<String>", "DataHolder")
            lookup.isO1 shouldBe false
            lookup.isScalar shouldBe false
        }
    }

    @Nested
    inner class InheritedFieldTypeResolution {
        private fun inheritedFieldLookup(
            fieldName: String,
            fieldType: String,
        ): LookupCall {
            val parentFile =
                fileRoot(
                    children =
                        listOf(
                            field(fieldName, fieldType),
                            function(name = "parentMethod", declaringClass = "ParentRegistry"),
                        ),
                )
            val childFile =
                FileRoot(
                    filePath = "Child.java",
                    language = Language.JAVA,
                    location = loc,
                    children =
                        listOf(
                            function("childMethod", "ChildFactory", children = listOf(lookupCall(fieldName))),
                        ),
                )
            val irTrees = mapOf("Parent.java" to parentFile, "Child.java" to childFile)
            return markScalarLookups(irTrees, registry)["Child.java"]!!
                .findDescendants<LookupCall>()
                .single()
        }

        @Test
        fun `should resolve inherited Set field from parent class via fallback`() {
            val lookup = inheritedFieldLookup("singletonsInCreation", "Set<String>")
            lookup.isO1 shouldBe true
        }

        @Test
        fun `should not promote inherited List field to O1`() {
            val lookup = inheritedFieldLookup("beanNames", "ArrayList<String>")
            lookup.isO1 shouldBe false
            lookup.isScalar shouldBe false
        }
    }

    @Nested
    inner class FactoryMethodInference {
        @Test
        fun `should promote setOf initialized variable to O1`() {
            val fn =
                function(
                    children =
                        listOf(
                            varWithFactory("allowed", "setOf"),
                            lookupCall("allowed"),
                        ),
                )
            val root = fileRoot(language = Language.KOTLIN, children = listOf(fn))
            val lookup =
                markScalarLookups(mapOf("Test.kt" to root), registry)
                    .values
                    .first()
                    .findDescendants<LookupCall>()
                    .single()
            lookup.isO1 shouldBe true
        }

        @Test
        fun `should promote Set-dot-of initialized variable to O1`() {
            val fn =
                function(
                    children =
                        listOf(
                            varWithFactory("immutable", "of", qualifiedTarget = "Set"),
                            lookupCall("immutable"),
                        ),
                )
            val root = fileRoot(children = listOf(fn))
            val lookup =
                markScalarLookups(mapOf("Test.java" to root), registry)
                    .values
                    .first()
                    .findDescendants<LookupCall>()
                    .single()
            lookup.isO1 shouldBe true
        }

        @Test
        fun `should promote ConcurrentHashMap-newKeySet to O1`() {
            val fn =
                function(
                    children =
                        listOf(
                            varWithFactory("active", "newKeySet", qualifiedTarget = "ConcurrentHashMap"),
                            lookupCall("active"),
                        ),
                )
            val root = fileRoot(children = listOf(fn))
            val lookup =
                markScalarLookups(mapOf("Test.java" to root), registry)
                    .values
                    .first()
                    .findDescendants<LookupCall>()
                    .single()
            lookup.isO1 shouldBe true
        }

        @Test
        fun `should not promote unknown factory to O1`() {
            val fn =
                function(
                    children =
                        listOf(
                            varWithFactory("items", "toList"),
                            lookupCall("items"),
                        ),
                )
            val root = fileRoot(children = listOf(fn))
            val lookup =
                markScalarLookups(mapOf("Test.java" to root), registry)
                    .values
                    .first()
                    .findDescendants<LookupCall>()
                    .single()
            lookup.isO1 shouldBe false
        }

        @Test
        fun `should promote keySet return to O1`() {
            val fn =
                function(
                    children =
                        listOf(
                            varWithFactory("keys", "keySet", qualifiedTarget = "cache"),
                            lookupCall("keys"),
                        ),
                )
            val root = fileRoot(children = listOf(fn))
            val lookup =
                markScalarLookups(mapOf("Test.java" to root), registry)
                    .values
                    .first()
                    .findDescendants<LookupCall>()
                    .single()
            lookup.isO1 shouldBe true
        }

        @Test
        fun `should promote entrySet return to O1`() {
            val fn =
                function(
                    children =
                        listOf(
                            varWithFactory("entries", "entrySet", qualifiedTarget = "map"),
                            lookupCall("entries"),
                        ),
                )
            val root = fileRoot(children = listOf(fn))
            val lookup =
                markScalarLookups(mapOf("Test.java" to root), registry)
                    .values
                    .first()
                    .findDescendants<LookupCall>()
                    .single()
            lookup.isO1 shouldBe true
        }

        @Test
        fun `should not promote values return to O1`() {
            val fn =
                function(
                    children =
                        listOf(
                            varWithFactory("vals", "values", qualifiedTarget = "map"),
                            lookupCall("vals"),
                        ),
                )
            val root = fileRoot(children = listOf(fn))
            val lookup =
                markScalarLookups(mapOf("Test.java" to root), registry)
                    .values
                    .first()
                    .findDescendants<LookupCall>()
                    .single()
            lookup.isO1 shouldBe false
        }

        @Test
        fun `should promote mutableMapOf initialized variable to O1`() {
            val fn =
                function(
                    children =
                        listOf(
                            varWithFactory("cache", "mutableMapOf"),
                            lookupCall("cache"),
                        ),
                )
            val root = fileRoot(language = Language.KOTLIN, children = listOf(fn))
            val lookup =
                markScalarLookups(mapOf("Test.kt" to root), registry)
                    .values
                    .first()
                    .findDescendants<LookupCall>()
                    .single()
            lookup.isO1 shouldBe true
        }
    }
}
