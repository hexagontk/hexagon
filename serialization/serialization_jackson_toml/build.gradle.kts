
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/lean.gradle")

apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")

group = "com.hexagontk.serialization"
description = "Hexagon TOML serialization format (using Jackson)."

dependencies {
    val jacksonVersion = libs.versions.jackson.get()

    "api"(project(":serialization:serialization_jackson"))
    "api"(platform("com.fasterxml.jackson:jackson-bom:$jacksonVersion"))
    "api"("com.fasterxml.jackson.dataformat:jackson-dataformat-toml")

    "testImplementation"(project(":serialization:serialization_test"))
}
