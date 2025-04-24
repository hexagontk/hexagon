
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/lean.gradle")

apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")

group = "com.hexagontk.http"
description = "HTTP server adapter for Netty."

dependencies {
    val jettyVersion = libs.versions.jetty.get()
    val nettyVersion = libs.versions.netty.get()
    val nettyTcNativeVersion = libs.versions.nettyTcNative.get()
    val slf4jVersion = libs.versions.slf4j.get()

    "api"(project(":http:http_server"))
    "api"("io.netty:netty-codec-http2:$nettyVersion") { exclude(group = "org.slf4j") }
    "api"("io.netty:netty-tcnative-boringssl-static:$nettyTcNativeVersion")
    "compileOnlyApi"("io.netty:netty-transport-native-epoll:$nettyVersion")
    "compileOnlyApi"("io.netty:netty-transport-native-io_uring:$nettyVersion")

    "testImplementation"(project(":http:http_test"))
    "testImplementation"(project(":http:http_client_jetty"))
    "testImplementation"(project(":serialization:serialization_jackson_json"))
    "testImplementation"(project(":serialization:serialization_jackson_yaml"))
    "testImplementation"("org.slf4j:slf4j-jdk14:$slf4jVersion")
    "testImplementation"("org.eclipse.jetty.websocket:jetty-websocket-jetty-client:$jettyVersion")
    "testImplementation"("io.netty:netty-transport-native-epoll:$nettyVersion")
    "testImplementation"("io.netty:netty-transport-native-epoll:$nettyVersion:linux-x86_64")
    "testImplementation"("io.netty:netty-transport-native-io_uring:$nettyVersion")
    "testImplementation"("io.netty:netty-transport-native-io_uring:$nettyVersion:linux-x86_64")
}
