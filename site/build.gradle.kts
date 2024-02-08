
import kotlin.math.floor
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask
import org.jetbrains.dokka.gradle.DokkaTaskPartial

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/icons.gradle")

val venv: String = "build/mkdocs"

tasks.register<JacocoReport>("jacocoRootReport") {
    dependsOn(":dokkaHtmlMultiModule")

    val projectExecutionData = fileTree(rootDir) { include("**/build/jacoco/*.exec") }
    val modulesSources = rootProject.modulesPaths("src/main/kotlin")
    val modulesClasses = rootProject.modulesPaths("build/classes/kotlin/main")
        .asSequence()
        .filterNot { it.absolutePath.contains("http_test") }
        .filterNot { it.absolutePath.contains("serialization_test") }
        .filterNot { it.absolutePath.contains("templates_test") }
        .filterNot { it.absolutePath.contains("rest") }
        .filterNot { it.absolutePath.contains("rest_tools") }
        .filterNot { it.absolutePath.contains("serverless_http") }
        .filterNot { it.absolutePath.contains("serverless_http_google") }
        .filterNot { it.absolutePath.contains("web") }
        .toList()
        // TODO Include the filtered modules when they are ready

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

val footer = file("footer.txt").readLines().joinToString(" ") { it.trim() }

val dokkaConfiguration =
    mapOf("org.jetbrains.dokka.base.DokkaBase" to """{ "footerMessage": "$footer" }""")

rootProject.tasks.named<DokkaMultiModuleTask>("dokkaHtmlMultiModule") {
    outputDirectory.set(rootProject.file("site/build/content/api"))
    pluginsMapConfiguration.set(dokkaConfiguration)
}

rootProject
    .getTasksByName("dokkaHtmlPartial", true)
    .filterIsInstance<DokkaTaskPartial>()
    .forEach { it.pluginsMapConfiguration.set(dokkaConfiguration) }

task("mkDocs") {
    dependsOn(rootProject.tasks["dokkaHtmlMultiModule"], tasks["jacocoRootReport"])
    dependsOn("icons")

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
        overwrite("$contentTarget/api/core/dokka-mermaid.js", "$contentTarget/api/dokka-mermaid.js")
        project.file("build/content/api/styles/main.css")
            .appendText(project.file("assets/css/dokka.css").readText())

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

task("checkDocs") {
    dependsOn("mkDocs")
    doLast {
        val readme = rootProject.file("README.md")
        val helloWorld = "com/hexagonkt/http/server/jetty/HelloWorldTest.kt"
        val service = rootProject.file("http/http_server_jetty/src/test/kotlin/$helloWorld")
        val examples = "http/http_test/src/main/kotlin/com/hexagonkt/http/test/examples"

        checkSampleCode(readme, rootProject.file(service), "hello_world")
        checkSampleCode(readme, rootProject.file("$examples/BooksTest.kt"), "books")
        checkSampleCode(readme, rootProject.file("$examples/CookiesTest.kt"), "cookies")
        checkSampleCode(readme, rootProject.file("$examples/ErrorsTest.kt"), "errors")
        checkSampleCode(readme, rootProject.file("$examples/FiltersTest.kt"), "filters")
        checkSampleCode(readme, rootProject.file("$examples/FilesTest.kt"), "files")
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
        val mkdocsMaterialVersion = properties["mkdocsMaterialVersion"]
        exec { commandLine("python -m venv $venv".split(" ")) }
        exec { commandLine("$venv/bin/pip install mkdocs-material==$mkdocsMaterialVersion".split(" ")) }
        exec { commandLine("$venv/bin/pip install mkdocs-htmlproofer-plugin".split(" ")) }
    }
}

tasks.register<Exec>("serveSite") {
    dependsOn("checkDocs", "installMkDocs")
    commandLine("$venv/bin/mkdocs serve".split(" "))
}

tasks.register<Exec>("buildSite") {
    dependsOn("checkDocs", "installMkDocs")
    commandLine("$venv/bin/mkdocs build -cs".split(" "))
}

tasks.withType<PublishToMavenLocal>().configureEach { enabled = false }
tasks.withType<PublishToMavenRepository>().configureEach { enabled = false }

fun generateCoverageBadge() {
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

fun generateDownloadBadge() {
    val downloadBadge = file("build/content/img/download.svg")
    val downloadSvg = downloadBadge.readText().replace("\${download}", "${rootProject.version}")
    downloadBadge.writeText(downloadSvg)
}

fun Project.modulesPaths(path: String): List<File> =
    subprojects.map { sp -> sp.file(path) }.filter { it .exists() }

fun overwrite(source: String, target: String) {
    project.file(source).copyTo(file(target), true)
}

fun checkSampleCode(documentationFile: File, sourceFile: File, tag: String) {
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

fun insertSamplesCode(parent: File, content: String): String =
    content.replace("@code (.*)".toRegex()) {
        try {
            val sampleLocation = it.groups[1]?.value?.trim() ?: error("Location expected")
            val url = java.net.URI("file:${parent.absolutePath}/$sampleLocation").toURL()
            val tag = "// ${url.query}"
            val lines = url.readText().lines()
            val text = lines.slice(lines.rangeOf(tag)).joinToString("\n").trimIndent()
            "```kotlin\n$text\n```"
        } catch (e: Exception) {
            val code = it.value
            println("ERROR: Unable to process '$code' in folder: '${parent.absolutePath}'")
            code
        }
    }

fun fixCodeTabs(content: String): String =
    content
        .replace("""=== "(.*)"\n\n```""".toRegex(), "=== \"$1\"\n")
        .replace("    ```\n```", "    ```")

fun List<String>.rangeOf(tag: String): IntRange {
    val start = indexOfFirst { it.contains(tag) } + 1
    val end = indexOfLast { it.contains(tag) } - 1
    return start .. end
}
