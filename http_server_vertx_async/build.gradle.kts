
plugins {
    id("java-library")
}

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/native.gradle")

dependencies {
    val vertxVersion = properties["vertxVersion"]

    "api"(project(":http_server_async"))
    "api"("io.vertx:vertx-web:$vertxVersion")

    "testImplementation"(project(":http_test_async"))
    "testImplementation"(project(":http_client_jetty"))
    "testImplementation"(project(":serialization_jackson_json"))
    "testImplementation"(project(":serialization_jackson_yaml"))
}
