
# ${projectName} Service

This is an Hexagon service created from a template.

## Usage

* Build: `./gradlew build`
* Rebuild: `./gradlew clean build`
* Assemble: `./gradlew installDist`
* Run: `./gradlew run`
* Watch: `./gradlew --no-daemon --continuous runService`
* Test: `./gradlew test`

## Docker

Prior to generate the Docker image, you need to create the service distribution: `./gradlew installDist`
After that you can start the whole service stack executing: `docker-compose up -d`
