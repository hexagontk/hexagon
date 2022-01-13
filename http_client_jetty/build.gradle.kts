
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

extra["basePackage"] = "com.hexagonkt.http.client.jetty"

dependencies {
    val jettyVersion = properties["jettyVersion"]

    "api"(project(":http_client"))
    "api"("org.eclipse.jetty:jetty-client:$jettyVersion")
    "api"("org.eclipse.jetty.websocket:websocket-jetty-client:$jettyVersion")
}
