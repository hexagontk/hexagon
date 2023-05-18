
import com.github.jk1.license.LicenseReportExtension
import com.github.jk1.license.render.CsvReportRenderer
import com.github.jk1.license.render.InventoryHtmlReportRenderer
import com.github.jk1.license.render.InventoryMarkdownReportRenderer
import com.github.jk1.license.render.ReportRenderer

/*
 * Main build script, responsible for:
 *
 *  1. Publishing: upload binaries and templates to Maven Central
 *  2. Releasing: tag source code in GitHub
 *  3. Coverage report: aggregated coverage report for all modules
 *  4. Handle Docker containers: take care of tasks depending on Docker and containers clean up
 *
 * Plugins that are not used in the root project (this one) are only applied by the modules that use
 * them.
 */

plugins {
    kotlin("jvm") version("1.8.21") apply(false)

    id("idea")
    id("eclipse")
    id("project-report")
    id("org.jetbrains.dokka") version("1.8.10")
    id("com.github.jk1.dependency-license-report") version("2.2")
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version("0.13.1")
    id("org.graalvm.buildtools.native") version("0.9.22") apply(false)
    id("io.gitlab.arturbosch.detekt") version("1.22.0") apply(false)
    id("me.champeau.jmh") version("0.7.1") apply(false)
}

apply(from = "gradle/certificates.gradle")

defaultTasks("build")

repositories {
    mavenCentral()
}

task("setUp") {
    group = "build setup"
    description = "Set up project for development. Creates the Git pre push hook (run build task)."

    doLast {
        val dotfiles = "https://raw.githubusercontent.com/hexagonkt/.github/master"
        exec { commandLine("curl $dotfiles/.gitignore -o .gitignore".split(" ")) }
        exec { commandLine("curl $dotfiles/commit_template.txt -o .git/message".split(" ")) }
        exec { commandLine("curl $dotfiles/.editorconfig -o .editorconfig".split(" ")) }
        exec { commandLine("git config commit.template .git/message".split(" ")) }

        val prePush = file(".git/hooks/pre-push")
        prePush.writeText("""
            #!/usr/bin/env sh
            set -e
            ./gradlew
        """.trimIndent() + "\n")
        prePush.setExecutable(true)
    }
}

task("release") {
    group = "publishing"
    description = "Tag the source code with the version number after publishing all artifacts."
    dependsOn(project.getTasksByName("publish", true))

    doLast {
        val release = version.toString()
        val actor = System.getenv("GITHUB_ACTOR")

        project.exec { commandLine = listOf("git", "config", "--global", "user.name", actor) }
        project.exec { commandLine = listOf("git", "tag", "-m", "Release $release", release) }
        project.exec { commandLine = listOf("git", "push", "--tags") }
    }
}

extensions.configure<LicenseReportExtension> {
    projects = subprojects.toTypedArray()
    renderers = arrayOf<ReportRenderer>(
        CsvReportRenderer(),
        InventoryHtmlReportRenderer(),
        InventoryMarkdownReportRenderer(),
    )
}

gradle.taskGraph.whenReady(closureOf<TaskExecutionGraph> {
    if (logger.isInfoEnabled) {
        allTasks.forEach { task ->
            logger.info(task.toString())
            task.dependsOn.forEach { logger.info("  - $it") }
        }
    }
})

apiValidation {
    validationDisabled = true
}

subprojects {
    apply(from = "$rootDir/gradle/detekt.gradle")
}
