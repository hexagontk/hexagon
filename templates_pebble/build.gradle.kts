
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/bintray.gradle")
apply(from = "../gradle/dokka.gradle")

dependencies {
    "api"(project(":port_templates"))
    "api"("io.pebbletemplates:pebble:${properties["pebbleVersion"]}") {
        exclude (module = "slf4j-api")
    }
}
