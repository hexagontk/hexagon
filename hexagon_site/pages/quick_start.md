
# What is Hexagon

Hexagon is a microservices library written in [Kotlin] which pursose is to ease the building of
services (Web applications, APIs or queue consumers) that run inside cloud platforms.

It is meant to provide abstraction from underlying technoligies (data storage, HTTP server 
engines, etc.) to be able to change them with minimum impact.

It only supports Kotlin, Java is not a targeted language for the framework.
  
## Project Structure

The Hexagon is a multiple module project. There are several kind of modules:

* The ones that provide a single functionality (which doesn't depend on different implementations).
  Like Scheduling or Core.
* Modules that define a "Port": An interface to use a feature that may have different 
  implementations.
* "Ports", which are ports implementations for a given tool.
* Infrastructure modules. Components used by the project itself, like the benchmark, the examples
  and the site generator.

### Site Generation

### Benchmark

Code for the benchmark

### Starters

Gradle utility scripts



System:

Service: API, message broker
Application: are a kind of service which is final (meant for users)
WebApplication, Desktop Application, Mobile Application or Browser Aplication

Managers: singletons to manage across an application and/or between services
    EventManager
    SettingsManager
    TemplatesManager
    
    

Motivation
Ports and Adapters

# Why it was created

* More time reading framework docs than coding
* Frameworks targeted to everyone instead my custom needs
* Easy to hack better than work ok for every use case
* For fun!
* To learn Kotlin

# How it works

The framework is build upon smaller pieces:

## Modules

ports and adapters

## Components

events, core, templates

Features

Add Needed infrastructure for µS

## Adapters

feature implementations

The heavy lifting is done by these ones (Undertow, Jetty, RabbitMQ, MongoDB)

# Concepts

Service (API, Web, Consumer)

Paths

Routes

Filters

Handlers

Callbacks

Routers

Servers

Templates

Events

... Stores + Rest

# Roadmap

The focus will be library ease of use and API freeze

Async

Metrics

Registering

Logging

Health checks

Web to download starters

Tool for client requests

CBOR

Take out templates port

Code examples (take Spark ones)
