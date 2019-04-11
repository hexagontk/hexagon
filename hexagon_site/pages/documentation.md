
Documentation Index
===================

## Toolkit Structure
The project is developed as a set of [libraries][frameworks] that you call as opposed to
[frameworks] that call your code inside them. Being a library means that you won't need special
build settings or tools.

Project's modules fall into two categories: the ones for internal use, and the ones that provide a
functionality that clients can import. There are three kind of client libraries:

* The ones that provide a single functionality that does not depend on different implementations,
  like [hexagon_scheduler] or [hexagon_core].
* Modules that define a "Port": An interface to a feature that may have different implementations
  (ie: [port_http_server] or [port_store]). These ones can not be used by themselves and in their
  place, an adapter implementing them should be added to the list of dependencies.
* Adapter modules, which are Port implementations for a given tool. [store_mongodb] and
  [messaging_rabbitmq] are examples of this type of modules.
  
Ports are independent from each other.

[frameworks]: https://www.quora.com/Whats-the-difference-between-a-library-and-a-framework

[hexagon_scheduler]: https://hexagonkt.com/hexagon_scheduler/index.html
[hexagon_core]: https://hexagonkt.com/hexagon_core/index.html

[port_http_server]: https://hexagonkt.com/port_http_server/index.html
[port_store]: https://hexagonkt.com/port_store/index.html

[store_mongodb]: https://hexagonkt.com/store_mongodb/index.html
[messaging_rabbitmq]: https://hexagonkt.com/messaging_rabbitmq/index.html

## Hexagon Core

Hexagon Core module is used by all other libraries, so it would be added to your project anyway just
by using any adapter.

The main features it has are:

* [Helpers]: JVM information, a logger and other useful utilities.
* [Dependency Injection]: bind classes to creation closures or instances and inject them.
* [Instance Serialization]: parse/serialize data in different formats to class instances.
* [Configuration Settings]: load settings from different data sources and formats.

[Helpers]: /hexagon_core/index.html#helpers
[Dependency Injection]: /hexagon_core/index.html#dependency-injection
[Instance Serialization]: /hexagon_core/index.html#instance-serialization
[Configuration Settings]: /hexagon_core/index.html#configuration-settings

## Hexagon Modules
* [Scheduling]: explains how to execute tasks periodically using Cron expressions.
* [Testing]: explains how to the test Hexagon's services.
* [REST]: utilities to build REST services over HTTP servers.

## Ports
* [HTTP]: describes how to use HTTP routing and HTML templates for Web services.
* [Client]: documentation to use the HTTP client module to connect to other services.
* [Storage]: gives an overview of how to store data using different data stores.
* [Messaging]: how to support asynchronous communication with messages through message brokers.
* [Templates]: describes how to render pages using template engines like [Pebble] or [kotlinx.html].

[Building]: /gradle.html
[Services]: /create_services.html
[Configuration]: /core/configuration.html
[REST]: /modules/rest.html
[HTTP]: /ports/server.html
[Client]: /ports/client.html
[Serialization]: /core/serialization.html
[Storage]: /ports/storage.html
[Messaging]: /ports/messaging.html
[Scheduling]: /modules/scheduling.html
[Templates]: /ports/templates.html
[Testing]: /modules/testing.html

[Pebble]: http://www.mitchellbosecke.com/pebble/home
[kotlinx.html]: https://github.com/Kotlin/kotlinx.html

## Projects' Utilities

* [Services]: explains how to create, build, test, package and run your services.

* [Building]: build script tools (only Gradle now).
