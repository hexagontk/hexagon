
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

description = "Hexagon support for repeated tasks execution based on Cron expressions."

dependencies {
    val cronutilsVersion = properties["cronutilsVersion"]

    "api"(project(":hexagon_core"))
    "api"("com.cronutils:cron-utils:$cronutilsVersion") {
        // TODO Remove when the scope is changed in cron-utils
        exclude(group = "org.projectlombok")
    }
}
