
# Module messaging_rabbitmq

!!! Warning
    This module is a preview and its code is still being reviewed and tested.

Hexagon's event bus implementation based in RabbitMQ.

### Install the Dependency

=== "build.gradle"
  ```groovy
  repositories {
      mavenCentral()
  }

  implementation("com.hexagonkt:messaging_rabbitmq:$hexagonVersion")
  ```

=== "pom.xml"
  ```xml
  <dependency>
    <groupId>com.hexagonkt</groupId>
    <artifactId>messaging_rabbitmq</artifactId>
    <version>$hexagonVersion</version>
  </dependency>
  ```

# Package com.hexagonkt.messaging.rabbitmq

Contains a RabbitMQ Client and a Hexagon messaging implementation backed by it.
