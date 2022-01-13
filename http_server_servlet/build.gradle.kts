
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

extra["basePackage"] = "com.hexagonkt.http.server.servlet"

dependencies {
    val jettyVersion = properties["jettyVersion"]
    val servletVersion = properties["servletVersion"]

    "api"(project(":http_server"))
    "compileOnly"("jakarta.servlet:jakarta.servlet-api:$servletVersion")

    "testImplementation"(project(":http_client_jetty"))
    "testImplementation"("org.eclipse.jetty:jetty-webapp:$jettyVersion")
}
