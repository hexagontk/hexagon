
plugins {
    id("java-library")
}

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/native.gradle")

dependencies {
    val nimaVersion = properties["nimaVersion"]

    "api"(project(":http_server"))
    "api"(project(":logging_jul"))
    "api"("io.helidon.nima.webserver:helidon-nima-webserver:$nimaVersion")

    "testImplementation"(project(":http_test"))
    "testImplementation"(project(":http_client_jetty"))
    "testImplementation"(project(":serialization_jackson_json"))
    "testImplementation"(project(":serialization_jackson_yaml"))
}
