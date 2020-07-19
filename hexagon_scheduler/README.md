
# Module hexagon_scheduler

Provides repeated tasks execution based on [Cron] expressions for Hexagon framework. It uses the
[Cron-utils Java Library].

!!! Note
    In some platforms I.e.: Kubernetes there is a way to execute repeated tasks, you should take
    advantage of them as using your own service will raise problems scaling those services'
    instances (you will have to coordinate them)

### Install Dependency

```groovy tab="build.gradle"
repositories {
    mavenCentral()
}

implementation("com.hexagonkt:hexagon_scheduler:$hexagonVersion")
```

```xml tab="pom.xml"
<dependency>
  <groupId>com.hexagonkt</groupId>
  <artifactId>hexagon_scheduler</artifactId>
  <version>$hexagonVersion</version>
</dependency>
```

### Example

You can check a usage example in the following code:

@sample hexagon_scheduler/src/test/kotlin/CronSchedulerSamplesTest.kt:sample

# Package com.hexagonkt.scheduler

Classes for scheduling blocks of code repeatedly based on a [Cron] expression.

[Cron]: https://en.wikipedia.org/wiki/Cron
[Cron-utils Java Library]: http://cron-parser.com
