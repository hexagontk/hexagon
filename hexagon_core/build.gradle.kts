
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/detekt.gradle")

description = "Hexagon core utilities. Includes DI, serialization, http and settings helpers."

extra["basePackage"] = "com.hexagonkt"

dependencies {
    "testImplementation"(project(":serialization_yaml"))
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
