
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/lean.gradle")

if (findProperty("fullBuild") != null) {
    apply(from = "$rootDir/gradle/publish.gradle")
    apply(from = "$rootDir/gradle/dokka.gradle")
    apply(from = "$rootDir/gradle/native.gradle")
}

group = "com.hexagontk.http"
description = "HTTP server adapter for Netty."

dependencies {
    val nettyVersion = libs.versions.netty.get()
    val nettyTcNativeVersion = libs.versions.nettyTcNative.get()
    val slf4jVersion = libs.versions.slf4j.get()

    "api"(project(":http:http_server"))
    "api"("io.netty:netty-codec-http2:$nettyVersion") { exclude(group = "org.slf4j") }
    "api"("io.netty:netty-tcnative-boringssl-static:$nettyTcNativeVersion")

    "testImplementation"(project(":http:http_test"))
    "testImplementation"(project(":http:http_client_jetty_ws"))
    "testImplementation"(project(":serialization:serialization_jackson_json"))
    "testImplementation"(project(":serialization:serialization_jackson_yaml"))
    "testImplementation"("org.slf4j:slf4j-jdk14:$slf4jVersion")
}
