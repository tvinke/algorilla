package com.github.tvinke.algorilla.architecture

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter

/**
 * The compiled `core` production classes, imported once and shared by every ArchUnit-based
 * architecture test in this package ([UtilSharedFunctionCallSiteTest],
 * [WordBoundaryRequiresTypeEnvironmentTest]) - importing from bytecode walks and parses every
 * `.class` file under the path, which is wasted work to repeat per test class when the
 * classes under test don't change between them within a single test run.
 */
internal object MainClassesFixture {
    val mainClasses: JavaClasses by lazy { ClassFileImporter().importPath("build/classes/kotlin/main") }
}
