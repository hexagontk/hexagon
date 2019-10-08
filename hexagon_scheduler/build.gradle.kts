
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/bintray.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/testng.gradle")

dependencies {
    "api"(project(":hexagon_core"))
    "api"("com.cronutils:cron-utils:${properties.get("cronutilsVersion")}") { exclude(module = "slf4j-api") }
}
