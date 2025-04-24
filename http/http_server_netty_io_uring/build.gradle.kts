
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/lean.gradle")

apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
//apply(from = "$rootDir/gradle/native.gradle")

group = "com.hexagontk.http"
description = "HTTP server adapter for Netty (using Linux io_uring)."

dependencies {
    val jettyVersion = libs.versions.jetty.get()
    val nettyVersion = libs.versions.netty.get()

    "api"(project(":http:http_server_netty"))
    "api"("io.netty:netty-transport-native-io_uring:$nettyVersion")

    "testImplementation"(project(":http:http_test"))
    "testImplementation"(project(":http:http_client_jetty"))
    "testImplementation"(project(":serialization:serialization_jackson_json"))
    "testImplementation"(project(":serialization:serialization_jackson_yaml"))
    "testImplementation"("io.netty:netty-transport-native-io_uring:$nettyVersion:linux-x86_64")
    "testImplementation"("org.eclipse.jetty.websocket:jetty-websocket-jetty-client:$jettyVersion")
}
