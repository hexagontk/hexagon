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
    idea
    eclipse

    kotlin("jvm") version("1.4.10") apply(false)
    id("org.jetbrains.dokka") version("0.10.1") apply(false)
    id("io.gitlab.arturbosch.detekt") version("1.13.1") apply(false)
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

/*
 * TODO Move `dokkaMd` task to `gradle/dokka.gradle.kts` when it is ported to Kotlin DSL.
 *   Check: https://github.com/Kotlin/dokka/issues/50
 */
childProjects.forEach { pair ->
    val name = pair.key
    val prj = pair.value
    val empty = prj.getTasksByName("dokkaMd", false).isEmpty()

    if (name !in listOf("hexagon_site", "hexagon_starters") && empty) {
        project(name).tasks.register<DokkaTask>("dokkaMd") {
            project("hexagon_site").tasks["mkdocs"].dependsOn(":$name:dokkaMd")

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
}

tasks.register<Exec>("infrastructure") {
    group = "build"
    description = "Start the project's infrastructure (with Docker Compose) required for the tests."

    errorOutput = nullOutputStream()
    commandLine("docker-compose --log-level warning up -d mongodb rabbitmq".split(" "))
}

getTasksByName("test", true).forEach {
    it.dependsOn(tasks["infrastructure"])
}
