
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
    id("com.github.jk1.dependency-license-report") version(libs.versions.licenseReport)
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version(libs.versions.binValidator)
    id("org.graalvm.buildtools.native") version(libs.versions.nativeTools) apply(false)
    id("me.champeau.jmh") version(libs.versions.jmhGradle) apply(false)
    id("org.jetbrains.dokka") version(libs.versions.dokka) apply(false)
    id("org.jetbrains.dokka-javadoc") version(libs.versions.dokka) apply(false)
}

mapOf(
    // Publish
    "licenses" to "MIT",
    "vcsUrl" to "https://github.com/hexagontk/hexagon.git",

    // SSL
    "sslOrganization" to "Hexagon",
    "sslDomain" to "hexagontk.com",
    // Domain to test `certificates` helper
    "sslDomain1" to "benchmark.test",
    // Domain to test `certificates` helper
    "sslDomain2" to "name1|name2|subdomains.es",
    // Used to test the Gradle helper script
    "sslLogCommands" to true,

    // Site
    "siteHost" to "https://hexagontk.com",
    "logoSmall" to "assets/img/logo.svg",
    "iconsDirectory" to "content",
)
.forEach { (k, v) -> ext.set(k, v.toString()) }

apply(from = "gradle/certificates.gradle")

allprojects {
    version = "4.0.0-B3"
    group = "com.hexagontk"
}

defaultTasks("build")

repositories {
    mavenCentral()
}

tasks.register("setUp") {
    group = "build setup"
    description = "Set up project for development. Creates the Git pre push hook (run build task)."

    doLast {
        val dotfiles = "https://raw.githubusercontent.com/hexagontk/.github/master"

        execute("curl $dotfiles/.gitignore -o .gitignore")
        execute("curl $dotfiles/commit_template.txt -o .git/message")
        execute("curl $dotfiles/.editorconfig -o .editorconfig")
        execute("git config commit.template .git/message")

        file(".git/hooks/pre-push").apply {
            writeText("""
                #!/usr/bin/env sh
                set -e
                ./gradlew
            """.trimIndent() + "\n")
            setExecutable(true)
        }
    }
}

tasks.register("release") {
    group = "publishing"
    description = "Tag the source code with the version number after publishing all artifacts."
    dependsOn(project.getTasksByName("publish", true))

    doLast {
        val release = version.toString()
        val actor = System.getenv("GITHUB_ACTOR")

        execute(listOf("git", "config", "--global", "user.name", actor))
        execute(listOf("git", "tag", "-m", "Release $release", release))
        execute(listOf("git", "push", "--tags"))
    }
}

tasks.register("nativeTestModules") {
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
                val r = sp.projectDir.resolve("main/META-INF/native-image/$g/$n")
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
    validationDisabled = rootProject.version.toString().matches(".*A.*".toRegex())

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
                    maxRetries = 240

                    val stagingProperty = findProperty("stagingDirectory")?.toString()
                    val stagingDirectory = stagingProperty ?: System.getenv("STAGING_DIRECTORY")
                    stagingRepository(stagingDirectory)
                }
            }
        }
    }
}

private fun execute(command: String) {
    execute(command.split(" "))
}

private fun execute(command: List<String>) {
    exec { commandLine(command) }
}

// TODO Move this logic to 'kotlin.gradle'
private val generatedDir = "build/generated/sources/annotationProcessor/java/main"
private val moduleFile = "main/module-info.java"
private val classFile = "z.java"
private val classContent = """package %s; class z {}"""

subprojects
    .filter { it.file(moduleFile).exists() }
    .forEach {
        it.afterEvaluate {
            it.tasks.named("compileJava") {
                doFirst {
                    it.createPackages()
                }
            }

            it.sourceSets {
                main {
                    java {
                        srcDir(generatedDir)
                        exclude("**/$classFile")
                    }
                }
            }
        }
    }

subprojects
    .filter { it.file("main").exists() }
    .forEach {
        it.afterEvaluate {
            it.tasks.named("jacocoReportSources") {
                doFirst {
                    val baseSrc = rootProject.group.toString() + '.' + it.name.replace("_", "/")
                    it.ext.set("basePackage", baseSrc)
                }
            }
        }
    }

private fun Project.createPackages() {
    file(moduleFile)
        .readLines()
        .asSequence()
        .map { it.trim() }
        .filter { it.startsWith("exports ") }
        .map { it.removePrefix("exports ") }
        .map { it.removeSuffix(";") }
        .sorted()
        .forEach { packageName ->
            val packageDir = packageName.replace('.', '/')
            val classPath = "$generatedDir/$packageDir/$classFile"
            val classBody = classContent.format(packageName)

            mkdir("$generatedDir/$packageDir")
            file(classPath).writeText(classBody)
        }
}
