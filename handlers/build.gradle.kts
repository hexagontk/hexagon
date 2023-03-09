
plugins {
    id("java-library")
    id("me.champeau.jmh")
}

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/jmh.gradle")

description = "Handlers to be applied on events processing."
