import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Document
import org.w3c.dom.Element

project.extra["bintrayPublications"] = listOf("kotlinPom")

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/junit.gradle")
apply(from = "../gradle/bintray.gradle")

plugins {
    `maven-publish`
}

dependencies {
    "implementation"(project(":http_server_jetty"))
    "implementation"(project(":http_client_ahc"))
}

task("processTemplate") {
    dependsOn("test")

    val stringProperties: Map<String, *> = project.properties.filter { it.value is String }

    projectDir
        .listFiles { f -> f.isDirectory && f.name.startsWith("hexagon_") }
        ?.map { it.name }
        ?.forEach { dir ->
            copy {
                from("$projectDir/$dir")
                into("$buildDir/$dir")
                filter {
                    var content = it

                    stringProperties.entries.forEach { entry ->
                        val entryValue = entry.value.toString()
                        content = content.replace("\${project.${entry.key}}", entryValue)
                    }

                    content
                }
            }

            copy {
                from("$projectDir/src")
                into("$buildDir/$dir/src")
            }

            copy {
                from("$rootDir/gradle/wrapper")
                into("$buildDir/$dir/gradle/wrapper")
            }

            copy {
                from(rootDir.toString())
                into("$buildDir/$dir")
                include("gradlew", "gradlew.bat", ".editorconfig")
            }

            val ghUrl = "https://raw.githubusercontent.com/hexagonkt/hexagon"
            val logbackVersion = properties["logbackVersion"]
            val junitVersion = properties["junitVersion"]

            file("$buildDir/$dir/gradle.properties").writeText("""
                name=\${project.name}
                version=\${version}
                group=\${group}
                description=\${description}

                gradleScripts=$ghUrl/${rootProject.version}/gradle

                hexagonVersion=$rootProject.version
                logbackVersion=$logbackVersion

                junitVersion=$junitVersion
            """.trimIndent())
        }
}

publishing {
    publications {
        create<MavenPublication>("kotlinPom") {
            val pomFile = project.file("kotlin_pom/pom.xml")
            val pomDom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pomFile)

            fun Document.firstElement(name: String): Element =
                getElementsByTagName(name).item(0) as Element

            fun Element.appendElement(name: String, value: Any?): Element =
                appendChild(
                    ownerDocument.createElement(name).also { it.textContent = value.toString() }
                ) as Element

            fun Element.importElement(element: Element): Element =
                appendChild(ownerDocument.importNode(element, true)) as Element

            groupId = pomDom.firstElement("groupId").textContent
            artifactId = pomDom.firstElement("artifactId").textContent

            pom {
                packaging = pomDom.firstElement("packaging").textContent
                description.set(pomDom.firstElement("description").textContent)

                withXml {
                    val node = asElement()
                    val pomProperties = node.importElement(pomDom.firstElement("properties"))
                    val gradleProperties = project.properties
                    pomProperties.appendElement("kotlin.version", gradleProperties["kotlinVersion"])
                    pomProperties.appendElement("hexagon.version", rootProject.version)
                    node.importElement(pomDom.firstElement("repositories"))
                    node.importElement(pomDom.firstElement("dependencyManagement"))
                    node.importElement(pomDom.firstElement("build"))
                }
            }
        }
    }
}
