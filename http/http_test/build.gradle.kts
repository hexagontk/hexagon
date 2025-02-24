
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/lean.gradle")

apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")

group = "com.hexagontk.http"
description = "Test cases for HTTP client and server adapters."

dependencies {
    val slf4jVersion = libs.versions.slf4j.get()

    "api"(project(":serialization:serialization"))
    "api"(project(":http:http_client"))
    "api"(project(":http:http_server"))
    "api"("org.jetbrains.kotlin:kotlin-test-junit5")
    "api"("org.slf4j:log4j-over-slf4j:$slf4jVersion")
    "api"("org.slf4j:jcl-over-slf4j:$slf4jVersion")
    "api"("org.slf4j:slf4j-jdk14:$slf4jVersion")
}
