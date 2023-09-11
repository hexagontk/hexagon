
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

description = "HTTP server adapter for Netty (using Linux Epoll)."

dependencies {
    val nettyVersion = properties["nettyVersion"]

    "api"(project(":http:http_server_netty_coroutines"))
    "api"("io.netty:netty-transport-native-epoll:$nettyVersion")

    "testImplementation"(project(":http:http_test_coroutines"))
    "testImplementation"(project(":http:http_client_jetty"))
    "testImplementation"(project(":serialization:serialization_jackson_json"))
    "testImplementation"(project(":serialization:serialization_jackson_yaml"))
}
