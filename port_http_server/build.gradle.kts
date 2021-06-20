
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

// IMPORTANT: Required for compiling classes in test dependencies. It *MUST* be before dependencies
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.dependsOn(tasks.getByPath(":hexagon_core:compileTestKotlin"))
val coreTest: SourceSetOutput = project(":hexagon_core").sourceSet("test").output

// Overridden because this test bundle requires the templates
tasks.named<Jar>("testJar") {
    archiveClassifier.set("test")
    from(project.sourceSet("test").output){
        exclude("**.yml")
        exclude("**.properties")
        exclude("**.xml")
    }
}

extra["basePackage"] = "com.hexagonkt.http.server"

dependencies {
    val swaggerParserVersion = properties["swaggerParserVersion"]

    "api"(project(":hexagon_http"))
    "testImplementation"(project(":http_client_ahc"))
    "testImplementation"(project(":http_server_jetty"))

    // For the Mock OpenAPI Server
    "testImplementation"("io.swagger.parser.v3:swagger-parser:$swaggerParserVersion")
    "testImplementation"(project(":hexagon_settings"))
    "testImplementation"(project(":serialization_json"))
    "testImplementation"(coreTest)
}

extensions.configure<PublishingExtension> {
    (publications["mavenJava"] as MavenPublication).artifact(tasks.named("testJar"))
}

setUpDokka(tasks.getByName<DokkaTaskPartial>("dokkaHtmlPartial"))
setUpDokka(tasks.getByName<DokkaTask>("dokkaJavadoc"))

fun setUpDokka(dokkaTask: DokkaTaskPartial) {
    dokkaTask.dokkaSourceSets {
        configureEach {
            sourceRoots.from(file("src/test/kotlin"))
        }
    }
}

fun setUpDokka(dokkaTask: DokkaTask) {
    dokkaTask.dokkaSourceSets {
        configureEach {
            sourceRoots.from(file("src/test/kotlin"))
        }
    }
}
