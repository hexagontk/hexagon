
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/detekt.gradle")

description = "Hexagon serialization module."

extra["basePackage"] = "com.hexagonkt.serialization"

dependencies {
    "api"(project(":core"))
}
