
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

group = "com.hexagontk.templates"
description = "Template processor adapter for Pebble."

dependencies {
    val pebbleVersion = libs.versions.pebble.get()

    "api"(project(":templates:templates"))
    "api"("io.pebbletemplates:pebble:$pebbleVersion")

    "testImplementation"(project(":templates:templates_test"))
    "testImplementation"(project(":serialization:serialization_jackson_json"))
}
