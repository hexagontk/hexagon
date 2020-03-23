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

plugins {
    idea
    eclipse

    id("org.sonarqube") version "2.8"
    id("org.jetbrains.kotlin.jvm") version "1.3.71" apply false
    id("org.jetbrains.dokka") version "0.10.1" apply false
    id("com.jfrog.bintray") version "1.8.4" apply false
}

apply(from = "gradle/sonarqube.gradle")
apply(from = "gradle/certificates.gradle")

tasks.register<Delete>("clean") {
    delete("build", "log", "out", ".vertx", "file-uploads")

    delete(
        fileTree(rootDir) { include("**/*.log") },
        fileTree(rootDir) { include("**/*.hprof") },
        fileTree(rootDir) { include("**/.attach_pid*") },
        fileTree(rootDir) { include("**/hs_err_pid*") }
    )
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

    if (name !in listOf("hexagon_benchmark", "hexagon_site", "hexagon_starters") && empty) {
        project(name).tasks.register<DokkaTask>("dokkaMd") {
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

task("all") {
    dependsOn(
        project.getTasksByName("build", true),
        project.getTasksByName("jacocoTestReport", true),
        project.getTasksByName("installDist", true),
        project.getTasksByName("publishToMavenLocal", true),
        project.getTasksByName("createCa", true),
        project.getTasksByName("createIdentities", true),
        project.getTasksByName("dokkaMd", true),
        project.getTasksByName("checkSite", true),
        project.getTasksByName("tfb", true)
    )
}
