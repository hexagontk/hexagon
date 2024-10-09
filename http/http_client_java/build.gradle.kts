
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")

if (findProperty("fullBuild") != null) {
    apply(from = "$rootDir/gradle/publish.gradle")
    apply(from = "$rootDir/gradle/dokka.gradle")
    apply(from = "$rootDir/gradle/native.gradle")
    apply(from = "$rootDir/gradle/detekt.gradle")
}

description = "HTTP client adapter for the Java HTTP client."

dependencies {
    "api"(project(":http:http_client"))

    "testImplementation"(project(":http:http_test"))
    "testImplementation"(project(":http:http_server_jetty"))
    "testImplementation"(project(":serialization:serialization_jackson_json"))
    "testImplementation"(project(":serialization:serialization_jackson_yaml"))
}
