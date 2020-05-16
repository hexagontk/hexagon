/*
 * Main build script, responsible for:
 *
 *  1. Publishing: upload binaries and templates to Bintray
 *  2. Releasing: tag source code in GitHub
 *  3. Coverage report: aggregated coverage report for all modules
 *  4. Group all tasks: shortcut for all tasks to ease the release process
 *
 * Plugins that are not used in the root project (this one) are only applied by the modules that use
 * them.
 */

import org.jetbrains.dokka.gradle.DokkaTask
import java.io.OutputStream.nullOutputStream

plugins {
    idea
    eclipse

    id("org.sonarqube") version "2.8"
    id("org.jetbrains.kotlin.jvm") version "1.3.72" apply false
    id("org.jetbrains.dokka") version "0.10.1" apply false
    id("com.jfrog.bintray") version "1.8.5" apply false
}

apply(from = "gradle/sonarqube.gradle")
apply(from = "gradle/certificates.gradle")
apply(from = "gradle/docker.gradle.kts")

tasks.register<Delete>("clean") {
    group = "build"
    description = "Delete root project's generated artifacts, logs and error dumps."
    dependsOn("cleanDocker")

    delete("build", "log", "out", ".vertx", "file-uploads")
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
            ./gradlew --warn --console=plain clean build publishToMavenLocal
        """.trimIndent())
        prePush.setExecutable(true)
    }
}

task("publish") {
    dependsOn(project.getTasksByName("bintrayUpload", true))
}

task("release") {
    dependsOn("publish")
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
                includes = filesCollection(prj.projectDir, "*.md")
                samples = filesCollection("${prj.projectDir}/src/test/kotlin", "**/*SamplesTest.kt")
                sourceRoot { path = "$projectDir/src/main/kotlin" }
            }
        }
    }
}

getTasksByName("jacocoTestReport", true).forEach {
    it.dependsOn(getTasksByName("test", true))
}

tasks.register<Exec>("infrastructure") {
    errorOutput = nullOutputStream()
    commandLine("docker-compose --log-level warning up -d mongodb rabbitmq".split(" "))
}

getTasksByName("test", true).forEach {
    it.dependsOn(tasks["infrastructure"])
}
