[![BuildImg]][Build] [![CoverageImg]][Coverage]
[![DownloadImg]][Download] [![KanbanImg]][Kanban]
[![WebImg]][Web]

[BuildImg]: https://travis-ci.org/jamming/hexagon.svg?branch=master
[Build]: https://travis-ci.org/jamming/hexagon

[CoverageImg]: https://codecov.io/github/jamming/hexagon/coverage.svg?branch=master
[Coverage]: https://codecov.io/github/jamming/hexagon?branch=master

[DownloadImg]: https://img.shields.io/bintray/v/jamming/maven/Hexagon.svg
[Download]: https://bintray.com/jamming/maven/Hexagon/_latestVersion

[KanbanImg]: https://img.shields.io/badge/kanban-huboard-blue.svg
[Kanban]: https://huboard.com/jamming/hexagon

[WebImg]: https://img.shields.io/badge/web-there4.co%2Fhexagon-blue.svg
[Web]: http://there4.co/hexagon


HEXAGON
=======
### The atoms of your platform

Hexagon is a micro services framework that doesn't follow the flock. It is written in [Kotlin] and
uses [Ratpack], [Jackson], [RabbitMQ] and [MongoDB]. It takes care of:

* rest
* messaging
* serialization
* storage
* events
* configuration
* logging
* scheduling

The purpose of the project is to provide a micro services framework with the following priorities
(in order):

* Simple to use
* Easily hackable
* Be small

DISCLAIMER: The project status right now is alpha... You should not use it in production yet

## Getting started

Check the project [Website](http://there4.co/hexagon)

## Setup

Requires [Docker Compose installed](https://docs.docker.com/compose/install)

You can build the project and its documentation after getting the source from github running:

    ./gradle/wrapper --no-daemon clean site

The results are located in the `/build` directory


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
