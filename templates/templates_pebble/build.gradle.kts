
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

dependencies {
    "api"(project(":templates:templates"))
    "api"("io.pebbletemplates:pebble:${properties["pebbleVersion"]}")

    "testImplementation"(project(":templates:templates_test"))
    "testImplementation"(project(":serialization:serialization_jackson_json"))
}
