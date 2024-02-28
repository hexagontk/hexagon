
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
    val slf4jVersion = properties["slf4jVersion"]

    "api"(project(":http:rest"))
    "api"(project(":http:http_server"))
    "api"(project(":http:http_client"))
    "api"("com.atlassian.oai:swagger-request-validator-core:$swaggerRequestValidatorVersion")

    "testImplementation"(project(":http:http_client_jetty"))
    "testImplementation"(project(":http:http_server_jetty"))
    "testImplementation"(project(":serialization:serialization_jackson_json"))
    "testImplementation"(project(":serialization:serialization_jackson_yaml"))
    "testImplementation"("org.slf4j:log4j-over-slf4j:$slf4jVersion")
    "testImplementation"("org.slf4j:jcl-over-slf4j:$slf4jVersion")
    "testImplementation"("org.slf4j:slf4j-jdk14:$slf4jVersion")
}
