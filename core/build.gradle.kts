
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/resources.gradle")

if (findProperty("fullBuild") != null) {
    apply(from = "$rootDir/gradle/publish.gradle")
    apply(from = "$rootDir/gradle/dokka.gradle")
    apply(from = "$rootDir/gradle/native.gradle")
    apply(from = "$rootDir/gradle/detekt.gradle")
}

description = "Hexagon core utilities (dates, exceptions, logging, etc.)."

dependencies {
    val mockkVersion = libs.versions.mockk.get()

    "api"("org.jetbrains.kotlin:kotlin-stdlib")

    "testImplementation"("org.jetbrains.kotlin:kotlin-reflect")
    "testImplementation"("io.mockk:mockk:$mockkVersion") {
        exclude(group = "org.jetbrains.kotlin")
    }
}
