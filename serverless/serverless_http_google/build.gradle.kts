
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

description = "Google Functions Serverless adapter."

private val target = "com.hexagontk.serverless.http.google.GoogleServerlessHttpAdapter"
private val invoker by configurations.creating

dependencies {
    val functionsVersion = libs.versions.functions.get()
    val invokerVersion = libs.versions.invoker.get()

    "api"(project(":http:http_handlers"))
    "compileOnly"("com.google.cloud.functions:functions-framework-api:$functionsVersion")

    "testImplementation"("com.google.cloud.functions:functions-framework-api:$functionsVersion")
    "testImplementation"("com.google.cloud.functions.invoker:java-function-invoker:$invokerVersion")
    "testImplementation"(project(":http:http_client_java"))

    "invoker"("com.google.cloud.functions.invoker:java-function-invoker:$invokerVersion")
}

tasks.register<JavaExec>("runFunction") {
    val classpath = files(configurations.runtimeClasspath, sourceSets["main"].output)

    classpath(invoker)
    mainClass = "com.google.cloud.functions.invoker.runner.Invoker"
    inputs.files(classpath)

    args(
        "--target", properties["run.target"] ?: target,
        "--port", properties["run.port"] ?: 8080
    )

    doFirst {
        args("--classpath", classpath.asPath)
    }
}
