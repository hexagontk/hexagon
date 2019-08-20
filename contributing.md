
# Contributing

You can contribute code or documentation to the framework. This document will guide you through the
process or picking a task or building the code.

To know what issues are currently open and be aware of the next features yo can check the
[Project Board] at Github. Issues with the [help wanted] tag are recommended for a first time
contribution.

You can ask any question, suggestion or complaint at the project's [Slack channel][Slack]. And be up
to date of project's news following [@hexagon_kt] in Twitter.

[Project Board]: https://github.com/hexagonkt/hexagon/projects/1
[Slack]: https://kotlinlang.slack.com/messages/hexagon
[@hexagon_kt]: https://twitter.com/hexagon_kt
[help wanted]: https://github.com/hexagonkt/hexagon/issues?q=is%3Aissue+is%3Aopen+label%3A%22help+wanted%22

## Project Structure

The Hexagon project is composed of several modules. Most of the modules publish libraries for their
use by other projects (check the [Project Libraries] section of the readme file for more details).

Aside of that kind of modules, you can also find infrastructure modules: components used by the
project itself. These are internal modules not intended to be used by users (like the
[hexagon_benchmark] or the [hexagon_site]).

[Project Libraries]: https://github.com/hexagonkt/hexagon/blob/master/README.md#hexagon-libraries
[hexagon_benchmark]: https://github.com/hexagonkt/hexagon/blob/master/hexagon_benchmark/README.md
[hexagon_site]: https://github.com/hexagonkt/hexagon/blob/master/hexagon_site/README.md

## API Design Principles

For the public API of the library's classes and methods. The following rules are applied:

1. Prefer Kotlin STD lib methods if they exist.
2. Follow Kotlin language and library conventions. Ie: check methods will be named `require...`
3. Use named parameters instead builders.
4. Use fields instead getters/setters.
5. Objects used across the library (Singletons) are named 'Managers'. Ie: EventManager,
   SettingsManager, TemplatesManager.

## Build Hexagon

Hexagon build process requires [Docker Compose installed](https://docs.docker.com/compose/install)

For `rabbitmq` container to work properly, you should follow the Docker setup described in:
[https://www.rabbitmq.com/install-debian.html] (inside the "With Docker" section)

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

* For a Pull Request to be accepted, follow the [pull request template] recommendations. And check
  that the code follows the [Kotlin Coding Conventions], with the exception of final brace
  position in `else`, `catch` and `finally` (in its own line).

* Commit format: the preferred commit format would have:
  - Summary: small summary of the change. In imperative form.
  - Issue Id: it should be written in Github's format: `#taskNumber`. Optional.
  - Description: a more complete description of the issue. It is optional.

  ```
  Summary [#Id]

  [Description]
  ```

* Bug format: when filing bugs please comply with the [bug template] requirements.

* A feature requests should follow the [feature template] rules.

[pull request template]: https://github.com/hexagonkt/hexagon/blob/master/.github/pull_request_template.md
[Kotlin Coding Conventions]: https://kotlinlang.org/docs/reference/coding-conventions.html
[bug template]: https://github.com/hexagonkt/hexagon/blob/master/.github/ISSUE_TEMPLATE/bug.md
[feature template]: https://github.com/hexagonkt/hexagon/blob/master/.github/ISSUE_TEMPLATE/feature.md

## Tasks and Milestones

Project's tasks and milestones are tracked in a [Github board][Project board]. You can use that
board to check the roadmap, vote the features you want (using [issue reactions]) or to pick tasks
that you wish to contribute.

[issue reactions]: https://github.com/blog/2119-add-reactions-to-pull-requests-issues-and-comments
