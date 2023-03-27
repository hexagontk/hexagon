
plugins {
    id("java-library")
    id("me.champeau.jmh")
}

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/jmh.gradle")

description = "Test cases for HTTP client and server adapters."

dependencies {
    val junitVersion = properties["junitVersion"]
    val gatlingVersion = properties["gatlingVersion"]

    "api"(project(":logging_slf4j_jul"))
    "api"(project(":serialization"))
    "api"(project(":http_client"))
    "api"(project(":http_server"))
    "api"("org.jetbrains.kotlin:kotlin-test")
    "api"("org.junit.jupiter:junit-jupiter:$junitVersion")
    "api"("io.gatling.highcharts:gatling-charts-highcharts:$gatlingVersion")

    "testImplementation"(project(":http_client_jetty"))
    "testImplementation"(project(":http_server_jetty"))
    "testImplementation"(project(":http_server_netty"))
    "testImplementation"(project(":serialization_jackson_json"))
    "testImplementation"(project(":serialization_jackson_yaml"))
}
