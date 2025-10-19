
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/lean.gradle")

apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")

group = "com.hexagontk.serialization"
description = "Hexagon CSV serialization format (using Jackson)."

dependencies {
    val jacksonVersion = libs.versions.jackson.get()

    "api"(project(":serialization:serialization_jackson"))
    "api"(platform("tools.jackson:jackson-bom:$jacksonVersion"))
    "api"("tools.jackson.dataformat:jackson-dataformat-csv")
}
