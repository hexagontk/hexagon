
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/lean.gradle")

apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")

group = "com.hexagontk.store"
description = "."

dependencies {
    val mongodbVersion = libs.versions.mongodb.get()
    val testcontainersVersion = libs.versions.testcontainers.get()
    val commonsCompressVersion = libs.versions.commonsCompress.get()

    "api"(project(":core"))
    "api"(project(":store:store"))
    "api"("org.mongodb:mongodb-driver-sync:$mongodbVersion")

    "testImplementation"("org.apache.commons:commons-compress:$commonsCompressVersion")
    "testImplementation"("org.testcontainers:mongodb:$testcontainersVersion") {
        exclude(module = "commons-compress")
    }
}
