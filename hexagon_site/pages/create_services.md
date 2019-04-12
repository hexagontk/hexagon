
Create a service
================

## From scratch

To build Hexagon services you have some Gradle helpers that you can use on your own project. To
use them, you can use the online versions, or copy them to your `gradle` directory.

You can write a [Gradle] project from scratch (Gradle 5 or newer is required) using the following
`build.gradle`:

```groovy
plugins {
    id 'org.jetbrains.kotlin.jvm' version '1.3.30'
}

apply plugin: 'kotlin'
apply plugin: 'application'

mainClassName = 'HelloKt'

repositories {
    jcenter ()
}

dependencies {
    compile ("com.hexagonkt:http_server_jetty:$hexagonVersion")
}
```

## From a template

You can create a service from a [Lazybones] template. To do so type:
`lazybones create hexagon-service service`

```bash
curl -s get.sdkman.io | bash && source ~/.sdkman/bin/sdkman-init.sh
sdk i lazybones
mkdir ~/.lazybones
lazybones config set bintrayRepositories pledbrook/lazybones-templates jamming/maven
lazybones create hexagon-service service -Pgroup=org.example -Pversion=0.1 -Pdescription=Description
cd service
gradle/wrapper
```
