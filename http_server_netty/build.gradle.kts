
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

dependencies {
    val nettyVersion = properties["nettyVersion"]
    val nettyTcNativeVersion = properties["nettyTcNativeVersion"]

    "api"(project(":http_server"))
    "api"(platform("io.netty:netty-bom:$nettyVersion"))
    "api"("io.netty:netty-codec-http") { exclude(group = "org.slf4j") }
    "api"("io.netty:netty-codec-http2") { exclude(group = "org.slf4j") }

    if (System.getProperty("os.name").lowercase().contains("mac"))
        "api"("io.netty:netty-tcnative:$nettyTcNativeVersion:osx-x86_64") {
            exclude(group = "org.slf4j")
        }

    "testImplementation"(project(":http_test"))
    "testImplementation"(project(":http_client_jetty_ws"))
    "testImplementation"(project(":serialization_jackson_json"))
    "testImplementation"(project(":serialization_jackson_yaml"))
}
