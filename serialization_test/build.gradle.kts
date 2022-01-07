
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/detekt.gradle")

description = "Hexagon serialization testing helpers."

extra["basePackage"] = "com.hexagonkt.serialization.test"

dependencies {
    val kotlinVersion = properties["kotlinVersion"]
    val junitVersion = properties["junitVersion"]

    "api"(project(":serialization"))
    "api"("org.junit.jupiter:junit-jupiter:$junitVersion")
    "api"("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
}
