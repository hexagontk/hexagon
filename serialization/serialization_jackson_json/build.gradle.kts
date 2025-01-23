
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/lean.gradle")

if (findProperty("fullBuild") != null) {
    apply(from = "$rootDir/gradle/publish.gradle")
    apply(from = "$rootDir/gradle/dokka.gradle")
    apply(from = "$rootDir/gradle/native.gradle")
}

group = "com.hexagontk.serialization"
description = "Hexagon JSON serialization format (using Jackson)."

dependencies {
    "api"(project(":serialization:serialization_jackson"))
    "testImplementation"(project(":serialization:serialization_test"))
}
