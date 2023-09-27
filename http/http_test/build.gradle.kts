
plugins {
    id("java-library")
    id("me.champeau.jmh")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

description = "Test cases for HTTP client and server adapters."

dependencies {
    val junitVersion = properties["junitVersion"]
    val gatlingVersion = properties["gatlingVersion"]

    "api"(project(":serialization:serialization"))
    "api"(project(":http:http_client"))
    "api"(project(":http:http_server"))
    "api"("org.jetbrains.kotlin:kotlin-test")
    "api"("org.junit.jupiter:junit-jupiter:$junitVersion")
    "api"("io.gatling.highcharts:gatling-charts-highcharts:$gatlingVersion")
}
