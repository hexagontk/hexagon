
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/lean.gradle")

apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")

group = "com.hexagontk.serialization"
description = "Jackson's serialization utilities (used in several serialization formats)."

dependencies {
    val jacksonVersion = libs.versions.jackson.get()

    "api"(project(":serialization:serialization"))
    "api"("tools.jackson.core:jackson-databind:$jacksonVersion")

    "testImplementation"("org.jetbrains.kotlin:kotlin-reflect")
}
