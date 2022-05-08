
plugins {
    id("java-library")
}

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/detekt.gradle")

description = "Hexagon CSV serialization format (using Jackson)."

dependencies {
    val jacksonVersion = properties["jacksonVersion"]

    api(project(":serialization_jackson"))
    api(platform("com.fasterxml.jackson:jackson-bom:$jacksonVersion"))
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-csv")
}
