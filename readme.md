[![Build Img]][Build Status] [![Coverage Img]][Coverage Status]
[![Download Img]][Download Status] [![Kanban Img]][Kanban Status]
[![Web Img]][Web Link]

[Build Img]: https://travis-ci.org/jamming/hexagon.svg?branch=master
[Build Status]: https://travis-ci.org/jamming/hexagon

[Coverage Img]: https://codecov.io/github/jamming/hexagon/coverage.svg?branch=master
[Coverage Status]: https://codecov.io/github/jamming/hexagon?branch=master

[Download Img]: https://img.shields.io/bintray/v/jamming/maven/Hexagon.svg
[Download Status]: https://bintray.com/jamming/maven/Hexagon/_latestVersion

[Kanban Img]: https://img.shields.io/badge/kanban-huboard-blue.svg
[Kanban Status]: https://huboard.com/jamming/hexagon

[Web Img]: https://img.shields.io/badge/web-there4.co%2Fhexagon-blue.svg
[Web Link]: http://there4.co/hexagon


HEXAGON
=======

The atoms of your platform

A micro services framework that doesn't follow the flock. It is written in [Kotlin] and based in
[Ratpack], [Jackson], [RabbitMQ], [MongoDB]

Why another web framework? It doesn't follow trends (Spring Boot).

DISCLAIMER: The project status right now is alpha... You should not use it in production (though
I'm going to do it anyway).


## Arch

Codecs (Serializer/Deserializer), Repositories, Controllers, Views, Model


## Conventions

Main class package is the root, controller name is app name

## Release

    rm -rf .gradle
    gw --no-daemon clean javadoc check jacoco site

## Coding standards

For code and file names, etc. Use either camel case or snake case (if possible)
avoid `-` in file names, etc.


CONTRIBUTE
----------

If you like it and are willing to help me develop it, we can talk ;)

PRs not to master

Follow conventions

Cover the changes


WORKFLOW
--------

git-extras + git-flow + hub + huboard + bintray


GETTING STARTED
===============

Hexagon is available at [JCenter]!!!

```groovy
dependencies {
  compile ('co.there4.hexagon:hexagon:${version}') { transitive = false }
  // Import the backend you are going to use
  compile 'io.undertow:undertow-servlet:1.3.19.Final'
}
```

```xml
<dependency>
  <groupId>co.there4.hexagon</groupId>
  <artifactId>hexagon</artifactId>
  <version>${version}</version>
</dependency>
```

[API Docs](http://there4.co/hexagon/dokka/)

[JCenter]: https://bintray.com/jamming/maven/Hexagon


Getting Started
---------------

```java
import co.there4.hexagon.http.*

fun main(args: Array<String>) {
    get("/") { ok("Hello World!") }
    run()
}
```

View at: http://localhost:4321/hello

You can also check out the javadoc. After getting the source from github run:

    ./gradle/wrapper docs

The result is put in `/build/dokka` and `/build/site`


The Mission
-----------

The purpose of the project is to provide a micro services framework with the following priorities (in
order):

* Simple to use
* Easily hackable
* Extensible to different backends
* Be small


IDE Settings
------------

Take care of the output path in IntelliJ, for Gradle projects maybe the tests classes are not
generated in the same place as Gradle itself.


Examples
---------

Check out and try the examples in the source code.

<!---
[Code Examples][Examples]
[Examples]: https://github.com/jamming/hexagon/tree/master/src/test/kotlin/hexagon/Examples.kt

You can also check the [integration tests][tests]
[tests]: https://github.com/jamming/hexagon/tree/master/src/test/kotlin/hexagon/it/undertow
-->


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
