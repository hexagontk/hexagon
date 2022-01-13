
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/detekt.gradle")

description = "Hexagon JSON serialization format (using Jackson)."

extra["basePackage"] = "com.hexagonkt.serialization.jackson.json"

dependencies {
    "api"(project(":serialization_jackson"))
    "testImplementation"(project(":serialization_test"))
}
