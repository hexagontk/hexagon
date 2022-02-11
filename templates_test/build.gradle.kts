
plugins {
    id("java-library")
}

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

extra["basePackage"] = "com.hexagonkt.templates"

dependencies {
    val kotlinVersion = properties["kotlinVersion"]
    val junitVersion = properties["junitVersion"]

    api(project(":templates"))
    api("org.junit.jupiter:junit-jupiter:$junitVersion")
    api("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
}
