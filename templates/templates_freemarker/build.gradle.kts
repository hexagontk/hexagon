
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/lean.gradle")

apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")

group = "com.hexagontk.templates"
description = "Template processor adapter for Freemarker."

dependencies {
    val freemarkerVersion = libs.versions.freemarker.get()

    "api"(project(":templates:templates"))
    "api"("org.freemarker:freemarker:$freemarkerVersion")

    "testImplementation"(project(":templates:templates_test"))
    "testImplementation"(project(":serialization:serialization_jackson_json"))
}
