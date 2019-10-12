
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/bintray.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/testng.gradle")

// Overridden because this test bundle requires the templates

plugins{
    java
}

tasks.named<Jar>("testJar") {
        archiveClassifier.set("test")
        from(sourceSets.test.get().output){
            exclude("*.yaml")
            exclude("**.yml")
            exclude("**.properties")
            exclude("**.xml")
        }
    }
dependencies {
    "api"(project(":hexagon_core"))
    "testImplementation"(project(":port_http_client"))
}
