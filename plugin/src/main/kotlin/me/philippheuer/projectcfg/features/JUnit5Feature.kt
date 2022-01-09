package me.philippheuer.projectcfg.features

import me.philippheuer.projectcfg.ProjectConfigurationExtension
import me.philippheuer.projectcfg.domain.PluginModule
import me.philippheuer.projectcfg.domain.ProjectLanguage
import me.philippheuer.projectcfg.util.DependencyVersion
import me.philippheuer.projectcfg.util.PluginLogger
import me.philippheuer.projectcfg.util.addDepdenency
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

/**
 * JUnit5 Task Configuration
 */
class JUnit5Feature constructor(override var project: Project, override var config: ProjectConfigurationExtension) : PluginModule {
    override fun check(): Boolean {
        return true
    }

    override fun run() {
        configureJunitDependencies(project, config)
        configureTestTask(project)
    }

    companion object {
        private fun configureJunitDependencies(project: Project, config: ProjectConfigurationExtension) {
            project.addDepdenency(
                "testImplementation",
                "org.junit.jupiter:junit-jupiter-api:${DependencyVersion.junit5Version}"
            )
            project.addDepdenency(
                "testImplementation",
                "org.junit.jupiter:junit-jupiter-params:${DependencyVersion.junit5Version}"
            )
            project.addDepdenency(
                "testRuntimeOnly",
                "org.junit.jupiter:junit-jupiter-engine:${DependencyVersion.junit5Version}"
            )

            if (ProjectLanguage.KOTLIN.valueEquals(config.language.get())) {
                project.addDepdenency(
                    "testImplementation",
                    "org.jetbrains.kotlin:kotlin-test:${DependencyVersion.kotlinVersion}"
                )
            }
        }

        private fun configureTestTask(project: Project) {
            project.tasks.withType(Test::class.java).configureEach { test ->
                // use junit5
                PluginLogger.log(LogLevel.DEBUG, "setting [test.useJUnitPlatform()]")
                test.useJUnitPlatform()

                // test logging
                test.testLogging.showExceptions = true
                PluginLogger.log(LogLevel.DEBUG, "setting [test.testLogging.showExceptions] to [${test.testLogging.showExceptions}]")
                test.testLogging.showStandardStreams = true
                PluginLogger.log(LogLevel.DEBUG, "setting [test.testLogging.showStandardStreams] to [${test.testLogging.showStandardStreams}]")
                test.testLogging.exceptionFormat = TestExceptionFormat.FULL
                PluginLogger.log(LogLevel.DEBUG, "setting [test.testLogging.exceptionFormat] to [${test.testLogging.exceptionFormat.name}]")

                // don't require tests
                test.filter { filter ->
                    filter.isFailOnNoMatchingTests = false
                    PluginLogger.log(LogLevel.DEBUG, "setting [test.filter.isFailOnNoMatchingTests] to [${filter.isFailOnNoMatchingTests}]")
                }

                // full retest, even if no changes have been made
                test.dependsOn("cleanTest")
            }
        }
    }
}