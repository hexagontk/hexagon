
plugins {
    id("java-library")
}

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/detekt.gradle")

description = "Hexagon YAML serialization format (using Jackson)."

extra["basePackage"] = "com.hexagonkt.serialization.jackson.yaml"

dependencies {
    val jacksonVersion = properties["jacksonVersion"]

    api(project(":serialization_jackson"))
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")

    testImplementation(project(":serialization_test"))
}
