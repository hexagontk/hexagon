
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/detekt.gradle")

description = "Hexagon core utilities. Includes DI, serialization, http and settings helpers."

extra["basePackage"] = "com.hexagonkt.settings"

dependencies {
    "api"(project(":hexagon_core"))

    "testImplementation"(project(":serialization_yaml"))
}
