
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/bintray.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/junit.gradle")

dependencies {
    "api"(project(":port_http_server"))
    "api"(project(":port_templates"))

    "api"("org.jetbrains.kotlinx:kotlinx-html-jvm:${properties["kotlinxHtmlVersion"]}")

    "testImplementation"(project(":http_client_ahc"))
    "testImplementation"(project(":http_server_jetty"))
    "testImplementation"(project(":templates_pebble"))
}
