[![BuildImg]][Build] [![CoverageImg]][Coverage]
[![DownloadImg]][Download] [![WebImg]][Web]

[BuildImg]: https://travis-ci.org/jaguililla/hexagon.svg?branch=master
[Build]: https://travis-ci.org/jaguililla/hexagon

[CoverageImg]: https://codecov.io/gh/jaguililla/hexagon/branch/master/graph/badge.svg
[Coverage]: https://codecov.io/gh/jaguililla/hexagon

[DownloadImg]: https://img.shields.io/bintray/v/jamming/maven/Hexagon.svg
[Download]: https://bintray.com/jamming/maven/Hexagon/_latestVersion

[WebImg]: https://img.shields.io/badge/web-there4.co%2Fhexagon-blue.svg
[Web]: http://there4.co/hexagon

HEXAGON
=======
### The atoms of your platform

Hexagon is a micro services framework that doesn't follow the flock. It is written in [Kotlin] and
uses [RabbitMQ] and [MongoDB]. It takes care of:

* [rest](http://there4.co/hexagon/rest.html)
* [messaging](http://there4.co/hexagon/messaging.html) (TODO Write documentation)
* [serialization](http://there4.co/hexagon/serialization.html) (TODO Write documentation)
* [storage](http://there4.co/hexagon/storage.html) (TODO Write documentation)
* [events](http://there4.co/hexagon/events.html)
* [configuration](http://there4.co/hexagon/configuration.html) (TODO Partial implementation)
* [templates](http://there4.co/hexagon/templates.html) (TODO Partial implementation)
* [scheduling](http://there4.co/hexagon/scheduling.html)
* [testing](http://there4.co/hexagon/testing.html) (TODO Write documentation)
* [builds](http://there4.co/hexagon/builds.html)

The purpose of the project is to provide a micro services framework with the following priorities
(in order):

1. Simple to use
2. Easily hackable
3. Be small

The name and logo are an hexagon because it is the usual way of representing a microservice in a
diagram.

DISCLAIMER: The project status right now is beta. Use it at your own risk

[Kotlin]: http://kotlinlang.org
[RabbitMQ]: http://www.rabbitmq.com
[MongoDB]: https://www.mongodb.com

## Getting started

Get the dependency from [JCenter][JCenter] (you need to setup de repository first):

Gradle:

```groovy
compile ('co.there4:hexagon:${version}')
```

Maven:

```xml
<dependency>
  <groupId>co.there4</groupId>
  <artifactId>hexagon</artifactId>
  <version>${version}</version>
</dependency>
```

[JCenter]: https://bintray.com/jamming/maven/Hexagon

Write the code:

```kotlin
import co.there4.hexagon.web.*

fun main(args: Array<String>) {
    get("hello/:name") { ok("Hello ${pathTokens["name"]}!") }
    run()
}
```

Launch it and view the results at: [http://localhost:2010/hello]


## Build and Contribute

Requires [Docker Compose installed](https://docs.docker.com/compose/install)

Prior to running the tests you need to import sample data with the following commands:

    tar -Jxvf db.txz && \
    mongorestore dump/hello_world/ --db hello_world && \
    rm -rf dump

You can build the project, generate the documentation and install it in your local repository
typing:

    git clone https://github.com/jaguililla/hexagon.git
    cd hexagon
    docker-compose -f src/test/services.yml up -d
    ./gradle/wrapper --no-daemon clean docs site publishToMavenLocal

The results are located in the `/build` directory

Code coverage grid:

![coverage](https://codecov.io/gh/jaguililla/hexagon/branch/master/graphs/tree.svg)


## Lazybones template project

You have just created a simple project for managing your own Lazybones project
templates. You get a build file (`build.gradle`) and a directory for putting
your templates in (`templates`).

To get started, simply create new directories under the `templates` directory
and put the source of the different project templates into them. You can then
package and install the templates locally with the command:

    ./gradlew installAllTemplates

You'll then be able to use Lazybones to create new projects from these templates.
If you then want to distribute them, you will need to set up a Bintray account,
populate the `repositoryUrl`, `repositoryUsername` and `repositoryApiKey` settings
in `build.gradle`, add new Bintray packages in the repository via the Bintray
UI, and finally publish the templates with

    ./gradlew publishAllTemplates

You can find out more about creating templates on [the GitHub wiki][1].

[1]: https://github.com/pledbrook/lazybones/wiki/Template-developers-guide

## Gradle wrapper setup

You can change Gradle version in `gradle/wrapper.properties`, but if you need to regenerate the
wrapper, follow the next steps:

1. Add this to `build.gradle`:

```groovy
    import static org.gradle.api.tasks.wrapper.Wrapper.DistributionType.*

    wrapper {
        String wrapperBaseFile = "$projectDir/gradle/wrapper"

        gradleVersion = '3.2.1'
        jarFile = wrapperBaseFile + ".jar"
        scriptFile = wrapperBaseFile
        distributionType = ALL
    }
```

2. Execute `gradle wrapper`

3. Remove the lines added in point 1 as they may cause problems in continuous integration
   environments

LICENSE
-------

MIT License

Copyright (c) 2016 Juanjo Aguililla

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
