
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

val compileTestKotlin: KotlinCompile by tasks

plugins {
    java
}

// IMPORTANT: Required for compiling classes in test dependencies. It *MUST* be before dependencies
compileTestKotlin.dependsOn(tasks.getByPath(":port_templates:compileTestKotlin"))

val entityTests: SourceSetOutput = project(":port_templates").sourceSets["test"].output

dependencies {
    "api"(project(":port_templates"))
    "api"("io.pebbletemplates:pebble:${properties["pebbleVersion"]}") {
        exclude (module = "slf4j-api")
    }
    "testImplementation"(entityTests)
}
