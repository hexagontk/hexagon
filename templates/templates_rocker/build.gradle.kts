
plugins {
    id("java-library")
    id("nu.studer.rocker") version("3.0.4")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

dependencies {
    val rockerVersion = properties["rockerVersion"]

    "api"(project(":templates:templates"))
    "api"("com.fizzed:rocker-runtime:$rockerVersion")

    "testImplementation"(project(":templates:templates_test"))
    "testImplementation"(project(":serialization:serialization_jackson_json"))
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
