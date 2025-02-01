
plugins {
    id("java-library")
    id("nu.studer.rocker") version("3.0.4")
    // TODO Check 'official' plugin
//    id("com.fizzed.rocker") version(libs.versions.rocker.get())
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/lean.gradle")

if (findProperty("fullBuild") != null) {
    apply(from = "$rootDir/gradle/publish.gradle")
    apply(from = "$rootDir/gradle/dokka.gradle")
    apply(from = "$rootDir/gradle/native.gradle")
}

group = "com.hexagontk.templates"
description = "Template processor adapter for Rocker. Don't support dynamic template loading."

dependencies {
    val rockerVersion = libs.versions.rocker.get()

    "api"(project(":templates:templates"))
    "api"("com.fizzed:rocker-runtime:$rockerVersion")

    "testImplementation"(project(":templates:templates_test"))
    "testImplementation"(project(":serialization:serialization_jackson_json"))
}

rocker {
    configurations {
        create("test") {
            templateDir.set(file("test"))
            optimize.set(true)
        }
    }
}

// TODO Check 'official' plugin
//tasks.named("compileKotlin") {
//    dependsOn("generateRockerTemplateSource")
//}
//
//tasks.named("compileTestKotlin") {
//    dependsOn("generateTestRockerTemplateSource")
//}
//
//rocker {
//    optimize = true
//}
