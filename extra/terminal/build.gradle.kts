
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")

if (findProperty("fullBuild") != null) {
    apply(from = "$rootDir/gradle/publish.gradle")
    apply(from = "$rootDir/gradle/dokka.gradle")
    apply(from = "$rootDir/gradle/detekt.gradle")
    apply(from = "$rootDir/gradle/native.gradle")
}

description = "Hexagon core utilities. Includes serialization and logging helpers."

dependencies {
    "api"("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    "api"(project(":extra:helpers"))
}
