
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
    <img src="https://travis-ci.org/hexagonkt/hexagon.svg?branch=master" alt="Travis CI" />
  </a>
  <a href="https://codecov.io/gh/hexagonkt/hexagon">
    <img
      src="https://codecov.io/gh/hexagonkt/hexagon/branch/master/graph/badge.svg"
      alt="Codecov" />
  </a>
  <a href="https://codebeat.co/projects/github-com-hexagonkt-hexagon-master">
    <img
      src="https://codebeat.co/badges/f8fafe6f-767a-4248-bc34-e6d4a2acb971"
      alt="Codebeat" />
  </a>
  <a href="https://bintray.com/jamming/maven/hexagon_core/_latestVersion">
    <img
      src="https://api.bintray.com/packages/jamming/maven/hexagon_core/images/download.svg"
      alt="Bintray" />
  </a>
</p>

<p align="center">
  <a href="http://hexagonkt.com/quick_start.html">Quick Start</a> |
  <a href="http://hexagonkt.com/guides.html">Guides</a> |
  <a href="http://hexagonkt.com/api.html">API Reference</a>
</p>

---

Hexagon is a microservices library written in [Kotlin]. Its purpose is to ease the building of
services (Web applications, APIs or queue consumers) that run inside a cloud platform.

It is meant to provide abstraction from underlying technologies to be able to change them with
minimum impact. It is designed to fit in applications that conforms to the [Hexagonal Architecture]
(also called [Clean Architecture] or [Ports and Adapters Architecture]).

The goals of the project are:

1. Be simple to use: make it easy to develop user services (HTTP or message consumers) quickly. It
   is focused on making the usual tasks easy, rather than making a complex tool with a lot of
   features.
2. Make it easy to hack: allow the user to add extensions or change the framework itself. The code
   is meant to be simple for the users to understand it. Avoid having to read blogs, documentation
   or getting certified to use it efectively.

What are NOT project goals:

1. To be the fastest framework. Write the code fast and optimize only the critical parts. It is
   [not slow][benchmark] anyway.
2. Support all available technologies and tools: the spirit is to define simple interfaces for
   the most common features , so users can implement integrations with different tools easily.
3. To be usable from Java. Hexagon is *Kotlin first*.

[Kotlin]: http://kotlinlang.org
[Hexagonal Architecture]: http://fideloper.com/hexagonal-architecture
[Clean Architecture]: https://8thlight.com/blog/uncle-bob/2012/08/13/the-clean-architecture.html
[Ports and Adapters Architecture]: https://herbertograca.com/2017/09/14/ports-adapters-architecture
[benchmark]: https://www.techempower.com/benchmarks

## Quick Start

1. Configure [Kotlin] in [Gradle][Setup Gradle] or [Maven][Setup Maven].
2. Setup the [JCenter] repository (follow the link and click on the `Set me up!` button).
3. Add the dependency:

  * In Gradle. Import it inside `build.gradle`:

    ```groovy
    compile ("com.hexagonkt:server_jetty:0.23.2")
    ```

  * In Maven. Declare the dependency in `pom.xml`:

    ```xml
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>server_jetty</artifactId>
      <version>0.23.2</version>
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

You can read more details reading the [Quick Start] page, or checking the [guides].

[Setup Gradle]: https://kotlinlang.org/docs/reference/using-gradle.html
[Setup Maven]: https://kotlinlang.org/docs/reference/using-maven.html
[JCenter]: https://bintray.com/bintray/jcenter
[Endpoint]: http://localhost:2010/hello/world
[Quick Start]: http://hexagonkt.com/quick_start.html
[guides]: http://hexagonkt.com/guides.html

## Status

**DISCLAIMER**: The project status is beta. Use it at your own risk. There are some modules not
finished yet (e.g: storage and HTTP client) and the API is subject to change any time prior to
release 1.0.

It is used in personal not released projects to develop APIs and Web applications.

Performance is not the primary goal, but it is taken seriously. You can check performance numbers
in the [TechEmpower Web Framework Benchmarks][benchmark]. You can also run the stress tests using
[JMeter] with the `hexagon_benchmark/load_test.jmx` file.

Tests, of course, are taken into account. This is the coverage grid:

[![CoverageGrid]][Coverage]

The code quality is checked by Codebeat:

[![codebeat badge]][codebeat page]

[JMeter]: http://jmeter.apache.org
[CoverageGrid]: https://codecov.io/gh/hexagonkt/hexagon/branch/master/graphs/icicle.svg
[Coverage]: https://codecov.io/gh/hexagonkt/hexagon
[codebeat badge]: https://codebeat.co/badges/f8fafe6f-767a-4248-bc34-e6d4a2acb971
[codebeat page]: https://codebeat.co/projects/github-com-hexagonkt-hexagon-master

## Contribute

If you like this project and want to support it, the easiest way is to [give it a star] :v:.

If you feel like you can do more. You can contribute to the framework in different ways:

* By using it and [spreading the word][@hexagon_kt].
* Giving feedback by [Twitter][@hexagon_kt] or [Slack].
* Requesting [new features or submitting bugs][issues].
* Voting for the features you want in the [issue tracker][issues] (using [reactions]).
* And... Drum roll... Submitting [code or documentation][contributing].

To know what issues are currently open and be aware of the next features you can check the
[Project Board] at Github.

You can ask any question, suggestion or complaint at the project's [Slack channel][Slack]. And be up
to date of project's news following [@hexagon_kt] in Twitter.

Thanks to all project's [contributors]!

[give it a star]: https://github.com/hexagonkt/hexagon/stargazers
[@hexagon_kt]: https://twitter.com/hexagon_kt
[Slack]: https://kotlinlang.slack.com/messages/hexagon
[issues]: https://github.com/hexagonkt/hexagon/issues
[reactions]: https://github.com/blog/2119-add-reactions-to-pull-requests-issues-and-comments
[contributing]: contributing.md
[Project Board]: https://github.com/hexagonkt/hexagon/projects/1
[contributors]: https://github.com/hexagonkt/hexagon/graphs/contributors

## License

The project is licensed under the [MIT License]. This license lets you use the source for free or
commercial purposes as long as you provide attribution and don’t hold any project member liable.

[MIT License]: license.md
