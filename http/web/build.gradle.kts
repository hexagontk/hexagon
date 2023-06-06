
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

dependencies {
    "api"(project(":http:http_server"))
    "api"(project(":templates:templates"))

    "testImplementation"(project(":logging:logging_jul"))
    "testImplementation"(project(":http:http_client_jetty"))
    "testImplementation"(project(":http:http_server_jetty"))
    "testImplementation"(project(":templates:templates_pebble"))
    "testImplementation"(project(":serialization:serialization_jackson_json"))
}
