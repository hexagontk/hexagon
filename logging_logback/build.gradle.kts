
plugins {
    id("java-library")
}

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/detekt.gradle")

description = "Hexagon Logback logging adapter."

dependencies {
    val slf4jVersion = properties["slf4jVersion"]
    val logbackVersion = properties["logbackVersion"]

    "api"(project(":logging_slf4j_jul")) { exclude(module = "slf4j-jdk14") }
    "api"("ch.qos.logback:logback-classic:$logbackVersion") { exclude("org.slf4j") }
    "api"("org.slf4j:jul-to-slf4j:$slf4jVersion")
}
