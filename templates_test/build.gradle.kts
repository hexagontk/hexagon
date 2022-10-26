
plugins {
    id("java-library")
}

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/detekt.gradle")

dependencies {
    val junitVersion = properties["junitVersion"]

    "api"(project(":templates"))
    "api"("org.jetbrains.kotlin:kotlin-test")
    "api"("org.junit.jupiter:junit-jupiter:$junitVersion")
}
