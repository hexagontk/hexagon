
plugins {
    id("java-library")
}

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

extra["basePackage"] = "com.hexagonkt.templates.freemarker"

dependencies {
    api(project(":templates"))
    api("org.freemarker:freemarker:${properties["freemarkerVersion"]}")

    testImplementation(project(":templates_test"))
    testImplementation(project(":serialization_jackson_json"))
}
