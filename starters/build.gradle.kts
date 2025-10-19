
import org.w3c.dom.Document
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")

description = "Project starters."

extensions.configure<PublishingExtension> {
    publications {
        createPomPublication("hexagon_bom") { pomDom ->
            properties.set(mapOf(
                "hexagon.version" to rootProject.version.toString(),
                "helidon.version" to libs.versions.helidon.get(),
                "jetty.version" to libs.versions.jetty.get(),
                "netty.version" to libs.versions.netty.get(),
                "jackson.version" to libs.versions.jackson.get(),
                "junit.version" to libs.versions.junit.get(),
            ))

            withXml {
                listOf("dependencyManagement").forEach {
                    asElement().importElement(pomDom.firstElement(it))
                }
            }
        }

        createPomPublication("kotlin_pom") { pomDom ->
            val javaPlugin = extensions.getByType(JavaPluginExtension::class.java)
            val target = javaPlugin.targetCompatibility.toString()

            properties.set(mapOf(
                "kotlin.code.style" to "official",
                "downloadSources" to true.toString(),
                "downloadJavadocs" to true.toString(),
                "linkXRef" to false.toString(),
                "project.build.sourceEncoding" to Charsets.UTF_8.name(),
                "maven.compiler.release" to target,
                "kotlin.compiler.jvmTarget" to target,
                "maven" to libs.versions.maven.get(),
                "kotlin.version" to libs.versions.kotlin.get(),
                "dokka.version" to libs.versions.dokka.get(),
                "native.tools.version" to libs.versions.nativeTools.get(),
                "jacoco.version" to libs.versions.jacoco.get(),
                "jmh.version" to libs.versions.jmh.get(),
                "junit.version" to libs.versions.junit.get(),
                "jlink.phase" to "none",
                "jlink.launcher" to $$"${project.artifactId}=${project.groupId}.${project.artifactId}/${exec.mainClass}",
                "skip.executable" to "true",
                "doxia-module-markdown.version" to "2.0.0",
                "maven-antrun-plugin.version" to "3.1.0",
                "maven-assembly-plugin.version" to "3.7.1",
                "maven-clean-plugin.version" to "3.5.0",
                "maven-compiler-plugin.version" to "3.14.1",
                "maven-gpg-plugin.version" to "3.2.8",
                "maven-install-plugin.version" to "3.1.4",
                "maven-jar-plugin.version" to "3.4.2",
                "maven-javadoc-plugin.version" to "3.11.3",
                "maven-jlink-plugin.version" to "3.2.0",
                "maven-resources-plugin.version" to "3.3.1",
                "maven-site-plugin.version" to "3.21.0",
                "maven-source-plugin.version" to "3.3.1",
                "maven-surefire-plugin.version" to "3.5.3",
            ))

            withXml {
                listOf("dependencyManagement", "dependencies", "build", "profiles").forEach {
                    asElement().importElement(pomDom.firstElement(it))
                }
            }
        }

        createPomPublication("hexagon_pom") { pomDom ->
            properties.set(mapOf("hexagon.version" to rootProject.version.toString()))

            withXml {
                val root = asElement()
                val document = root.ownerDocument

                val parent = document.createElement("parent")

                val parentGroupId = document.createElement("groupId")
                val parentArtifactId = document.createElement("artifactId")
                val parentVersion = document.createElement("version")

                parentGroupId.textContent = rootProject.group.toString()
                parentArtifactId.textContent = "kotlin_pom"
                parentVersion.textContent = rootProject.version.toString()

                parent.appendChild(parentGroupId)
                parent.appendChild(parentArtifactId)
                parent.appendChild(parentVersion)

                root.appendChild(parent)

                listOf("dependencyManagement").forEach {
                    root.importElement(pomDom.firstElement(it))
                }
            }
        }
    }
}

fun File.parseDom(): Document =
    DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this)

fun Document.firstElement(name: String): Element =
    getElementsByTagName(name).item(0) as Element

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
