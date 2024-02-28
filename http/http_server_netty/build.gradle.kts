
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

description = "HTTP server adapter for Netty."

dependencies {
    val nettyVersion = properties["nettyVersion"]
    val nettyTcNativeVersion = properties["nettyTcNativeVersion"]
    val slf4jVersion = properties["slf4jVersion"]

    "api"(project(":http:http_server"))
    "api"("io.netty:netty-codec-http2:$nettyVersion") { exclude(group = "org.slf4j") }

    if (System.getProperty("os.name").lowercase().contains("mac"))
        "api"("io.netty:netty-tcnative-boringssl-static:$nettyTcNativeVersion:osx-x86_64")

    "testImplementation"(project(":http:http_test"))
    "testImplementation"(project(":http:http_client_jetty_ws"))
    "testImplementation"(project(":serialization:serialization_jackson_json"))
    "testImplementation"(project(":serialization:serialization_jackson_yaml"))
    "testImplementation"("org.slf4j:slf4j-jdk14:$slf4jVersion")
}
