
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
description = "Hexagon YAML serialization format (using Jackson)."

dependencies {
    val jacksonVersion = libs.versions.jackson.get()

    "api"(project(":serialization:serialization_jackson"))
    "api"("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")

    "testImplementation"(project(":serialization:serialization_test"))
}
