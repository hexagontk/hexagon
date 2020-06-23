
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

plugins {
    java
}

// Overridden because this test bundle requires the templates
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
    "testImplementation"(project(":http_client_ahc"))
}
