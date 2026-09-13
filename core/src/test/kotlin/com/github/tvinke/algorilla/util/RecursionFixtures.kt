package com.github.tvinke.algorilla.util

import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.GenericNode
import com.github.tvinke.algorilla.model.Parameter
import com.github.tvinke.algorilla.model.SourceLocation

/**
 * Shared hand-built IR fixtures for the cc #64/#71 recursion tests
 * ([RecursionDetectorTest], [NameVsTypeRecursionPropertyTest]) — a bare declaration/call pair
 * with a fixed dummy location, no parser involved.
 */
internal val loc = SourceLocation("Fixture.java", 1, 1)

internal fun decl(
    name: String,
    declaringClass: String,
    parameters: List<Parameter>,
) = FunctionDecl(
    name = name,
    qualifiedName = "$declaringClass.$name",
    parameters = parameters,
    declaringClass = declaringClass,
    location = loc,
    children = emptyList(),
)

internal fun call(
    name: String,
    qualifiedTarget: String?,
    argCount: Int,
) = FunctionCall(
    name = name,
    qualifiedTarget = qualifiedTarget,
    arguments = List(argCount) { genericNodeArg() },
    location = loc,
    children = emptyList(),
)

// A placeholder argument node — isSelfCallOf only ever looks at arguments.size.
internal fun genericNodeArg() = GenericNode("arg", loc, emptyList())
