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
    id("org.jetbrains.kotlin.jvm") version "1.3.61" apply false
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
    val siteContentPath = "${rootDir}/hexagon_site/content"

    if (name !in listOf("hexagon_benchmark", "hexagon_site", "hexagon_starters") && empty) {
        project(name).tasks.register<DokkaTask>("dokkaMd") {
            outputFormat = "gfm"
            outputDirectory = siteContentPath

            configuration {
                reportUndocumented = false
                includes = filesCollection(prj.projectDir, "*.md")
                samples = filesCollection("${prj.projectDir}/src/test/kotlin", "**/*SamplesTest.kt")
                sourceRoot { path = "$projectDir/src/main/kotlin" }
            }

            doLast {
                addMetadata(siteContentPath, prj)
            }
        }
    }
}

project.getTasksByName("jacocoTestReport", true).forEach {
    it.dependsOn(project.getTasksByName("test", true))
}

task("all") {
    dependsOn(
        project.getTasksByName("build", true),
        project.getTasksByName("jacocoTestReport", true),
        project.getTasksByName("installDist", true),
        project.getTasksByName("publishToMavenLocal", true),
        project.getTasksByName("createCa", true),
        project.getTasksByName("createIdentity", true),
        project.getTasksByName("dokkaMd", true),
        project.getTasksByName("checkSite", true),
        project.getTasksByName("tfb", true)
    )
}

// TODO Move these functions to `buildSrc` (to Site.kt, or Helpers.kt)
fun filesCollection(dir: Any, pattern: String): List<String> =
    fileTree(dir) { include(pattern) }.files.map { it.absolutePath }

fun addMetadata(siteContentPath: String, project: Project) {
    val projectDirName = project.projectDir.name
    filesCollection(siteContentPath, "**/${projectDirName}/**/*.md").forEach {
        val md = File(it)
        val tempFile = File.createTempFile("temp", md.name)
        tempFile.printWriter().use { writer ->
            writer.println(toEditUrl(it, siteContentPath, projectDirName))
            md.forEachLine { line ->
                writer.println(line)
            }
        }
        ant.withGroovyBuilder {
            "move"("file" to tempFile, "tofile" to md)
        }
    }
}

fun toEditUrl(mdPath: String, siteContentPath: String, projectDirName: String): String {
    val prefix = "edit_url: edit/master/${projectDirName}"
    val withoutContentPath = mdPath.replace("${siteContentPath}/${projectDirName}/", "")
    val parts = withoutContentPath.split(File.separator)

    var editUrl = ""

    if (parts.size > 1 && withoutContentPath.startsWith("com.hexagon")) {
        val afterPackage = parts[1]
        if ("test" !in afterPackage) {
            val srcPrefix = "${prefix}/src/main/kotlin"
            val packageName = parts[0].replace(".", "/")

            if (afterPackage == "index.md") {
                editUrl = "${srcPrefix}/${packageName}/package-info.java"
            } else if (afterPackage.endsWith(".md") || afterPackage.contains(".")) {
                val lastPath = packageName.split("/").last()

                editUrl = "${srcPrefix}/${packageName}/${lastPath.capitalize()}.kt"
            } else {
                val className = toClassName(afterPackage)

                editUrl = "${srcPrefix}/${packageName}/${className}.kt"
            }
        }
    } else if (withoutContentPath == "index.md") {
        editUrl = "${prefix}/README.md"
    }

    return editUrl
}

fun toClassName(mdClassName: String): String =
    mdClassName.replace("\\-[a-z][a-z]*".toRegex()) {
        it.value[1].toUpperCase() + it.value.substring(2)
    }
