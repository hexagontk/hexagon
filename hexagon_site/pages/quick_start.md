
# What is Hexagon

Hexagon is a microservices library written in [Kotlin] which purpose is to ease the building of
services (Web applications, APIs or queue consumers) that run inside cloud platforms.

The project is developed as a [library][frameworks] that you call as opposed to [frameworks] that 
call your code inside them. Being a library means that you won't need special build settings or
tools.

It is meant to provide abstraction from underlying technologies (data storage, HTTP server 
engines, etc.) to be able to change them with minimum impact.

It only supports [Kotlin], Java is not a targeted language for the framework.

[Kotlin]: http://kotlinlang.org
[frameworks]: https://www.quora.com/Whats-the-difference-between-a-library-and-a-framework

# Middleware definition

TODO Mounting routers you can accomplish this

# Project Structure

The Hexagon is a multiple module project. There are several kind of modules:

* The ones that provide a single functionality (which does not depend on different implementations).
  Like Scheduling or Core.
* Modules that define a "Port": An interface to use a feature that may have different 
  implementations.
* "Ports", which are ports implementations for a given tool.
* Infrastructure modules. Components used by the project itself, like the benchmark, the examples
  and the site generator.

# How it works

The framework is build upon smaller pieces:

## Modules

ports and adapters

# Concepts

Service (API, Web, Consumer)
