import kotlin.math.floor

apply(from = "../gradle/icons.gradle")
apply(plugin = "org.jetbrains.dokka")
apply(plugin = "jacoco")

tasks.register<Delete>("clean") {
    delete("build", "content")
}

tasks.register<Exec>("serveSite") {
    dependsOn("mkdocs")
    workingDir = rootDir
    commandLine("docker-compose --log-level warning up -d site".split(" "))
}

tasks.register<Exec>("buildSite") {
    dependsOn("mkdocs")
    workingDir = rootDir
    commandLine("docker-compose --log-level warning run site build -csq".split(" "))
}

task("checkDocs") {
    dependsOn("mkdocs")
    doLast {
        val readme = rootProject.file("README.md")
        val service = rootProject.file("hexagon_starters/src/main/kotlin/Service.kt")
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
            FilesRange(readme, rootProject.file("$examples/HttpsTest.kt"), "https")
        )

        val contentTarget = project.file("content").absolutePath
        val markdownFiles = project.fileTree("dir" to contentTarget, "include" to "**/*.md")

        markdownFiles.forEach { markdownFile ->
            if (markdownFile.readText().contains("@sample")) {
                val message = "${markdownFile.absolutePath} Contains `@sample` placeholder"
                throw GradleException(message)
            }
        }
    }
}

task("mkdocs") {
    dependsOn("jacocoRootReport")
    doLast {
        val contentTarget = project.file("content").absolutePath
        val markdownFiles = fileTree("dir" to contentTarget, "include" to "**/*.md")

        copy {
            from(project.file("pages"))
            from(project.file("assets"))
            into(contentTarget)
        }

        // Hack to fix site tabs when two of them point to the same file
        listOf("hexagon_core", "port_http_server", "port_http_client", "port_store").forEach {
            copy {
                from(rootProject.file(it))
                include("README.md")
                into("$contentTarget/$it")
                rename("(.*)", "${it}.md")
            }
        }

        markdownFiles.forEach { markdownFile ->
            var content = markdownFile.readText()
            content = insertSamplesCode(rootProject.projectDir, content)
            markdownFile.writeText(content)
        }

        project.addMetadata(contentTarget)
        project.file("content/CNAME").writeText(findProperty("sslDomain").toString())

        // Generate coverage badge
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
}

repositories {
    mavenCentral()
}

tasks.register<JacocoReport>("jacocoRootReport") {
    dependsOn(rootProject.getTasksByName("check", true))

    executionData.from(fileTree(rootDir) { include("**/build/jacoco/*.exec") })
    sourceDirectories.from(rootProject.modulesPaths("src/main/kotlin"))
    classDirectories.from(rootProject.modulesPaths("build/classes/kotlin/main"))

    reports {
        html.isEnabled = true
        xml.isEnabled = true

        val reportsOutput = file("content/jacoco").also { it.mkdirs() }
        html.outputLocation.set(reportsOutput)
        xml.outputLocation.set(reportsOutput.resolve("jacoco.xml"))
    }
}
