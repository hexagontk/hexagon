
plugins {
    id("java-library")
}

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/detekt.gradle")

description = "Jackson's serialization utilities (used in several serialization formats)."

dependencies {
    val jacksonVersion = properties["jacksonVersion"]

    "api"(project(":serialization"))
    "api"(platform("com.fasterxml.jackson:jackson-bom:$jacksonVersion"))
    "api"("com.fasterxml.jackson.core:jackson-databind")
    "api"("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    "testImplementation"("org.jetbrains.kotlin:kotlin-reflect")
    "testImplementation"("com.fasterxml.jackson.module:jackson-module-kotlin") {
        exclude("org.jetbrains.kotlin")
    }
}
