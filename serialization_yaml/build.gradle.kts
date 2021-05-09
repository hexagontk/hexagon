
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/detekt.gradle")

description = "Hexagon YAML serialization format."

extra["basePackage"] = "com.hexagonkt.serialization"

dependencies {
    val jacksonVersion = properties["jacksonVersion"]

    "api"(project(":serialization_json"))

    "api"("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
}
