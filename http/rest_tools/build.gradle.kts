
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
    val guavaVersion = "33.0.0-jre"
    val slf4jVersion = properties["slf4jVersion"]
    val swaggerRequestValidatorVersion = properties["swaggerRequestValidatorVersion"]

    "api"(project(":http:rest"))
    "api"(project(":http:http_server"))
    "api"(project(":http:http_client"))
    "api"("org.slf4j:slf4j-api:$slf4jVersion")
    "api"("com.google.guava:guava:$guavaVersion")
    "api"("com.atlassian.oai:swagger-request-validator-core:$swaggerRequestValidatorVersion") {
        exclude(group = "org.slf4j")
        exclude(group = "com.google.guava")
    }

    "testImplementation"(project(":http:http_client_jetty"))
    "testImplementation"(project(":http:http_server_jetty"))
    "testImplementation"(project(":serialization:serialization_jackson_json"))
    "testImplementation"(project(":serialization:serialization_jackson_yaml"))
}
