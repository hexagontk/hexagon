
plugins {
    id("java-library")
}

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

dependencies {
    val nettyVersion = properties["nettyVersion"]

    api(project(":http_server"))
    api(platform("io.netty:netty-bom:$nettyVersion"))
    api("io.netty:netty-codec-http")
    api("io.netty:netty-codec-http2")

    testImplementation(project(":http_test"))
    testImplementation(project(":http_client_jetty"))
    testImplementation(project(":serialization_jackson_json"))
    testImplementation(project(":serialization_jackson_yaml"))
}
