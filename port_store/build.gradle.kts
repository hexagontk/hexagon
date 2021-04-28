
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

extra["basePackage"] = "com.hexagonkt.store"

dependencies {
    val kotlinVersion = properties["kotlinVersion"]

    "api"(project(":hexagon_core"))
    "api"("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
}
