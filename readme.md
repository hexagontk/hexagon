
<h1 align="center">
  <a href="http://hexagonkt.com">
    <img alt="Hexagon" src="hexagon_site/assets/tile-small.png" />
  </a>
  <br>
  Hexagon
</h1>

<h4 align="center">The atoms of your platform</h4>

<p align="center">
  <a href="https://travis-ci.org/hexagonkt/hexagon">
    <img
      src="https://img.shields.io/travis/hexagonkt/hexagon.svg?colorA=0073BB&style=flat-square" 
      alt="Travis CI" />
  </a>
  <a href="https://codecov.io/gh/hexagonkt/hexagon">
    <img
      src=
        "https://img.shields.io/codecov/c/github/hexagonkt/hexagon.svg?colorA=0073BB&style=flat-square"
      alt="Codecov" />
  </a>
  <a href="https://bintray.com/jamming/maven/hexagon_core/_latestVersion">
    <img
      src=
        "https://img.shields.io/bintray/v/jamming/maven/hexagon_core.svg?colorA=0073BB&style=flat-square"
      alt="Bintray" />
  </a>
</p>

<p align="center">
  <a href="http://hexagonkt.com/TODO">Getting Started</a> |
  <a href="http://hexagonkt.com/TODO">Guides</a> |
  <a href="http://hexagonkt.com/TODO">API Reference</a> |
  <a href="http://hexagonkt.com/TODO">Community</a>
</p>

---

Hexagon is a microservices framework that doesn't follow the flock. It is written in [Kotlin] and
its pursose is to ease the building of services (Web applications, APIs or queue consumers) that run
inside a cloud platform.

The goals of the project are:

1. Be simple to use: make it easy to develop user services (HTTP or message consumers) quickly. It
   is focused on making the usual tasks easy, rather than making a complex tool with a lot of
   features.
2. Make it easy to hack: allow the user to add extensions or change the framework itself. The code
   is meant to be simple for the users to understand it, instead of having to read blogs,
   documentation or getting certified to use it efectiveliy.

What are NOT project goals:

1. Code the fastest framework. Write the code fast and optimize only the critical parts. It is
   [not slow][benchmark] anyway, and it will be faster when it fully support asynchronous non 
   blocking operation.
2. Support all available technologies and tools: the spirit is to define simple interfaces for
   the framework's features, so users can implement integrations with different tools easily.

[not slow]:

## Quick Start

1. Setup Kotlin in [Gradle][Setup Gradle] or [Maven][Setup Maven].
2. Setup the [JCenter repository](https://bintray.com/bintray/jcenter) (click on the `Set me up!`
   button).
3. Add the dependency:

  * In [Gradle]. Import it inside `build.gradle`:

    ```groovy
    compile ("com.hexagonkt:server_jetty:0.21.0")
    ```

  * In [Maven]. Declare the dependency in `pom.xml`:

    ```xml
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>server_jetty</artifactId>
      <version>0.21.0</version>
    </dependency>
    ```

4. Write the code in the `src/main/kotlin/Hello.kt` file:

    ```kotlin
    import com.hexagonkt.server.*
    import com.hexagonkt.server.jetty.*

    fun main(vararg args: String) {
        serve(JettyServletEngine()) {
            get("/hello/{name}") { "Hello ${request["name"]}!" }
        }
    }
    ```

5. Run the service and view the results at: [http://localhost:2010/hello/world][Endpoint]

You can read more details in the [Services] guide.

[Setup Gradle]: https://kotlinlang.org/docs/reference/using-gradle.html
[Setup Maven]: https://kotlinlang.org/docs/reference/using-maven.html
[Endpoint]: http://localhost:2010/hello/world
[Maven]: https://maven.apache.org
[Gradle]: https://gradle.org

## Guides

* Task scheduling using Cron expressions.

* [Services]: explains how to create, build, test, package and run your services.
* [Configuration]: how to load service's configuration from different sources and data formats.
* [HTTP]: describes how to use HTTP routing and HTML templates for Web services.
* [Serialization]: details how to serialize/deserialize object instances using different formats.
* [Storage]: gives an overview of how to store data using different data stores.
* [Events]: how to support asynchronous communication with events through message brokers.

* [Scheduling]: supports the execution of tasks periodically using Cron expressions.
* [Templates]: allow the service to render results using [Pebble] or [kotlinx.html].
* [Testing]: Hexagon adds utilities to ease the testing of its services.

[Pebble]: http://www.mitchellbosecke.com/pebble/home
[kotlinx.html]: https://github.com/Kotlin/kotlinx.html

[Services]: http://hexagonkt.com/services.html
[HTTP]: http://hexagonkt.com/rest.html
[Serialization]: http://hexagonkt.com/serialization.html
[Storage]: http://hexagonkt.com/storage.html
[Events]: http://hexagonkt.com/events.html
[Configuration]: http://hexagonkt.com/configuration.html
[Templates]: http://hexagonkt.com/templates.html
[Scheduling]: http://hexagonkt.com/scheduling.html
[Testing]: http://hexagonkt.com/testing.html

## Status

**DISCLAIMER**: The project status is beta. Use it at your own risk. There are some modules not
started yet (ie: metrics and remote configuration) and the API is subject to change any time prior
 to release 1.0.

Performance is not the primary goal, but it is taken seriously. You can check performance numbers
in the [TechEmpower Web Framework Benchmarks][benchmark]

Test, of course, are taken into account. This is the coverage grid:

[![CoverageGrid]][Coverage]

[benchmark]: https://www.techempower.com/benchmarks
[CoverageGrid]: https://codecov.io/gh/hexagonkt/hexagon/branch/master/graphs/icicle.svg
[Coverage]: https://codecov.io/gh/hexagonkt/hexagon
[Kotlin]: http://kotlinlang.org

## Contribute

If you like this project and want to support it, the easiest way is to give it a star :victory:.
TODO Link to add star.

If you feel like you can do more. You can contribute to the framework in different ways:

* By using it and spreading the word.
* Giving feedback by [Twitter] or [Slack].
* Requesting new features, submitting bugs.
* Vote for the features you want in the issue tracker
* Adding documentation to the project.
* Submitting code

Refer to the [contributing.md](contributing.md) file for detailed information about Hexagon's
development and how to help.

To know what issues are currently open and be aware of the next features yo can check the 
[Project board](https://github.com/hexagonkt/hexagon/projects/1) at [Github].

You can ask any question, suggestion or complaint at the project's [Slack channel]. And be up to 
date of project's news following [@hexagon_kt] in [Twitter].

Eventually I will thank all [contributors], but now it's just [me].

[@hexagon_kt]: https://twitter.com/hexagon_kt
[Slack channel]: https://kotlinlang.slack.com/messages/hexagon
[contributors]: https://github.com/hexagonkt/hexagon/graphs/contributors
[me]: https://github.com/jaguililla

## License

The project is licensed under the [MIT License](license.md). This license lets you use the source
for free or commercial purposes as long as you provide attribution and don’t hold any project member
liable.
