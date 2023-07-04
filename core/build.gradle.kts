
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

description = "Hexagon core utilities. Includes logging helpers."

dependencies {
    val scriptMockkVersion = findProperty("mockkVersion")

    "api"("org.jetbrains.kotlin:kotlin-stdlib")

    "testImplementation"("org.jetbrains.kotlin:kotlin-reflect")
    "testImplementation"("io.mockk:mockk:$scriptMockkVersion") {
        exclude(group = "org.jetbrains.kotlin")
    }
}
