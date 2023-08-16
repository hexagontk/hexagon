
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

description =
    "Template processing port. Supports template loading and context passing. " +
    "Allow multiple adapters at once."

dependencies {
    "api"(project(":core"))

    "testImplementation"(project(":serialization:serialization_jackson_yaml"))
}
