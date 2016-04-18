title=Hexagon
date=2016-04-13
type=page
status=published
~~~~~~


Hexagon ${projectVersion}
=========================

The atoms of your platform

A micro services framework that doesn't follow the flock. It is written in [Kotlin] and uses
[Ratpack], [Jackson], [RabbitMQ], [MongoDB]

The purpose of the project is to provide a micro services framework with the following priorities
(in order):

* Simple to use
* Easily hackable
* Be small


## Getting Started

Get the dependency from [JCenter] (you need to setup de repository first):

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

```java
import co.there4.hexagon.rest.ratpack.*

fun main(args: Array<String>) {
    serverStart {
        handlers {
            get("hello/:name") { render("Hello ${pathTokens["name"]}!") }
        }
    }
}
```

Launch it and view the results at: [http://localhost:5050/hello]
