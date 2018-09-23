
# Contributing

You can contribute code or documentation to the framework. This document will guide you through the
process or picking a task or building the code.

To know what issues are currently open and be aware of the next features yo can check the
[Project Board] at Github.

Make sure you read the project [Quick Start] guide to know the project structure before picking a
task.

You can ask any question, suggestion or complaint at the project's [Slack channel][Slack]. And be up
to date of project's news following [@hexagon_kt] in Twitter.

[@hexagon_kt]: https://twitter.com/hexagon_kt
[Slack]: https://kotlinlang.slack.com/messages/hexagon
[Project Board]: https://github.com/hexagonkt/hexagon/projects/1

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
alias dcupd='docker-compose up -d'
```

It is recommended that you add:
`gw clean all publishToMavenLocal` to your `.git/hooks/pre-push` script. As this command will be
executed before pushing code to the repository (saving time fixing [Travis] build errors).

If you want to commit to the project. It is convenient to setup your own [Travis] account to execute
the CI job defined in `.travis.yml` when code is pushed to your fork.

Inside Idea IDE, you need to review Kotlin's settings to make sure JVM 1.8 and API 1.1 is used
(`Project Structure > Modules > <Any Module> > Kotlin > Target Platform`).

## Benchmarking

The benchmark are the same run inside [TechEmpower Framework Benchmarks][TFB], to run them:

1. Start the benchmark's compose file. From the project's root execute:
   `docker-compose -f docker-compose.yaml -f hexagon_benchmark/docker-compose.yaml up -d`
2. Run `gw hexagon_benchmark:test -Phost=localhost -Pport=9020` where "localhost" and "9020" should
   point to the endpoint with the benchmark application you want to test.

[TFB]: https://www.techempower.com/benchmarks

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

* Make sure you read the project [Quick Start] guide to know the project structure before picking a
  task.

* New features should be discussed within an issue in the issue tracker before actual coding.

* For code, file names, tags and branches use either camel case or snake case only. Ie: avoid `-` in
  file names if it is possible.

* For a Pull Request to be accepted:
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
[Quick Start]: http://hexagonkt.com/quick_start.html
[Kotlin Coding Conventions]: https://kotlinlang.org/docs/reference/coding-conventions.html

## Tasks and Milestones

Project's tasks and milestones are tracked in a [Github board]. You can use that board to check the
roadmap, vote the features you want (using [issue ractions]) or to pick tasks that you wish to 
contribute.

[Github board]: https://github.com/hexagonkt/hexagon/projects/1
[issue reactions]: https://github.com/blog/2119-add-reactions-to-pull-requests-issues-and-comments
