
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
   is meant to be simple for the users to understand it. Avoid having to read blogs, documentation
   or getting certified to use it efectively.

What are NOT project goals:

1. To be the fastest framework. Write the code fast and optimize only the critical parts. It is
   [not slow][benchmark] anyway, and it will be faster when it supports asynchronous non blocking
   operation.
2. Support all available technologies and tools: the spirit is to define simple interfaces for
   the framework's features, so users can implement integrations with different tools easily.

[Kotlin]: http://kotlinlang.org
[benchmark]: https://www.techempower.com/benchmarks

## Quick Start

1. Configure [Kotlin] in [Gradle][Setup Gradle] or [Maven][Setup Maven].
2. Setup the [JCenter] repository (follow the link and click on the `Set me up!` button).
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
    import com.hexagonkt.server.jetty.serve

    fun main(vararg args: String) {
        serve {
            get("/hello/{name}") { "Hello ${request["name"]}!" }
        }
    }
    ```

5. Run the service and view the results at: [http://localhost:2010/hello/world][Endpoint]

You can read more details in the [Services] guide.

[Setup Gradle]: https://kotlinlang.org/docs/reference/using-gradle.html
[Setup Maven]: https://kotlinlang.org/docs/reference/using-maven.html
[JCenter]: https://bintray.com/bintray/jcenter
[Gradle]: https://gradle.org
[Maven]: https://maven.apache.org
[Endpoint]: http://localhost:2010/hello/world

## Guides

* [Services]: explains how to create, build, test, package and run your services.
* [Configuration]: how to load service's configuration from different sources and data formats.
* [HTTP]: describes how to use HTTP routing and HTML templates for Web services.
* [Serialization]: details how to serialize/deserialize object instances using different formats.
* [Storage]: gives an overview of how to store data using different data stores.
* [Events]: how to support asynchronous communication with events through message brokers.
* [Scheduling]: explains how to execute tasks periodically using Cron expressions.
* [Templates]: describes how to render pages using template engines like [Pebble] or [kotlinx.html].
* [Testing]: explains how to the test Hexagon's services.

[Services]: http://hexagonkt.com/services.html
[Configuration]: http://hexagonkt.com/configuration.html
[HTTP]: http://hexagonkt.com/rest.html
[Serialization]: http://hexagonkt.com/serialization.html
[Storage]: http://hexagonkt.com/storage.html
[Events]: http://hexagonkt.com/events.html
[Scheduling]: http://hexagonkt.com/scheduling.html
[Templates]: http://hexagonkt.com/templates.html
[Testing]: http://hexagonkt.com/testing.html

[Pebble]: http://www.mitchellbosecke.com/pebble/home
[kotlinx.html]: https://github.com/Kotlin/kotlinx.html

## Status

**DISCLAIMER**: The project status is beta. Use it at your own risk. There are some modules not
started yet (ie: metrics and remote configuration) and the API is subject to change any time prior
to release 1.0.

Performance is not the primary goal, but it is taken seriously. You can check performance numbers
in the [TechEmpower Web Framework Benchmarks][benchmark]

Tests, of course, are taken into account. This is the coverage grid:

[![CoverageGrid]][Coverage]

[CoverageGrid]: https://codecov.io/gh/hexagonkt/hexagon/branch/master/graphs/icicle.svg
[Coverage]: https://codecov.io/gh/hexagonkt/hexagon

## Contribute

If you like this project and want to support it, the easiest way is to [give it a star] :v:.

If you feel like you can do more. You can contribute to the framework in different ways:

* By using it and [spreading the word][@hexagon_kt].
* Giving feedback by [Twitter][@hexagon_kt] or [Slack].
* Requesting [new features][issues] or [submitting bugs][issues].
* Vote for the features you want in the [issue tracker][issues] (using [reactions]).
* And... Drum roll... Submitting [code][contributing] or [documentation][contributing].

Refer to the [contributing.md][contributing] file for detailed information about Hexagon's
development and how to help.

To know what issues are currently open and be aware of the next features yo can check the 
[Project Board] at Github.

You can ask any question, suggestion or complaint at the project's [Slack channel][Slack]. And be up
to date of project's news following [@hexagon_kt] in Twitter.

Eventually I will thank all [contributors], but now it's just [me].

[give it a star]: https://github.com/hexagonkt/hexagon/stargazers
[@hexagon_kt]: https://twitter.com/hexagon_kt
[Slack]: https://kotlinlang.slack.com/messages/hexagon
[issues]: https://github.com/hexagonkt/hexagon/issues
[reactions]: https://github.com/blog/2119-add-reactions-to-pull-requests-issues-and-comments
[contributing]: contributing.md
[Project Board]: https://github.com/hexagonkt/hexagon/projects/1
[contributors]: https://github.com/hexagonkt/hexagon/graphs/contributors
[me]: https://github.com/jaguililla

## License

The project is licensed under the [MIT License]. This license lets you use the source for free or
commercial purposes as long as you provide attribution and don’t hold any project member liable.

[MIT License]: license.md
