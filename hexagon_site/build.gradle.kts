
apply(from = "../gradle/icons.gradle")
apply(plugin = "org.jetbrains.dokka")

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
        val starters = "hexagon_starters/src/main/kotlin"
        val service = rootProject.file("$starters/com/hexagonkt/starters/Service.kt")
        val examplesSources = "port_http_server/src/test/kotlin"
        val examples = "$examplesSources/com/hexagonkt/http/server/examples"

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

        addMetadata(contentTarget, project)
        project.file("content/CNAME").writeText(findProperty("sslDomain").toString())
    }
}
