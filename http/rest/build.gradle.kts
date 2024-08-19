
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

description = "HTTP server extensions to ease the development of REST APIs."

dependencies {
    val slf4jVersion = libs.versions.slf4j.get()

    "api"(project(":http:http_handlers"))
    "api"(project(":serialization:serialization"))

    "testImplementation"(project(":http:http_client_jetty"))
    "testImplementation"(project(":http:http_server_jetty"))
    "testImplementation"(project(":serialization:serialization_jackson_csv"))
    "testImplementation"(project(":serialization:serialization_jackson_json"))
    "testImplementation"(project(":serialization:serialization_jackson_toml"))
    "testImplementation"(project(":serialization:serialization_jackson_xml"))
    "testImplementation"(project(":serialization:serialization_jackson_yaml"))
    "testImplementation"("org.slf4j:log4j-over-slf4j:$slf4jVersion")
    "testImplementation"("org.slf4j:jcl-over-slf4j:$slf4jVersion")
    "testImplementation"("org.slf4j:slf4j-jdk14:$slf4jVersion")
}
