
# Contributing

You can contribute code or documentation to the framework. This document will guide you through the
process or picking a task or building the code.

To know what issues are currently open and be aware of the next features yo can check the
[Project Board] at Github.

You can ask any question, suggestion or complaint at the project's [Slack channel][Slack]. And be up
to date of project's news following [@hexagon_kt] in Twitter.

[Project Board]: https://github.com/hexagonkt/hexagon/projects/1
[Slack]: https://kotlinlang.slack.com/messages/hexagon
[@hexagon_kt]: https://twitter.com/hexagon_kt

## Project Structure

The Hexagon project is composed of several modules. There are several kind of modules:

* The ones that provide a single functionality that does not depend on different implementations,
  like [hexagon_scheduler] or [hexagon_core].
* Modules that define a "Port": An interface to a feature that may have different implementations
  (ie: [port_http_server] or [port_store]).
* Adapters modules, which are Port implementations for a given tool. [store_mongodb] and
  [messaging_rabbitmq] are examples of this type of modules.
* Infrastructure modules. Components used by the project itself. These are internal modules not
  intended to be used by users like the [hexagon_benchmark] or the [hexagon_site].

[hexagon_scheduler]: https://hexagonkt.com/hexagon_scheduler/index.html
[hexagon_core]: https://hexagonkt.com/hexagon_core/index.html

[port_http_server]: https://hexagonkt.com/port_http_server/index.html
[port_store]: https://hexagonkt.com/port_store/index.html

[store_mongodb]: https://hexagonkt.com/store_mongodb/index.html
[messaging_rabbitmq]: https://hexagonkt.com/messaging_rabbitmq/index.html

[hexagon_benchmark]: https://github.com/hexagonkt/hexagon/blob/master/hexagon_benchmark/readme.md
[hexagon_site]: https://github.com/hexagonkt/hexagon/blob/master/hexagon_site/readme.md

## API Design Principles

For the public API of the library's classes and methods. The following rules are applied:

1. Prefer Kotlin STD lib methods if they exist.
2. Use named parameters instead builders.
3. Use fields instead getters/setters.
4. Objects used across the library (Singletons) are named 'Managers'. Ie: EventManager,
   SettingsManager, TemplatesManager.

## Build Hexagon

Hexagon build process requires [Docker Compose installed](https://docs.docker.com/compose/install)

You can build the project, generate the documentation and install it in your local repository
typing:

```bash
git clone https://github.com/hexagonkt/hexagon.git
cd hexagon
docker-compose up -d
./gradlew clean all publishToMavenLocal
```

The binaries are located in the `/build` directory. And the site in `/hexagon_site/build`.

## Local Setup

You can define some useful aliases like:

```bash
alias gw='./gradlew'
alias dcup='docker-compose up -d'
```

It is recommended that you add:
`gw --quiet --stacktrace clean all publishToMavenLocal` to your `.git/hooks/pre-push` script. As
this command will be executed before pushing code to the repository (saving time fixing [Travis]
build errors).

If you want to commit to the project. It is convenient to setup your own [Travis] account to execute
the CI job defined in `.travis.yml` when code is pushed to your fork.

Inside [IntelliJ Idea IDE], you need to review Kotlin's settings to make sure the JVM version is
1.8+ and API one is 1.3+ (`Project Structure > Modules > <Any Module> > Kotlin > Target Platform`).

To run the benchmarks, refer to the [hexagon_benchmark readme][hexagon_benchmark]

If you want to generate the documentation site, check the [site module readme][hexagon_site]

[IntelliJ Idea IDE]: https://www.jetbrains.com/idea

## Tools Used

* [Travis]: For continuous integration.
* [Codecov]: To check code coverage.
* [Codebeat]: To measure code quality.
* [Github]: Web hosting, project board and code hosting.
* [Bintray]: Artifact repository for JARs.

[Travis]: https://travis-ci.org
[Codecov]: https://codecov.io
[Codebeat]: https://codebeat.co
[Github]: https://github.com
[Bintray]: https://bintray.com

## Contribute

* New features should be discussed within an issue in the issue tracker before actual coding.

* For code, file names, tags and branches use either camel case or snake case only. Ie: avoid `-` or
  `.` in file names if it is possible.

* For a Pull Request to be accepted:
  - The PR should have a meaningful title.
  - If the PR refers to an issue, it should be referenced with the Github format (*#ID*).
  - It should be done to the `develop` branch.
  - The code has to pass all PR checks.
  - All public methods and field must be documented using [Dokka](https://github.com/Kotlin/dokka).
  - The code should follow the [Kotlin Coding Conventions]. With the exception of final brace
    position in `else`, `catch` and `finally` (in its own line).

* Commit format: the preferred commit format would have:
  - Summary: small summary of the change. In imperative form.
  - Issue Id: it should be written in Github's format: `#taskNumber`. Optional.
  - Description: a more complete description of the issue. It is optional.

  ```
  Summary [#Id]

  [Description]
  ```

* Bug format: when filing bugs please use the given, when, then format, including the expected 
  result. Ie:

  ```
  Given a condition
  And another condition
  When an action is taken
  And other after the first
  Then something happened
  And I expected this other thing
  ```

  If it is possible, add a link to the code that produced the error. And the stack trace if
  available.

* A feature requests: should have a detailed description and Acceptance criteria (a list of
  requirements) with the following format:

  * When *action* then *result*
  * When this is done then other thing should happen

[Kotlin Coding Conventions]: https://kotlinlang.org/docs/reference/coding-conventions.html

## Tasks and Milestones

Project's tasks and milestones are tracked in a [Github board][Project board]. You can use that
board to check the roadmap, vote the features you want (using [issue reactions]) or to pick tasks
that you wish to contribute.

[issue reactions]: https://github.com/blog/2119-add-reactions-to-pull-requests-issues-and-comments
