
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

description = "Hexagon Logback logging adapter."

dependencies {
    val slf4jVersion = properties["slf4jVersion"]
    val logbackVersion = properties["logbackVersion"]

    "api"(project(":core"))
    "api"("ch.qos.logback:logback-classic:$logbackVersion") { exclude("org.slf4j") }
    "api"("org.slf4j:jul-to-slf4j:$slf4jVersion")
    "api"("org.slf4j:jcl-over-slf4j:$slf4jVersion")
    "api"("org.slf4j:log4j-over-slf4j:$slf4jVersion")
}
