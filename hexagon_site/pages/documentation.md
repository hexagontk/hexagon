
Modules
======

## Utilities
* [Services]: explains how to create, build, test, package and run your services.
* [Building]: build script tools (only Gradle now).

## Core
* [Injection]
* [Serialization]: details how to serialize/deserialize object instances using different formats.
* [Configuration]: how to load service's configuration from different sources and data formats.

## Modules
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

API Reference
=============

## Modules

Modules that provide features without the need of custom implementations:

* [Hexagon Core](/api/hexagon_core/index.html)
* [Hexagon Scheduler](/api/hexagon_scheduler/index.html)

## Ports

Interfaces to a certain feature that must be implemented by an adapter:

* [Client Port](/api/port_http_client/index.html)
* [Messaging Port](/api/port_messaging/index.html)
* [Server Port](/api/port_http_server/index.html)
* [Store Port](/api/port_store/index.html)
* [Templates Port](/api/port_templates/index.html)

## Adapters

Concrete ports implementations:

### Messaging
* [RabbitMQ Messaging Adapter](/api/messaging_rabbitmq/index.html)

### Server
* [Servlet Server Adapter](/api/http_server_servlet/index.html)
* [Jetty Server Adapter](/api/http_server_jetty/index.html)

### Store
* [MongoDB Store Adapter](/api/store_mongodb/index.html)

### Templates
* [Pebble Templates Adapter](/api/templates_pebble/index.html)
