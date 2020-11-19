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

import java.io.OutputStream.nullOutputStream
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    id("idea")
    id("eclipse")

    kotlin("jvm") version("1.4.20-RC") apply(false) // TODO

//    id("org.jetbrains.dokka") version("1.4.10.2") apply(false)
    id("org.jetbrains.dokka") version("0.10.1") apply(false)
    id("io.gitlab.arturbosch.detekt") version("1.14.2") apply(false)
}

apply(from = "gradle/certificates.gradle")
apply(from = "gradle/docker.gradle")

tasks.register<Delete>("clean") {
    group = "build"
    description = "Delete root project's generated artifacts, logs and error dumps."
    dependsOn("cleanDocker")

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
        exec { commandLine("docker-compose version".split(" ")) }
        exec { commandLine("git config commit.template .github/commit_template.txt".split(" ")) }
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

// TODO Move `dokkaGfm` task to `gradle/dokka.gradle.kts`
childProjects
    .filter { (name, _) -> name !in listOf("hexagon_site", "hexagon_starters") }
    .filter { (_, prj) -> prj.getTasksByName("dokkaGfm", false).isEmpty() }
    .forEach { (_, prj) ->
        prj.tasks.register<DokkaTask>("dokkaGfm") {
            project("hexagon_site").tasks["mkdocs"].dependsOn(this)

            outputFormat = "gfm"
            outputDirectory = "${rootDir}/hexagon_site/content"

            configuration {
                reportUndocumented = false
                includes = prj.pathsCollection(include = "*.md")
                samples = prj.pathsCollection(include = "src/test/kotlin/**/*SamplesTest.kt")
                sourceRoot { path = "${prj.projectDir}/src/main/kotlin" }
            }
        }
    }

tasks.register<Exec>("infrastructure") {
    group = "build"
    description = "Start the project's infrastructure (with Docker Compose) required for the tests."
    standardOutput = nullOutputStream()
    errorOutput = standardOutput

    commandLine("docker-compose --log-level warning up -d mongodb rabbitmq".split(" "))
}

getTasksByName("test", true).forEach {
    it.dependsOn(tasks["infrastructure"])
}
