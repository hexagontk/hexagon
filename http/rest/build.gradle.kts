
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

description = "HTTP server extensions to ease the development of REST APIs."

dependencies {
    val slf4jVersion = properties["slf4jVersion"]

    "api"(project(":http:http_handlers"))
    "api"(project(":serialization:serialization"))

    "testImplementation"(project(":http:http_client_jetty"))
    "testImplementation"(project(":http:http_server_jetty"))
    "testImplementation"(project(":serialization:serialization_jackson_json"))
    "testImplementation"("org.slf4j:slf4j-jdk14:$slf4jVersion")
}
