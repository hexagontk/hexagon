
plugins {
    id("java-library")
    id("me.champeau.jmh")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

description = "Test cases for HTTP client and server adapters."

dependencies {
    val junitVersion = properties["junitVersion"]
    val gatlingVersion = properties["gatlingVersion"]

    "api"(project(":logging:logging_slf4j_jul"))
    "api"(project(":serialization:serialization"))
    "api"(project(":http:http_client"))
    "api"(project(":http:http_server_async"))
    "api"("org.jetbrains.kotlin:kotlin-test")
    "api"("org.junit.jupiter:junit-jupiter:$junitVersion")
    "api"("io.gatling.highcharts:gatling-charts-highcharts:$gatlingVersion")

    "testImplementation"(project(":http:http_client_jetty"))
    "testImplementation"(project(":http:http_server_netty_async"))
    "testImplementation"(project(":serialization:serialization_jackson_json"))
    "testImplementation"(project(":serialization:serialization_jackson_yaml"))
}
