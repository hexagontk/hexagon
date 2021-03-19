
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/detekt.gradle")

description = "Hexagon core utilities. Includes DI, serialization, http and settings helpers."

extra["basePackage"] = "com.hexagonkt"

dependencies {
    val jacksonVersion = properties["jacksonVersion"]

    "api"("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")
    "api"("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    "api"("com.fasterxml.jackson.module:jackson-module-parameter-names:$jacksonVersion")
//    "api"("com.fasterxml.jackson.module:jackson-module-blackbird:$jacksonVersion")
//    "api"("com.fasterxml.jackson.module:jackson-module-afterburner:$jacksonVersion")
    "api"("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion") {
        exclude("org.jetbrains.kotlin")
    }

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
