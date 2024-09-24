
import org.w3c.dom.Document
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

extensions.configure<PublishingExtension> {
    publications {
        createPomPublication("hexagon_bom") { pomDom ->
            properties.set(mapOf("hexagon.version" to rootProject.version.toString()))

            withXml {
                listOf("dependencyManagement").forEach {
                    asElement().importElement(pomDom.firstElement(it))
                }
            }
        }

        createPomPublication("kotlin_pom") { pomDom ->
            val javaPlugin = extensions.getByType(JavaPluginExtension::class.java)
            val source = javaPlugin.sourceCompatibility.toString()
            val target = javaPlugin.targetCompatibility.toString()

            properties.set(mapOf(
                "kotlin.code.style" to "official",
                "downloadSources" to true.toString(),
                "downloadJavadocs" to true.toString(),
                "linkXRef" to false.toString(),
                "project.build.sourceEncoding" to Charsets.UTF_8.name(),
                "maven.compiler.source" to source,
                "maven.compiler.target" to target,
                "kotlin.compiler.jvmTarget" to target,
                "maven" to libs.versions.maven.get(),
                "kotlin.version" to libs.versions.kotlin.get(),
                "dokka.version" to libs.versions.dokka.get(),
                "junit.version" to libs.versions.junit.get(),
                "native.tools.version" to libs.versions.nativeTools.get(),
            ))

            withXml {
                listOf("dependencies", "build").forEach {
                    asElement().importElement(pomDom.firstElement(it))
                }
            }
        }

        createPomPublication("kotlin_lean_pom") { pomDom ->
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
                developerConnection.set("scm:git:git@github.com:hexagontk/hexagon.git")
                url.set("https://github.com/hexagontk/hexagon")
            }

            licenses {
                license {
                    name.set("The MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }

            developers {
                developer {
                    id.set("hexagontk")
                    name.set("Hexagon Toolkit")
                    email.set("project@hexagontk.com")
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
