
plugins {
    id("java-library")
}

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/detekt.gradle")
apply(from = "../gradle/native.gradle")

description = "Hexagon TOML serialization format (using Jackson)."

dependencies {
    val jacksonVersion = properties["jacksonVersion"]

    "api"(project(":serialization_jackson"))
    "api"(platform("com.fasterxml.jackson:jackson-bom:$jacksonVersion"))
    "api"("com.fasterxml.jackson.dataformat:jackson-dataformat-toml")

    "testImplementation"(project(":serialization_test"))
}
