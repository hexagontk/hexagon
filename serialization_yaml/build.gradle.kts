
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/detekt.gradle")

description = "Hexagon CSV serialization format."

dependencies {
    val jacksonVersion = properties["jacksonVersion"]

    "api"(project(":hexagon_core"))

    "api"("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
}
