
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier.Public
import kotlin.math.floor

plugins {
    id("org.jetbrains.dokka")
}

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/icons.gradle")

val venv: String = "${project.projectDir}/build/mkdocs"

dependencies {
    dokka(project(":core"))
    dokka(project(":handlers"))
    dokka(project(":helpers"))

    dokka(project(":http:http"))
    dokka(project(":http:http_client"))
    dokka(project(":http:http_client_jdk"))
    dokka(project(":http:http_client_jetty"))
    dokka(project(":http:http_handlers"))
    dokka(project(":http:http_server"))
    dokka(project(":http:http_server_helidon"))
    dokka(project(":http:http_server_jdk"))
    dokka(project(":http:http_server_jetty"))
    dokka(project(":http:http_server_netty"))
    dokka(project(":http:http_server_servlet"))
    dokka(project(":http:http_test"))
    dokka(project(":http:rest"))
    dokka(project(":http:rest_tools"))
    dokka(project(":http:web"))

    dokka(project(":serialization:serialization"))
    dokka(project(":serialization:serialization_dsl_json"))
    dokka(project(":serialization:serialization_jackson"))
    dokka(project(":serialization:serialization_jackson_csv"))
    dokka(project(":serialization:serialization_jackson_json"))
    dokka(project(":serialization:serialization_jackson_toml"))
    dokka(project(":serialization:serialization_jackson_xml"))
    dokka(project(":serialization:serialization_jackson_yaml"))
    dokka(project(":serialization:serialization_test"))

    dokka(project(":serverless:serverless_http_google"))

    dokka(project(":templates:templates"))
    dokka(project(":templates:templates_freemarker"))
    dokka(project(":templates:templates_jte"))
    dokka(project(":templates:templates_pebble"))
    dokka(project(":templates:templates_rocker"))
    dokka(project(":templates:templates_test"))
}

dokka {
    moduleName.set(rootProject.name)
    dokkaSourceSets {
        configureEach {
            documentedVisibilities.set(setOf(Public))
        }
        dokkaPublications.html {
            outputDirectory.set(project.file("build/content/api"))
        }
        pluginsConfiguration.html {
            val footerLines = rootProject.file("site/footer.txt").readLines()
            val footer = footerLines.joinToString(" ") { it.trim() }

            footerMessage.set(footer)
            customStyleSheets.from("assets/css/dokka.css")
            customAssets.from("assets/img/logo.svg")
        }
    }
}

tasks.register<JacocoReport>("jacocoRootReport") {
    dependsOn(rootProject.getTasksByName("jacocoReportSources", true), tasks["dokkaGenerate"])

    val projectExecutionData = fileTree(rootDir) { include("**/build/jacoco/*.exec") }
    val modulesSources = rootProject.modulesPaths("build/jacoco/src")
    val modulesClasses = rootProject.modulesPaths("build/classes/kotlin/main")
        .asSequence()
        .filterNot { it.absolutePath.contains("_test") }
        .filterNot { it.absolutePath.contains("starters") }
        .toList()

    executionData.from(projectExecutionData)
    sourceDirectories.from(modulesSources)
    classDirectories.from(modulesClasses)

    reports {
        html.required.set(true)
        xml.required.set(true)

        val reportsOutput = file("build/content/jacoco").also { it.mkdirs() }
        html.outputLocation.set(reportsOutput)
        xml.outputLocation.set(reportsOutput.resolve("jacoco.xml"))
    }
}

tasks.register("mkDocs") {
    dependsOn(tasks["jacocoRootReport"])
//    dependsOn("icons")

    doLast {
        val contentTarget = project.file("build/content").absolutePath

        rootProject.subprojects
            .filter { subproject -> subproject.file("README.md").exists() }
            .forEach { subproject ->
                val readme = subproject.file("README.md")
                readme.copyTo(file("$contentTarget/${subproject.name}.md"), true)
            }

        copy {
            from(project.file("pages"))
            from(project.file("assets"))
            into(contentTarget)
        }

        overwrite("assets/img/logo.svg", "$contentTarget/api/images/logo-icon.svg")
        overwrite("assets/img/logo_white_text.svg", "$contentTarget/api/images/docs_logo.svg")

        val markdownFiles = fileTree("dir" to contentTarget, "include" to "**/*.md")
        markdownFiles.forEach { markdownFile ->
            var content = markdownFile.readText()
            content = insertSamplesCode(rootProject.projectDir, content)
            content = fixCodeTabs(content)
            markdownFile.writeText(content)
        }

        project.file("build/content/CNAME").writeText(findProperty("sslDomain").toString())

        generateCoverageBadge()
        generateDownloadBadge()
    }
}

tasks.register("checkDocs") {
    dependsOn("mkDocs")
    doLast {
        val readme = rootProject.file("README.md")
        val service = rootProject.file("http/http_server_jetty/test/HelloWorldTest.kt")
        val examples = "http/http_test/main/examples"

        checkSampleCode(readme, rootProject.file(service), "hello_world")
        checkSampleCode(readme, rootProject.file("$examples/BooksTest.kt"), "books")
        checkSampleCode(readme, rootProject.file("$examples/CookiesTest.kt"), "cookies")
        checkSampleCode(readme, rootProject.file("$examples/ErrorsTest.kt"), "errors")
        checkSampleCode(readme, rootProject.file("$examples/FiltersTest.kt"), "filters")
        checkSampleCode(readme, rootProject.file("$examples/FilesTest.kt"), "files")
        checkSampleCode(readme, rootProject.file("$examples/MultipartTest.kt"), "multipart")
        checkSampleCode(readme, rootProject.file("$examples/CorsTest.kt"), "cors")
        checkSampleCode(readme, rootProject.file("$examples/HttpsTest.kt"), "https")
        checkSampleCode(readme, rootProject.file("$examples/ZipTest.kt"), "zip")

        val contentTarget = project.file("build/content").absolutePath
        val markdownFiles = project.fileTree("dir" to contentTarget, "include" to "**/*.md")

        markdownFiles.forEach { markdownFile ->
            if (markdownFile.readText().contains("@code")) {
                val message = "${markdownFile.absolutePath} Contains `@code` placeholder"
                throw GradleException(message)
            }
        }
    }
}

tasks.register("installMkDocs") {
    doLast {
        val mkdocsMaterialVersion = libs.versions.mkdocsMaterial.get()
        val pip = "$venv/bin/pip"
        execute("python -m venv $venv")
        execute("$pip install mkdocs-material==$mkdocsMaterialVersion")
        execute("$pip install mkdocs-htmlproofer-plugin")
        execute("$pip install mike")
    }
}

tasks.register<Exec>("buildSite") {
    dependsOn("checkDocs", "installMkDocs")
    val pushSite = findProperty("pushSite")?.let { if (it == "true") "--push " else "" } ?: ""
    val mike = "$venv/bin/mike"
    val rootVersion = rootProject.version.toString()
    val siteAlias = if (rootVersion.contains(Regex("-[AB]"))) "dev" else "stable"
    val majorVersion = "v" + rootVersion.split(".").first()
    val command = "$mike deploy $pushSite--update-aliases $majorVersion $siteAlias"
    environment["PATH"] = System.getenv("PATH") + ":$venv/bin"
    commandLine(command.split(" "))
}

tasks.register<Exec>("deleteSite") {
    dependsOn("installMkDocs")
    environment["PATH"] = System.getenv("PATH") + ":$venv/bin"
    commandLine("$venv/bin/mike delete --all".split(" "))
}

tasks.register<Exec>("defaultSite") {
    dependsOn("installMkDocs")
    environment["PATH"] = System.getenv("PATH") + ":$venv/bin"
    commandLine("$venv/bin/mike set-default stable".split(" "))
}

tasks.register<Exec>("serveSite") {
    dependsOn("checkDocs", "installMkDocs")
    environment["PATH"] = System.getenv("PATH") + ":$venv/bin"
    commandLine("$venv/bin/mike serve".split(" "))
}

tasks.withType<PublishToMavenLocal>().configureEach { enabled = false }
tasks.withType<PublishToMavenRepository>().configureEach { enabled = false }

private fun generateCoverageBadge() {
    val coverageReport = file("build/content/jacoco/jacoco.xml")
    val coverageReportLength = coverageReport.length()
    val groups = coverageReport.reader().use {
        val buffer = CharArray(1024)
        it.skip(coverageReportLength - buffer.size)
        it.read(buffer)
        """</package>\s*<counter type="INSTRUCTION" missed="(\d*)" covered="(\d*)"/>"""
            .toRegex()
            .findAll(String(buffer))
            .lastOrNull()
            ?.groupValues
            ?: error("No match found")
    }
    val missed = groups[1].toInt()
    val covered = groups[2].toInt()
    val total = missed + covered
    val percentage = floor((covered * 100.0) / total).toInt()

    val badge = file("build/content/img/coverage.svg")
    val svg = badge.readText().replace("\${coverage}", "$percentage%")
    badge.writeText(svg)
}

private fun generateDownloadBadge() {
    val downloadBadge = file("build/content/img/download.svg")
    val downloadSvg = downloadBadge.readText().replace("\${download}", "${rootProject.version}")
    downloadBadge.writeText(downloadSvg)
}

private fun Project.modulesPaths(path: String): List<File> =
    subprojects.map { sp -> sp.file(path) }.filter { it .exists() }

private fun overwrite(source: String, target: String) {
    project.file(source).copyTo(file(target), true)
}

private fun checkSampleCode(documentationFile: File, sourceFile: File, tag: String) {
    val fileTag = "// $tag"
    val documentationFileLines = documentationFile.readLines()
    val sourceFileLines = sourceFile.readLines()
    val documentation = documentationFileLines.slice(documentationFileLines.rangeOf(fileTag))
    val source = sourceFileLines.slice(sourceFileLines.rangeOf(fileTag))

    fun List<String>.strippedLines(): List<String> =
        map { it.trim() }.filter { it.isNotEmpty() }

    fun List<String>.text(): String =
        joinToString("\n").trimIndent()

    val documentationLines = documentation.strippedLines()
    if (documentationLines.isNotEmpty() && documentationLines != source.strippedLines())
        error("""
            Documentation $documentation does not match $source

            DOC -----------------------------------------------
            ${documentation.text()}

            SRC -----------------------------------------------
            ${source.text()}
        """.trimIndent())
}

private fun insertSamplesCode(parent: File, content: String): String =
    content.replace("@code (.*)".toRegex()) {
        try {
            val sampleLocation = it.groups[1]?.value?.trim() ?: error("Location expected")
            val url = java.net.URI("file:${parent.absolutePath}/$sampleLocation").toURL()
            val tag = "// ${url.query}"
            val lines = url.readText().lines()
            val text = lines.slice(lines.rangeOf(tag)).joinToString("\n").trimIndent()
            "```kotlin\n$text\n```"
        } catch (_: Exception) {
            val code = it.value
            println("ERROR: Unable to process '$code' in folder: '${parent.absolutePath}'")
            code
        }
    }

private fun fixCodeTabs(content: String): String =
    content
        .replace("""=== "(.*)"\n\n```""".toRegex(), "=== \"$1\"\n")
        .replace("    ```\n```", "    ```")

private fun List<String>.rangeOf(tag: String): IntRange {
    val start = indexOfFirst { it.contains(tag) } + 1
    val end = indexOfLast { it.contains(tag) } - 1
    return start .. end
}

private fun execute(command: String) {
    execute(command.split(" "))
}

private fun execute(command: List<String>) {
    Runtime
        .getRuntime()
        .exec(command.toTypedArray())
        .let { process ->
            process.waitFor()
            process.destroy()
        }
}
