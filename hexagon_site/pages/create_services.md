
## Create a service

### From scratch

To build Hexagon services you have some Gradle helpers that you can use on your own project. To
use them, you can use the online versions, or copy them to your `gradle` directory.

You can write a [Gradle] project from scratch (Gradle 4 or newer is required):

`build.gradle`:

```groovy
plugins {
    id 'org.jetbrains.kotlin.jvm' version '1.3.20'
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
