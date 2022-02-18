
plugins {
    id("java-library")
}

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

dependencies {
    val nettyVersion = properties["nettyVersion"]

    api(project(":http_server"))
    api("io.netty:netty-codec-http:$nettyVersion")
    api("io.netty:netty-codec-http2:$nettyVersion")
    // TODO Add support to native transports (maybe in different modules each)
//    api("io.netty:netty-transport-native-epoll:$nettyVersion")
//    api("io.netty:netty-transport-native-kqueue:$nettyVersion")

    testImplementation(project(":http_test"))
    testImplementation(project(":http_client_jetty"))
    testImplementation(project(":serialization_jackson_json"))
    testImplementation(project(":serialization_jackson_yaml"))
}
