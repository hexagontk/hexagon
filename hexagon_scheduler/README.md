
# Module hexagon_scheduler

Provides repeated tasks execution based on [Cron] expressions for Hexagon framework. It uses the
[Cron-utils Java Library].

!!! Note
    In some platforms (i.e.: Kubernetes) there is a way to execute repeated tasks, you could take
    advantage of them, as using your own service will raise problems scaling those services'
    instances (you will have to coordinate them).

This feature does not include any sort of synchronization if you have many instances of a scheduler
service. If you want your scheduled jobs to be executed just once, you have to take care of
synchronization yourself.

### Install Dependency

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    implementation("com.hexagonkt:hexagon_scheduler:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>hexagon_scheduler</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

### Example

You can check a usage example in the following code:

@code hexagon_scheduler/src/test/kotlin/CronSchedulerSamplesTest.kt:sample

# Package com.hexagonkt.scheduler

Classes for scheduling blocks of code repeatedly based on a [Cron] expression.

[Cron]: https://en.wikipedia.org/wiki/Cron
[Cron-utils Java Library]: http://cron-parser.com
