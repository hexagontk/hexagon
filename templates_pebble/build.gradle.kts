
plugins {
    id("java-library")
}

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/detekt.gradle")

dependencies {
    "api"(project(":templates"))
    "api"("io.pebbletemplates:pebble:${properties["pebbleVersion"]}")

    "testImplementation"(project(":templates_test"))
    "testImplementation"(project(":serialization_jackson_json"))
}
