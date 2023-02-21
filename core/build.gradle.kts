
plugins {
    id("java-library")
}

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

description = "Hexagon core utilities. Includes logging helpers."

dependencies {
    "api"("org.jetbrains.kotlin:kotlin-stdlib")

    "testImplementation"("org.jetbrains.kotlin:kotlin-reflect")
}
