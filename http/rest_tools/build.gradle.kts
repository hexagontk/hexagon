
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

description = "Tools to test and document REST services."

dependencies {
    val swaggerRequestValidatorVersion = properties["swaggerRequestValidatorVersion"]

    "api"(project(":http:rest"))
    "api"(project(":http:http_server"))
    "api"(project(":http:http_client"))
    "api"("com.atlassian.oai:swagger-request-validator-core:$swaggerRequestValidatorVersion")

    "testImplementation"(project(":http:http_client_jetty"))
    "testImplementation"(project(":http:http_server_jetty"))
    "testImplementation"(project(":serialization:serialization_jackson_json"))
    "testImplementation"(project(":serialization:serialization_jackson_yaml"))
}
