
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

description = "Hexagon SLF4J logging adapter (using JUL as SLF4J engine)."

dependencies {
    val slf4jVersion = properties["slf4jVersion"]

    "api"(project(":logging:logging_jul"))
    "api"("org.slf4j:slf4j-jdk14:$slf4jVersion")
    "api"("org.slf4j:jcl-over-slf4j:$slf4jVersion")
    "api"("org.slf4j:log4j-over-slf4j:$slf4jVersion")
}
