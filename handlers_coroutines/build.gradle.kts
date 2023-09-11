
plugins {
    id("java-library")
    id("me.champeau.jmh")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")
apply(from = "$rootDir/gradle/jmh.gradle")

description = "Handlers to be applied on events processing."

dependencies {
    "testImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}
