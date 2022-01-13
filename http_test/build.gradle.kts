
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/detekt.gradle")

description = "Test cases for HTTP client and server adapters."

extra["basePackage"] = "com.hexagonkt.http.test"

dependencies {
    val kotlinVersion = properties["kotlinVersion"]
    val junitVersion = properties["junitVersion"]
    val swaggerParserVersion = properties["swaggerParserVersion"]

    "api"(project(":logging_slf4j_jul"))
    "api"(project(":serialization"))
    "api"(project(":http_client"))
    "api"(project(":http_server"))
    "api"("io.swagger.parser.v3:swagger-parser:$swaggerParserVersion")
    "api"("org.junit.jupiter:junit-jupiter:$junitVersion")
    "api"("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")

    "testImplementation"(project(":http_client_jetty"))
    "testImplementation"(project(":http_server_jetty"))
    "testImplementation"(project(":serialization_jackson_json"))
    "testImplementation"(project(":serialization_jackson_yaml"))
}
