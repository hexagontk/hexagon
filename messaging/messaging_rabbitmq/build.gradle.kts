
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/lean.gradle")

apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")

group = "com.hexagontk.messaging"
description = "."

dependencies {
    val amqpClientVersion = libs.versions.amqpClient.get()
    val metricsJmxVersion = libs.versions.metricsJmx.get()
    val testcontainersVersion = libs.versions.testcontainers.get()
    val commonsCompressVersion = libs.versions.commonsCompress.get()

    "api"(project(":helpers"))
    "api"(project(":messaging:messaging"))
    "api"(project(":serialization:serialization"))
    "api"(project(":http:http"))
    "api"("com.rabbitmq:amqp-client:$amqpClientVersion")
    "api"("io.dropwizard.metrics:metrics-jmx:$metricsJmxVersion")

    "testImplementation"(project(":serialization:serialization_jackson_json"))
    "testImplementation"("org.apache.commons:commons-compress:$commonsCompressVersion")
    "testImplementation"("org.testcontainers:testcontainers-rabbitmq:$testcontainersVersion") {
        exclude(module = "commons-compress")
    }
}
