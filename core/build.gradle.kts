
plugins {
    id("java-library")
}

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/detekt.gradle")

description = "Hexagon core utilities. Includes serialization and logging helpers."

dependencies {
    "api"("org.jetbrains.kotlin:kotlin-stdlib")

    "testImplementation"("org.jetbrains.kotlin:kotlin-reflect")
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
