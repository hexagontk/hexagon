
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/lean.gradle")

apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")

description = "Hexagon helpers. Not used inside the toolkit but useful for applications."

dependencies {
    "api"(project(":core"))

    "testImplementation"("org.jetbrains.kotlin:kotlin-reflect")
}
