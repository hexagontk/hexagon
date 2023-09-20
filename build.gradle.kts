
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
 *
 * Plugins that are not used in the root project (this one) are only applied by the modules that use
 * them.
 */

plugins {
    kotlin("jvm") version("1.9.10") apply(false)

    id("idea")
    id("eclipse")
    id("project-report")
    id("org.jetbrains.dokka") version("1.9.0")
    id("com.github.jk1.dependency-license-report") version("2.5")
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version("0.13.2")
    id("org.graalvm.buildtools.native") version("0.9.27") apply(false)
    id("io.gitlab.arturbosch.detekt") version("1.23.1") apply(false)
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

task("nativeTestModules") {
    group = "reporting"
    description = "Print module descriptions to be used in the GraalVM native compliant directory."

    doLast {
        val gitHub = "https://github.com/hexagonkt/hexagon/tree/master"
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
                        "https://github.com/hexagonkt/hexagon/actions/workflows/nightly.yml"
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

            // Experimental modules
            "rest",
            "rest_tools",
            "web",
        )
    )
}
