
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/lean.gradle")

apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")

group = "com.hexagontk.templates"
description = "Test suite to verify template processing adapters."

dependencies {
    "api"(project(":templates:templates"))
    "api"("org.jetbrains.kotlin:kotlin-test-junit5")
}
