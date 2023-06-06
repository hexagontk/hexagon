
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

description = "Hexagon YAML serialization format (using Jackson)."

dependencies {
    val jacksonVersion = properties["jacksonVersion"]

    "api"(project(":serialization:serialization_jackson"))
    "api"("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")

    "testImplementation"(project(":serialization:serialization_test"))
}
