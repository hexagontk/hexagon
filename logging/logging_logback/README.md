
# Module logging_logback
Contains the logger adapter for the [Logback] logging library.

[Logback]: http://logback.qos.ch

### Install the Dependency

=== "build.gradle"

    ```groovy
    implementation("com.hexagonkt:logging_logback:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <!--
     ! Pick ONLY ONE of the options below
     !-->
    <!-- Full featured implementation -->
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>logging_logback</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

> ℹ️ **Info**
>
> <sup>The above adapter bridge other logging libraries that may be used by other third party
> libraries you use (if you want to disable this behaviour, you need to explicitly exclude bridging
> libraries).</sup>

=== "build.gradle"

    ```groovy
    // Bridges
    runtimeOnly("org.slf4j:jcl-over-slf4j:1.7.30")
    runtimeOnly("org.slf4j:log4j-over-slf4j:1.7.30")
    runtimeOnly("org.slf4j:jul-to-slf4j:1.7.30")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>jcl-over-slf4j</artifactId>
      <version>1.7.30</version>
    </dependency>
    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>log4j-over-slf4j</artifactId>
      <version>1.7.30</version>
    </dependency>
    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>jul-to-slf4j</artifactId>
      <version>1.7.30</version>
    </dependency>
    ```

# Package com.hexagonkt.logging.logback
Provides a logging management capabilities abstracting the application from logging libraries.
