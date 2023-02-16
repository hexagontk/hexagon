
plugins {
    id("java-library")
}

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

description = "Hexagon helpers."

dependencies {
    "api"(project(":core"))

    "testImplementation"("org.jetbrains.kotlin:kotlin-reflect")
}
