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

import java.io.OutputStream

plugins {
    id("idea")
    id("eclipse")

//    kotlin("jvm") version("1.5.0") apply(false)
    kotlin("jvm") version("1.4.32") apply(false)

    id("org.jetbrains.dokka") version("1.4.32") apply(false)
    id("io.gitlab.arturbosch.detekt") version("1.16.0") apply(false)
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

tasks.register<Exec>("infrastructure") {
    group = "build"
    description = "Start the project's infrastructure (with Docker Compose) required for the tests."
    standardOutput = object : OutputStream() { override fun write(b: Int) { /* discarded */ } }
    errorOutput = standardOutput

    commandLine("docker-compose --log-level warning up -d mongodb rabbitmq".split(" "))
}

getTasksByName("test", true).forEach {
    it.dependsOn(tasks["infrastructure"])
}
