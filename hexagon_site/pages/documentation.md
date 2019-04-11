
Documentation
=============

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
  
All ports are independent from each other.

[frameworks]: https://www.quora.com/Whats-the-difference-between-a-library-and-a-framework

[hexagon_scheduler]: /hexagon_scheduler/index.html
[hexagon_core]: /hexagon_core/index.html

[port_http_server]: /port_http_server/index.html
[port_store]: /port_store/index.html

[store_mongodb]: /store_mongodb/index.html
[messaging_rabbitmq]: /messaging_rabbitmq/index.html

## Hexagon Core

[Hexagon Core] module is used by all other libraries, so it would be added to your project anyway
just by using any adapter.

The main features it has are:

* [Helpers]: JVM information, a logger and other useful utilities.
* [Dependency Injection]: bind classes to creation closures or instances and inject them.
* [Instance Serialization]: parse/serialize data in different formats to class instances.
* [Configuration Settings]: load settings from different data sources and formats.

[Hexagon Core]: /hexagon_core/index.html
[Helpers]: /hexagon_core/index.html#helpers
[Dependency Injection]: /hexagon_core/index.html#dependency-injection
[Instance Serialization]: /hexagon_core/index.html#instance-serialization
[Configuration Settings]: /hexagon_core/index.html#configuration-settings

## Other Modules

The following libraries provide extra features not bound to different implementations. They will not
use dependencies outside the Hexagon toolkit.

* [Scheduling]: this module allows services to execute tasks periodically using Cron expressions.

[Scheduling]: /hexagon_scheduler/index.html

<!--
* [Testing]: explains how to the test Hexagon's services.
* [REST]: utilities to build REST services over HTTP servers.

[Testing]: /modules/testing.html
[REST]: /modules/rest.html
-->

## Toolkit Ports

These modules define features that need an specific implementation. You can use many implementations
of each port at the same time. You can even provide a custom implementations if you want to optimize
a particular use case.

These are the implemented ports:

* [HTTP Server]: describes how to use HTTP routing and HTML templates for Web services.
* [HTTP Client]: documentation to use the HTTP client module to connect to other services.
* [Storage]: gives an overview of how to store data using different data stores.
* [Messaging]: how to support asynchronous communication with messages through message brokers.
* [Templates]: describes how to render pages using template engines like [Pebble] or [kotlinx.html].

[HTTP Server]: /port_http_server/index.html
[HTTP Client]: /port_http_client/index.html
[Storage]: /port_store/index.html
[Messaging]: /port_messaging/index.html
[Templates]: /port_templates/index.html

[Pebble]: https://pebbletemplates.io
[kotlinx.html]: https://github.com/Kotlin/kotlinx.html

## Projects' Utilities

* [Services]: explains how to create, build, test, package and run your services.

* [Building]: build script tools (only Gradle now).

[Services]: /create_services.html
[Building]: /gradle.html
