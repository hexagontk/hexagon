
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

dependencies {
    val jettyVersion = properties["jettyVersion"]
    val slf4jVersion = properties["slf4jVersion"]

    "api"(project(":http_server_servlet"))
    "api"("org.slf4j:slf4j-api:$slf4jVersion")
    "api"("org.eclipse.jetty:jetty-webapp:$jettyVersion") { exclude("org.slf4j") }
    "api"("org.eclipse.jetty.http2:http2-server:$jettyVersion") { exclude("org.slf4j") }
    "api"("org.eclipse.jetty:jetty-alpn-java-server:$jettyVersion") { exclude("org.slf4j") }

    "testImplementation"(project(":http_client_jetty"))
    "testImplementation"("org.eclipse.jetty.websocket:websocket-jetty-server:$jettyVersion")
    "testImplementation"("org.eclipse.jetty.websocket:websocket-jetty-client:$jettyVersion")
}
