
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/bintray.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/junit.gradle")

dependencies {
    val cronutilsVersion = properties["cronutilsVersion"]

    "api"(project(":hexagon_core"))
    "api"("com.cronutils:cron-utils:$cronutilsVersion")
}
