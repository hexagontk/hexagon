
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
}

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/detekt.gradle")

description = "HTTP classes. These classes are shared among the HTTP client and the HTTP server."

dependencies {
    api(project(":core"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
}
