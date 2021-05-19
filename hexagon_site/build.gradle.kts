
import kotlin.math.floor
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/icons.gradle")

repositories {
    mavenCentral()
}

tasks.named<Delete>("clean") {
    delete("build", "content")
}

// TODO Declare inputs. Check that no Gradle warnings are present when running 'serveSite'
tasks.register<JacocoReport>("jacocoRootReport") {
    dependsOn(rootProject.getTasksByName("jacocoTestReport", true))
    executionData.from(fileTree(rootDir) { include("**/build/jacoco/*.exec") })
    sourceDirectories.from(
        rootProject.modulesPaths("src/main/kotlin") +
            rootProject.modulesPaths("build/jacoco/src")
    )
    classDirectories.from(rootProject.modulesPaths("build/classes/kotlin/main"))

    reports {
        html.isEnabled = true
        xml.isEnabled = true

        val reportsOutput = file("content/jacoco").also { it.mkdirs() }
        html.outputLocation.set(reportsOutput)
        xml.outputLocation.set(reportsOutput.resolve("jacoco.xml"))
    }
}

val dokkaConfiguration = mapOf(
    "org.jetbrains.dokka.base.DokkaBase" to """{
        "customStyleSheets": [ "${rootProject.file("hexagon_site/assets/css/dokka.css")}" ],
        "footerMessage": "<div style=\"color: red\">custom message</div>"
    }"""
)

rootProject.tasks.named<DokkaMultiModuleTask>("dokkaHtmlMultiModule") {
    outputDirectory.set(rootProject.file("hexagon_site/content/api"))
    pluginsMapConfiguration.set(dokkaConfiguration)
}

rootProject
    .getTasksByName("dokkaHtmlPartial", true)
    .filterIsInstance<org.jetbrains.dokka.gradle.DokkaTaskPartial>()
    .forEach { it.pluginsMapConfiguration.set(dokkaConfiguration) }

task("mkdocs") {
    dependsOn(rootProject.tasks["dokkaHtmlMultiModule"], tasks["jacocoRootReport"])

    doLast {
        val contentTarget = project.file("content").absolutePath

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

        val markdownFiles = fileTree("dir" to contentTarget, "include" to "**/*.md")
        markdownFiles.forEach { markdownFile ->
            var content = markdownFile.readText()
            content = insertSamplesCode(rootProject.projectDir, content)
            content = fixCodeTabs(content)
            markdownFile.writeText(content)
        }

        project.file("content/CNAME").writeText(findProperty("sslDomain").toString())

        generateCoverageBadge()
        generateDownloadBadge()
    }
}

task("checkDocs") {
    dependsOn("mkdocs")
    doLast {
        val readme = rootProject.file("README.md")
        val service = rootProject.file("http_server_jetty/src/test/kotlin/HelloWorld.kt")
        val examples = "port_http_server/src/test/kotlin/examples"

        checkSamplesCode(FileRange (readme, "hello"), FileRange(service))
        checkSamplesCode(
            FilesRange(readme, rootProject.file("$examples/BooksTest.kt"), "books"),
            FilesRange(readme, rootProject.file("$examples/SessionTest.kt"), "session"),
            FilesRange(readme, rootProject.file("$examples/CookiesTest.kt"), "cookies"),
            FilesRange(readme, rootProject.file("$examples/ErrorsTest.kt"), "errors"),
            FilesRange(readme, rootProject.file("$examples/FiltersTest.kt"), "filters"),
            FilesRange(readme, rootProject.file("$examples/FilesTest.kt"), "files"),
            FilesRange(readme, rootProject.file("$examples/CorsTest.kt"), "cors"),
            FilesRange(readme, rootProject.file("$examples/HttpsTest.kt"), "https"),
            FilesRange(readme, rootProject.file("$examples/ZipTest.kt"), "zip")
        )

        val contentTarget = project.file("content").absolutePath
        val markdownFiles = project.fileTree("dir" to contentTarget, "include" to "**/*.md")

        markdownFiles.forEach { markdownFile ->
            if (markdownFile.readText().contains("@code")) {
                val message = "${markdownFile.absolutePath} Contains `@code` placeholder"
                throw GradleException(message)
            }
        }
    }
}

val dockerCommand = "docker --log-level warning run --rm -v ${projectDir.absolutePath}:/docs"
val mkdocsMaterialImage = "squidfunk/mkdocs-material:${properties["mkdocsMaterialVersion"]}"

tasks.register<Exec>("serveSite") {
    dependsOn("checkDocs")
    commandLine("$dockerCommand -p 8000:8000 --name hexagon_site $mkdocsMaterialImage".split(" "))
}

tasks.register<Exec>("buildSite") {
    dependsOn("checkDocs")
    commandLine("$dockerCommand $mkdocsMaterialImage build -csq".split(" "))
}

tasks.withType<PublishToMavenLocal>().configureEach { enabled = false }
tasks.withType<PublishToMavenRepository>().configureEach { enabled = false }

fun generateCoverageBadge() {
    val coverageReport = file("content/jacoco/jacoco.xml")
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

    val badge = file("content/img/coverage.svg")
    val svg = badge.readText().replace("\${coverage}", "$percentage%")
    badge.writeText(svg)
}

fun generateDownloadBadge() {
    val downloadBadge = file("content/img/download.svg")
    val downloadSvg = downloadBadge.readText().replace("\${download}", "${rootProject.version}")
    downloadBadge.writeText(downloadSvg)
}
