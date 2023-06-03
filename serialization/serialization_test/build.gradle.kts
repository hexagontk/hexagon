
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

description = "Hexagon serialization testing helpers."

dependencies {
    val junitVersion = properties["junitVersion"]

    "api"(project(":serialization:serialization"))
    "api"("org.jetbrains.kotlin:kotlin-test")
    "api"("org.junit.jupiter:junit-jupiter:$junitVersion")

    "testImplementation"(project(":serialization:serialization_jackson_json"))
}
