
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

dependencies {
    "api"(project(":port_messaging"))
    "api"("com.rabbitmq:amqp-client:${properties["rabbitVersion"]}") {
        exclude(module = "slf4j-api")
    }
    "api"("io.dropwizard.metrics:metrics-jmx:${properties["metricsVersion"]}")

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
}
