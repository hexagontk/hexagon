
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")

description = "HTTP classes. These classes are shared among the HTTP client and the HTTP server."

dependencies {
    "api"(project(":core"))
}
