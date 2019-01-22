
# What is Hexagon

Hexagon is a microservices library written in [Kotlin] which purpose is to ease the building of
services (Web applications, APIs or queue consumers) that run inside cloud platforms.

The project is developed as a [library][frameworks] that you call as opposed to [frameworks] that 
call your code inside them. Being a library means that you won't need special build settings or
tools.

It is meant to provide abstraction from underlying technologies (data storage, HTTP server 
engines, etc.) to be able to change them with minimum impact.

It only supports [Kotlin], Java is not a targeted language for the framework.

[Kotlin]: http://kotlinlang.org
[frameworks]: https://www.quora.com/Whats-the-difference-between-a-library-and-a-framework

# Middleware definition

TODO Mounting routers you can accomplish this

## Create a service

### From scratch

To build Hexagon services you have some Gradle helpers that you can use on your own project. To
use them, you can use the online versions, or copy them to your `gradle` directory.

You can write a [Gradle] project from scratch (Gradle 4 or newer is required):

`build.gradle`:

```groovy
plugins {
    id 'org.jetbrains.kotlin.jvm' version '1.3.11'
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

Now you can run the service with `gradle run` and view the results at:
[http://localhost:2010/hello/world](http://localhost:2010/hello/world)

### From a template

You can create a service from a [Lazybones] template. To do so type:
`lazybones create hexagon-service service`

```bash
curl -s get.sdkman.io | bash && source ~/.sdkman/bin/sdkman-init.sh
sdk i lazybones
mkdir ~/.lazybones # Bug with Lazybones?
lazybones config set bintrayRepositories pledbrook/lazybones-templates jamming/maven
lazybones create hexagon-service service -Pgroup=org.example -Pversion=0.1 -Pdescription=Description
cd service
gradle/wrapper
```

## Directory structure

The project uses the standard Gradle structure. Gradle wrapper changed to be stored in the `gradle`
folder instead of the project root

## Running and Testing

gw run
gw -x test -t runService # Continuous mode AKA watch

## Configuration files

* service.yaml (see configuration.md)
* logback.xml

