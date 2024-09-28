
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

description = "Hexagon helpers. Not used inside the toolkit but useful for applications."

dependencies {
    val mockkVersion = libs.versions.mockk.get()

    "api"(project(":core"))

    "testImplementation"("org.jetbrains.kotlin:kotlin-reflect")
    "testImplementation"("io.mockk:mockk:$mockkVersion") {
        exclude(group = "org.jetbrains.kotlin")
    }
}
