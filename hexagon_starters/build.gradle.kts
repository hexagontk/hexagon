import org.gradle.api.publish.maven.MavenPom
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Document
import org.w3c.dom.Element

apply(from = "../gradle/bintray.gradle")

plugins {
    `maven-publish`
}

publishing {
    publications {
        createPomPublication("hexagon_pom") { pomDom ->
            properties.set(mapOf(
                "project.build.sourceEncoding" to Charsets.UTF_8.name(),
                "kotlin.version" to project.properties["kotlinVersion"].toString(),
                "hexagon.version" to rootProject.version.toString()
            ))

            withXml {
                listOf("repositories", "dependencyManagement", "build").forEach {
                    asElement().importElement(pomDom.firstElement(it))
                }
            }
        }

        createPomPublication("hexagon_lean_pom") { pomDom ->
            withXml {
                val root = asElement()
                val version = rootProject.version.toString()
                listOf("parent", "build").forEach {
                    root.importElement(pomDom.firstElement(it))
                }
                root.ownerDocument.firstElement("parent").appendElement("version", version)
            }
        }
    }
}

fun File.parseDom(): Document =
    DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this)

fun Document.firstElement(name: String): Element =
    getElementsByTagName(name).item(0) as Element

fun Element.appendElement(name: String, value: Any?): Element =
    appendChild(
        ownerDocument.createElement(name).also { it.textContent = value.toString() }
    ) as Element

fun Element.importElement(element: Element): Element =
    appendChild(ownerDocument.importNode(element, true)) as Element

fun PublicationContainer.createPomPublication(
    artifact: String, block: MavenPom.(Document) -> Unit = {}) {

    create<MavenPublication>(artifact) {
        artifactId = this.name
        pom {
            packaging = "pom"

            scm {
                val vcsUrl = findProperty("vcsUrl") ?: error("'vcsUrl' property must be defined")

                connection.set("scm:git:$vcsUrl")
                developerConnection.set("scm:git:git@github.com:hexagonkt/hexagon.git")
                url.set("https://github.com/hexagonkt/hexagon")
            }

            licenses {
                license {
                    name.set("The MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }

            developers {
                developer {
                    id.set("hexagonkt")
                    name.set("Hexagon Toolkit")
                    email.set("project@hexagonkt.com")
                }
            }

            url.set(project.properties["siteHost"].toString())
            val pomDom = project.file("${artifactId}.xml").parseDom()
            name.set(pomDom.firstElement("name").textContent)
            description.set(pomDom.firstElement("description").textContent)
            block(pomDom)
        }
    }
}
