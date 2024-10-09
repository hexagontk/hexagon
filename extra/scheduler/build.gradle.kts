
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")

if (findProperty("fullBuild") != null) {
    apply(from = "$rootDir/gradle/publish.gradle")
    apply(from = "$rootDir/gradle/dokka.gradle")
    apply(from = "$rootDir/gradle/detekt.gradle")
    apply(from = "$rootDir/gradle/native.gradle")
}

description = "Hexagon support for repeated tasks execution based on Cron expressions."

dependencies {
    val cronutilsVersion = libs.versions.cronutils.get()

    "api"(project(":core"))
    "api"("com.cronutils:cron-utils:$cronutilsVersion")
}
