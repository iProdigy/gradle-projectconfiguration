package me.philippheuer.projectcfg.features

import me.philippheuer.projectcfg.ProjectConfigurationExtension
import me.philippheuer.projectcfg.ProjectConfigurationPlugin
import me.philippheuer.projectcfg.domain.PluginModule
import me.philippheuer.projectcfg.domain.PluginModule.Companion.log
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension

class CheckstyleFeature constructor(override var project: Project, override var config: ProjectConfigurationExtension) : PluginModule {
    companion object {
        fun applyPlugin(project: Project, config: ProjectConfigurationExtension) {
            // plugin
            log(LogLevel.INFO, project, config, "applying plugin [checkstyle]")
            project.pluginManager.apply("checkstyle")

            // checkstyle
            project.tasks.register("checkstyleAll", Checkstyle::class.java) { task ->
                task.group = "verification"

                val mainSource = project.extensions.getByType(JavaPluginExtension::class.java).sourceSets.getByName("main").java
                task.source = mainSource
                task.classpath = mainSource
                project.subprojects.filter { sp -> sp.pluginManager.hasPlugin("java") || sp.pluginManager.hasPlugin("java-library") }.forEach { sp ->
                    try {
                        val addSource = sp.extensions.getByType(JavaPluginExtension::class.java).sourceSets.getByName("main").java
                        task.source = task.source.plus(addSource)
                        task.classpath = task.classpath.plus(addSource)
                    } catch (ignored: Exception) {}
                }

                task.exclude("**/generated/**", "**/internal/**")
            }

            // configure
            project.extensions.run {
                configure(CheckstyleExtension::class.java) {
                    it.toolVersion = config.checkstyleToolVersion.get()

                    if (project.file("${project.rootDir}/checkstyle.xml").exists()) {
                        it.configFile = project.file("${project.rootDir}/checkstyle.xml")
                    } else if (config.checkstyleRuleSet.get().isNotEmpty()) {
                        val file = ProjectConfigurationPlugin::class.java.classLoader.getResource("checkstyle/${config.checkstyleRuleSet.get()}.xml")
                            ?: throw GradleException("checkstyle ruleset ${config.checkstyleRuleSet.get()} is not supported!")
                        log(LogLevel.INFO, project, config, "using checkstyle ruleset [${config.checkstyleRuleSet.get()}]")
                        val targetFile = project.file("${project.buildDir}/tmp/checkstyle.xml")

                        val fileContent = file.readText()
                        targetFile.parentFile.mkdirs()
                        targetFile.writeText(fileContent)
                        it.configFile = targetFile
                    }
                    log(LogLevel.INFO, project, config, "using checkstyle config [${it.configFile}]")

                    it.maxWarnings = 0
                    it.maxErrors = 0
                }
            }
        }

        fun reportingSetup(project: Project) {
            // tasks
            project.tasks.withType(Checkstyle::class.java).configureEach { task ->
                task.reports { report ->
                    report.xml.required.set(false)
                    report.html.required.set(true)
                }
            }
        }
    }

    override fun check(): Boolean {
        return true
    }

    override fun run() {
        if (project.rootProject == project && (project.file("${project.rootDir}/checkstyle.xml").exists() || config.checkstyleRuleSet.orElse("").get().isNotEmpty())) {
            applyPlugin(project, config)
            reportingSetup(project)
        }
    }
}