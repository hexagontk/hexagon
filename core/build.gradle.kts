
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.gradle.DokkaTaskPartial

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/detekt.gradle")

description = "Hexagon core utilities. Includes serialization and logging helpers."

extra["basePackage"] = "com.hexagonkt"

dependencies {
    val kotlinVersion = properties["kotlinVersion"]

    "api"("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
}

task("hexagonInfo") {
    group = "build"
    description = "Add `META-INF/hexagon.properties` file (with toolkit variables) to the package."

    doLast {
        file("$buildDir/resources/main/META-INF").mkdirs()
        file("$buildDir/resources/main/META-INF/hexagon.properties").writeText("""
            project=${rootProject.name}
            module=${project.name}
            version=${project.version}
            group=${project.group}
            description=${project.description}
        """.trimIndent ())
    }
}

tasks.getByName("classes").dependsOn("hexagonInfo")

extensions.configure<PublishingExtension> {
    (publications["mavenJava"] as MavenPublication).artifact(tasks.named("testJar"))
}

setUpDokka(tasks.getByName<DokkaTaskPartial>("dokkaHtmlPartial"))
setUpDokka(tasks.getByName<DokkaTask>("dokkaJavadoc"))

fun setUpDokka(dokkaTask: DokkaTaskPartial) {
    dokkaTask.dokkaSourceSets {
        configureEach {
            sourceRoots.from(file("src/test/kotlin"))
        }
    }
}

fun setUpDokka(dokkaTask: DokkaTask) {
    dokkaTask.dokkaSourceSets {
        configureEach {
            sourceRoots.from(file("src/test/kotlin"))
        }
    }
}
