
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")

if (findProperty("fullBuild") != null) {
    apply(from = "$rootDir/gradle/publish.gradle")
    apply(from = "$rootDir/gradle/dokka.gradle")
    apply(from = "$rootDir/gradle/detekt.gradle")
}

description = "Hexagon serialization testing helpers."

dependencies {
    "api"(project(":serialization:serialization"))
    "api"("org.jetbrains.kotlin:kotlin-test-junit5")
}
