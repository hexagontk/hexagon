
plugins {
    id("java-library")
    id("nu.studer.rocker") version("3.0.4")
}

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/native.gradle")

dependencies {
    val rockerVersion = properties["rockerVersion"]

    "api"(project(":templates"))
    "api"("com.fizzed:rocker-runtime:$rockerVersion")

    "testImplementation"(project(":templates_test"))
    "testImplementation"(project(":serialization_jackson_json"))
}

rocker {
    configurations {
        create("test") {
            templateDir.set(file("src/test/resources"))
            optimize.set(true)
        }
    }
}

tasks.named("dokkaJavadoc") { dependsOn("compileTestJava") }
