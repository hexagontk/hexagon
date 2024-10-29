
import com.github.jk1.license.LicenseReportExtension
import com.github.jk1.license.render.CsvReportRenderer
import com.github.jk1.license.render.InventoryHtmlReportRenderer
import com.github.jk1.license.render.InventoryMarkdownReportRenderer
import com.github.jk1.license.render.ReportRenderer
import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.ALL
import org.jreleaser.model.Active.ALWAYS

/*
 * Main build script, responsible for:
 *
 *  1. Publishing: upload binaries and templates to Maven Central
 *  2. Releasing: tag source code in GitHub
 *  3. Coverage report: aggregated coverage report for all modules
 *
 * Plugins that are not used in the root project (this one) are only applied by the modules that use
 * them.
 */

plugins {
    kotlin("jvm") version(libs.versions.kotlin) apply(false)

    id("java")
    id("idea")
    id("eclipse")
    id("project-report")
    id("org.jreleaser") version(libs.versions.jreleaser)
    id("org.jetbrains.dokka") version(libs.versions.dokka)
    id("com.github.jk1.dependency-license-report") version(libs.versions.licenseReport)
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version(libs.versions.binValidator)
    id("org.graalvm.buildtools.native") version(libs.versions.nativeTools) apply(false)
    id("io.gitlab.arturbosch.detekt") version(libs.versions.detekt) apply(false)
    id("me.champeau.jmh") version(libs.versions.jmhGradle) apply(false)
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
        val dotfiles = "https://raw.githubusercontent.com/hexagontk/.github/master"
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

task("nativeTestModules") {
    group = "reporting"
    description = "Print module descriptions to be used in the GraalVM native compliant directory."

    doLast {
        val gitHub = "https://github.com/hexagontk/hexagon/tree/main"
        val entries = subprojects
            .filter { sp -> sp.tasks.any { t -> t.name == "nativeTest" } }
            .sortedBy { sp -> "${sp.group}:${sp.name}" }
            .joinToString(",\n") { sp ->
                val n = sp.name
                val g = sp.group
                val d = gitHub + sp.projectDir.absolutePath.removePrefix(rootDir.absolutePath)
                val r = sp.projectDir.resolve("src/main/resources/META-INF/native-image/$g/$n")
                val t = "$d/src/test"
                val m =
                    if (r.exists()) {
                        val metadata = r.absolutePath.removePrefix(rootDir.absolutePath)
                        "\n                        \"$gitHub$metadata\"\n                      "
                    }
                    else ""
                """
                {
                  "artifact": "$g:$n",
                  "description": "${sp.description}",
                  "details": [
                    {
                      "minimum_version": "${sp.version}",
                      "test_level": "fully-tested",
                      "metadata_locations": [$m],
                      "tests_locations": [
                        "$t",
                        "https://github.com/hexagontk/hexagon/actions/workflows/nightly.yml"
                      ]
                    }
                  ]
                }
                """.trimIndent()
            }
            .lines()
            .joinToString("") { "  $it\n" }
        println("[\n$entries\n]")
    }
}

extensions.configure<LicenseReportExtension> {
    projects = subprojects.toTypedArray()
    unionParentPomLicenses = false
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

tasks.wrapper {
    gradleVersion = libs.versions.gradleWrapper.get()
    distributionType = ALL
}

apiValidation {
    ignoredProjects.addAll(
        listOf(
            // Utility modules
            "site",
            "starters",

            // Test modules
            "http_test",
            "serialization_test",
            "templates_test",
        )
    )
}

jreleaser {
    signing {
        active.set(ALWAYS)
        armored = true
    }

    // TODO Enable GitHub release creation directly
    release {
        github {
            tagName = "{{projectVersion}}"
            skipRelease = true
        }
    }

    // TODO Leave Maven Central rules enabled (resolve problems with Kotlin POMs)
    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    applyMavenCentralRules = false // Already checked
                    active.set(ALWAYS)
                    url = "https://central.sonatype.com/api/v1/publisher"

                    val stagingProperty = findProperty("stagingDirectory")?.toString()
                    val stagingDirectory = stagingProperty ?: System.getenv("STAGING_DIRECTORY")
                    stagingRepository(stagingDirectory)
                }
            }
        }
    }
}
