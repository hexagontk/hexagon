
plugins {
    id("java-library")
}

val gradleScripts = properties["gradleScripts"]

apply(from = "$gradleScripts/kotlin.gradle")
apply(from = "$gradleScripts/publish.gradle")
apply(from = "$gradleScripts/dokka.gradle")
apply(from = "$gradleScripts/detekt.gradle")
apply(from = "$gradleScripts/native.gradle")

description = "Hexagon support for repeated tasks execution based on Cron expressions."

dependencies {
    val cronutilsVersion = libs.versions.cronutils.get()

    "api"("com.hexagonkt:core:$version")
    "api"("com.cronutils:cron-utils:$cronutilsVersion")
}
