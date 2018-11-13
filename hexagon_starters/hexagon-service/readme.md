
# ${projectName} Service

This is an Hexagon service created from a template.

## Usage

Prior to run the tests you need to start required services (declared in `docker-compose.yaml`).
You can do so executing: `docker-compose up -d`

* Build: `./gradlew build`
* Rebuild: `./gradlew clean build`
* Assemble: `./gradlew installDist`
* Run: `./gradlew run`
* Watch: `./gradlew --no-daemon --continuous runService`
* Test: `./gradlew test`

## Optional features

* WAR package: if you deploy the service only as a Java process you should:
  - Remove the `war` plugin from `build.gradle`.
  - Change the `jetty-webapp` dependency to `compile` (required).
  - Delete the `Web` class.

* Stand alone server: if you are going to use the service inside a Java Servlet server. You can:
  - Remove the `main` method.
  - Delete the `mainClassName` and `applicationDefaultJvmArgs ` properties from `build.gradle`.

* HTML templates: if you don't use templates you can remove the `pebble` dependency from
  `build.gradle`.

## Gradle wrapper setup

You can change Gradle version in `gradle/wrapper/gradle-wrapper.properties`.
