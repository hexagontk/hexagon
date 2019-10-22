
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/bintray.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/testng.gradle")

dependencies {
    "api"(project(":port_messaging"))
    "api"("com.rabbitmq:amqp-client:${properties.get("rabbitVersion")}") { exclude(module = "slf4j-api") }

    "testImplementation"("org.apache.qpid:qpid-broker:${properties.get("qpidVersion")}") {
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
