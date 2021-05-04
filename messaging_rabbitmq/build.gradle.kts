
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

extra["basePackage"] = "com.hexagonkt.messaging.rabbitmq"

dependencies {
    "api"(project(":hexagon_http"))
    "api"(project(":port_messaging"))
    "api"("com.rabbitmq:amqp-client:${properties["rabbitVersion"]}") {
        exclude(module = "slf4j-api")
    }
    "api"("io.dropwizard.metrics:metrics-jmx:${properties["metricsJmxVersion"]}")

    "testImplementation"("org.apache.qpid:qpid-broker:${properties["qpidVersion"]}") {
        exclude(module = "logback-classic")
        exclude(module = "jackson-databind")
        exclude(module = "jackson-core")
        exclude(module = "slf4j-api")
        exclude(module = "qpid-broker-plugins-derby-store")
        exclude(module = "qpid-broker-plugins-jdbc-provider-bone")
        exclude(module = "qpid-broker-plugins-jdbc-store")
        exclude(module = "qpid-broker-plugins-management-http")
        exclude(module = "qpid-broker-plugins-websocket")
    }

    val logbackVersion = properties["logbackVersion"]
    "testImplementation"(project(":serialization_json"))
    "testImplementation"("ch.qos.logback:logback-classic:$logbackVersion") {
        exclude(group = "org.slf4j")
    }
}
