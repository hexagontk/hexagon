
plugins {
    id("java-library")
}

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

dependencies {
    val servletVersion = properties["servletVersion"]

    "api"(project(":http_server"))
    "compileOnly"("jakarta.servlet:jakarta.servlet-api:$servletVersion")

    "testImplementation"(project(":logging_jul"))
    "testImplementation"(project(":http_client_jetty"))
    "testImplementation"("org.eclipse.jetty:jetty-webapp")
}
