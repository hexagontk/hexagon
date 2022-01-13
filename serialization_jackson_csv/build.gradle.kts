
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/detekt.gradle")

description = "Hexagon CSV serialization format (using Jackson)."

extra["basePackage"] = "com.hexagonkt.serialization.jackson.csv"

dependencies {
    val jacksonVersion = properties["jacksonVersion"]

    "api"(project(":serialization_jackson"))

    "api"("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:$jacksonVersion")
}
