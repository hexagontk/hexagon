
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
    kotlin("jvm") version("1.6.21") apply(false)

    id("idea")
    id("eclipse")
    id("org.jetbrains.dokka") version("1.6.21")
    id("com.github.jk1.dependency-license-report") version("2.1")
    id("io.gitlab.arturbosch.detekt") version("1.20.0") apply(false)
    id("me.champeau.jmh") version("0.6.6") apply(false)
}

apply(from = "gradle/certificates.gradle")

repositories {
    mavenCentral()
}

tasks.register<Delete>("clean") {
    group = "build"
    description = "Delete root project's generated artifacts, logs and error dumps."

    delete("build", "log", "out", ".vertx", "file-uploads", "config")
    delete(
        fileTree(rootDir) { include("**/*.log") },
        fileTree(rootDir) { include("**/*.hprof") },
        fileTree(rootDir) { include("**/.attach_pid*") },
        fileTree(rootDir) { include("**/hs_err_pid*") }
    )
}

task("setUp") {
    group = "build setup"
    description = "Set up project for development. Creates the Git pre push hook (run build task)."

    doLast {
        val prePush = file(".git/hooks/pre-push")
        prePush.writeText("""
            #!/usr/bin/env sh
            set -e
            ./gradlew clean build
        """.trimIndent() + "\n")
        prePush.setExecutable(true)

        exec { commandLine("docker version".split(" ")) }
        exec { commandLine("git config commit.template ../.github/commit_template.txt".split(" ")) }
    }
}

task("release") {
    group = "publishing"
    description = "Tag the source code with the version number after publishing all artifacts."
    dependsOn(project.getTasksByName("publish", true))

    doLast {
        val release = version.toString()
        project.exec { commandLine = listOf("git", "tag", "-m", "Release $release", release) }
        project.exec { commandLine = listOf("git", "push", "--tags") }
    }
}

licenseReport {
    projects = subprojects.toTypedArray()
    renderers = arrayOf<ReportRenderer>(
        CsvReportRenderer(),
        InventoryHtmlReportRenderer(),
        InventoryMarkdownReportRenderer(),
    )
}

gradle.taskGraph.whenReady(closureOf<TaskExecutionGraph> {
    if (logger.isInfoEnabled()) {
        allTasks.forEach { task ->
            logger.info(task.toString())
            task.dependsOn.forEach { logger.info("  - $it") }
        }
    }
})
