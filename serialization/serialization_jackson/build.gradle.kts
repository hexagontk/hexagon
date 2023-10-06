
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

description = "Jackson's serialization utilities (used in several serialization formats)."

dependencies {
    val jacksonVersion = properties["jacksonVersion"]

    "api"(project(":serialization:serialization"))
    "api"("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")

    "testImplementation"("org.jetbrains.kotlin:kotlin-reflect")
}
