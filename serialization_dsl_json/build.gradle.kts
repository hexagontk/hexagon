
plugins {
    id("java-library")
}

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

description = "Hexagon JSON serialization format (using DSL-JSON)."

dependencies {
    val dslJsonVersion = properties["dslJsonVersion"]

    "api"(project(":serialization"))
    "api"("com.dslplatform:dsl-json-java8:$dslJsonVersion")

    "testImplementation"("org.jetbrains.kotlin:kotlin-reflect")
    "testImplementation"(project(":serialization_test"))
}
