
plugins {
    id("java-library")
}

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/native.gradle")

dependencies {
    "api"(project(":http_server_netty"))
    "api"("io.netty:netty-transport-native-epoll")

    "testImplementation"(project(":http_test"))
    "testImplementation"(project(":http_client_jetty"))
    "testImplementation"(project(":serialization_jackson_json"))
    "testImplementation"(project(":serialization_jackson_yaml"))
}
