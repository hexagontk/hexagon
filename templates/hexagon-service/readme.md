
# ${projectName} Service

This is an Hexagon service created from a template.

## Usage

* Build: `gradle/wrapper install`
* Rebuild: `gradle/wrapper clean install`
* Run: `gradle/wrapper run`
* Watch: `gradle/wrapper --no-daemon -t runService`
* Test: `gradle/wrapper check`

## Optional features

* WAR package: if you deploy the service only as a Java process you should:
  - Remove the `war` plugin from `build.gradle`.
  - Change the `jetty-webapp` dependency to `compile` (required).
  - Delete the `Web` class.

* Stand alone server: if you are going to use the service inside a Java Servlet server. You can:
  - Remove the `main` method.
  - Delete the `mainClassName` and `applicationDefaultJvmArgs ` properties from `build.gradle`.

* HTML templates: if you don't use templates you can remove the `pebble` and `kotlinx.html.jvm`
  dependencies from `build.gradle`.

## Gradle wrapper setup

You can change Gradle version in `gradle/wrapper.properties`, but if you need to regenerate the
wrapper, follow the next steps:

1. Add this to `build.gradle`:

```groovy
    wrapper {
        String wrapperBaseFile = "$projectDir/gradle/wrapper"

        gradleVersion = '3.5'
        jarFile = wrapperBaseFile + '.jar'
        scriptFile = wrapperBaseFile
        distributionType = org.gradle.api.tasks.wrapper.Wrapper.DistributionType.ALL
    }
```

2. Execute `gradle wrapper`

3. Remove the lines added in point 1 as they may cause problems in continuous integration
   environments

