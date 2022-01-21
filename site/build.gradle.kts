
import kotlin.math.floor
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask
import org.jetbrains.dokka.gradle.DokkaTaskPartial

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/icons.gradle")

repositories {
    mavenCentral()
}

// TODO Declare inputs. Check that no Gradle warnings are present when running 'serveSite'
tasks.register<JacocoReport>("jacocoRootReport") {

    val projectExecutionData = fileTree(rootDir) { include("**/build/jacoco/*.exec") }
    val modulesSources = rootProject.modulesPaths("src/main/kotlin")
    val modulesJacocoSources = rootProject.modulesPaths("build/jacoco/src")
    val modulesClasses = rootProject.modulesPaths("build/classes/kotlin/main")
        .filterNot { it.absolutePath.contains("http_test") }

    executionData.from(projectExecutionData)
    sourceDirectories.from(modulesSources + modulesJacocoSources)
    classDirectories.from(modulesClasses)

    reports {
        html.required.set(true)
        xml.required.set(true)

        val reportsOutput = file("build/content/jacoco").also { it.mkdirs() }
        html.outputLocation.set(reportsOutput)
        xml.outputLocation.set(reportsOutput.resolve("jacoco.xml"))
    }
}

val footer = """
    Made with <svg class=\"fa-heart\" xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 512 512\">
    <path d=\"M462.3 62.6C407.5 15.9 326 24.3 275.7 76.2L256 96.5l-19.7-20.3C186.1 24.3 104.5 15.9
    49.7 62.6c-62.8 53.6-66.1 149.8-9.9 207.9l193.5 199.8c12.5 12.9 32.8 12.9 45.3
    0l193.5-199.8c56.3-58.1 53-154.3-9.8-207.9z\"></path></svg> by
    <a href=\"https://github.com/hexagonkt/hexagon/graphs/contributors\">OSS contributors</a>.
    Licensed under <a href=\"https://github.com/hexagonkt/hexagon/blob/master/license.md\">
    MIT License</a>
""".lines().joinToString(" ") { it.trim() }

val dokkaConfiguration = mapOf(
    "org.jetbrains.dokka.base.DokkaBase" to """{
        "footerMessage": "$footer"
    }"""
)

// TODO Make this task depend on 'assets' directory to update it upon changes on those CSS files
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

fun overwrite(source: String, target: String) {
    project.file(source).copyTo(file(target), true)
}

task("checkDocs") {
    dependsOn("mkDocs")
    doLast {
        val readme = rootProject.file("README.md")
        val service = rootProject.file("http_test/src/test/kotlin/HelloWorldTest.kt")
        val examples = "http_test/src/main/kotlin/examples"

        checkSamplesCode()
        checkSamplesCode(
            FilesRange(readme, rootProject.file(service), "hello_world"),
            FilesRange(readme, rootProject.file("$examples/BooksTest.kt"), "books"),
//            FilesRange(readme, rootProject.file("$examples/SessionTest.kt"), "session"),
            FilesRange(readme, rootProject.file("$examples/CookiesTest.kt"), "cookies"),
            FilesRange(readme, rootProject.file("$examples/ErrorsTest.kt"), "errors"),
            FilesRange(readme, rootProject.file("$examples/FiltersTest.kt"), "filters"),
            FilesRange(readme, rootProject.file("$examples/FilesTest.kt"), "files"),
            FilesRange(readme, rootProject.file("$examples/CorsTest.kt"), "cors"),
            FilesRange(readme, rootProject.file("$examples/HttpsTest.kt"), "https"),
            FilesRange(readme, rootProject.file("$examples/ZipTest.kt"), "zip")
        )

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

tasks.register<Exec>("installMkDocs") {
    val mkdocsMaterialVersion = properties["mkdocsMaterialVersion"]
    commandLine("pip install mkdocs-material==$mkdocsMaterialVersion".split(" "))
}

tasks.register<Exec>("serveSite") {
    dependsOn("checkDocs", "installMkDocs")
    commandLine("mkdocs serve".split(" "))
}

tasks.register<Exec>("buildSite") {
    dependsOn("checkDocs", "installMkDocs")
    commandLine("mkdocs build -cs".split(" "))
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
