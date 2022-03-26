
plugins {
    id("java-library")
}

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/detekt.gradle")

description = "Hexagon serialization testing helpers."

dependencies {
    val junitVersion = properties["junitVersion"]

    api(project(":serialization"))
    api("org.jetbrains.kotlin:kotlin-test")
    api("org.junit.jupiter:junit-jupiter:$junitVersion")
}
