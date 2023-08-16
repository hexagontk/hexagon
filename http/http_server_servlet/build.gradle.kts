
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

description = "HTTP server adapter to be used on JEE Web Applications (deployed inside a server)."

dependencies {
    val servletVersion = properties["servletVersion"]
    val jettyVersion = properties["jettyVersion"]

    "api"(project(":http:http_server"))
    "compileOnly"("jakarta.servlet:jakarta.servlet-api:$servletVersion")

    "testImplementation"(project(":logging:logging_jul"))
    "testImplementation"(project(":http:http_client_jetty"))
    "testImplementation"("org.eclipse.jetty.ee10:jetty-ee10-webapp:$jettyVersion")
}
