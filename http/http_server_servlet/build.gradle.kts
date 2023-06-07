
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

dependencies {
    val servletVersion = properties["servletVersion"]

    "api"(project(":http:http_server"))
    "compileOnly"("jakarta.servlet:jakarta.servlet-api:$servletVersion")

    "testImplementation"(project(":logging:logging_jul"))
    "testImplementation"(project(":http:http_client_jetty"))
    "testImplementation"("org.eclipse.jetty:jetty-webapp")
}
