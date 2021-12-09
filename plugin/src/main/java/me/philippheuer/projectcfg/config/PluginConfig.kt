package me.philippheuer.projectcfg.config

import me.philippheuer.projectcfg.domain.ProjectFramework
import me.philippheuer.projectcfg.domain.ProjectLanguage
import me.philippheuer.projectcfg.domain.ProjectType
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.Property

interface PluginConfig {

    // logLevel for debugging, if not set logs will be forwarded to slf4j
    val logLevel: Property<LogLevel>

    // language
    val language: Property<ProjectLanguage>

    // type
    val type: Property<ProjectType>

    // framework used in the project
    val framework: Property<ProjectFramework>

}