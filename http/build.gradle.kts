
plugins {
    id("java-library")
}

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/native.gradle")

description = "HTTP classes. These classes are shared among the HTTP client and the HTTP server."

dependencies {
    "api"(project(":core"))
}
