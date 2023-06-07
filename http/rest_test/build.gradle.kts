
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

description = "Test cases for HTTP client and server adapters."

dependencies {
    val swaggerParserVersion = properties["swaggerParserVersion"]
    val vertxVersion = properties["vertxVersion"]

    "api"(project(":http:rest"))
    "api"(project(":http:http_server"))
    "api"(project(":http:http_client"))
    "api"("io.swagger.parser.v3:swagger-parser:$swaggerParserVersion")
    "api"("io.vertx:vertx-json-schema:$vertxVersion")

    "testImplementation"(project(":http:http_client_jetty"))
    "testImplementation"(project(":http:http_server_jetty"))
    "testImplementation"(project(":http:http_server_netty"))
    "testImplementation"(project(":serialization:serialization_jackson_json"))
    "testImplementation"(project(":serialization:serialization_jackson_yaml"))
}
