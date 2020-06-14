
# Contributing

You can contribute code or documentation to the framework. This document will guide you through the
process or picking a task and building the code.

To know what issues are currently open and be aware of the next features yo can check the
[Project Board] at GitHub. Issues with the [help wanted] tag are recommended for a first time
contribution.

You can ask any question, make suggestions or complaints at the project's
[Slack channel][Slack]. You can also be up to date of project's news following [@hexagon_kt] on
Twitter.

[Project Board]: https://github.com/hexagonkt/hexagon/projects/1
[Slack]: https://kotlinlang.slack.com/messages/hexagon
[@hexagon_kt]: https://twitter.com/hexagon_kt
[help wanted]: https://github.com/hexagonkt/hexagon/issues?q=is%3Aissue+is%3Aopen+label%3A%22help+wanted%22

## Project Structure

The Hexagon project is composed of several modules. Most of the modules publish libraries for their
use by other projects (check the [Hexagon Structure] section of the readme file for more details).

Aside of that kind of modules, you can also find infrastructure modules: components used by the
project itself. These are internal modules not intended to be directly used by users (like the
[hexagon_starters] or the [hexagon_site]).

[Hexagon Structure]: https://github.com/hexagonkt/hexagon/blob/master/README.md#hexagon-structure
[hexagon_starters]: https://github.com/hexagonkt/hexagon/blob/master/hexagon_starters/README.md
[hexagon_site]: https://github.com/hexagonkt/hexagon/blob/master/hexagon_site/README.md

## Local Setup

Hexagon build process requires [Docker Compose installed](https://docs.docker.com/compose/install)

You can build the project, generate the documentation and install it in your local repository
typing:

```bash
git clone https://github.com/hexagonkt/hexagon.git
cd hexagon
./gradlew build buildSite publishToMavenLocal
```

The binaries are located in the `/build` directory of each module. The documentation site is in
`/hexagon_site/build`.

To work more comfortable, you can define some useful aliases like:

```bash
alias gw='./gradlew'
alias dcup='docker-compose up -d'
```

It is recommended that you create a Git pre-push script to check the code before pushing it. As
this command will be executed before pushing code to the repository (saving you time fixing
[GitHub Actions] build errors).

You can do so executing the `setUp` task by running: `./gradlew setUp`

IMPORTANT: For `rabbitmq` container to work properly, you should follow the
[Docker setup documentation] (inside the "With Docker" section)

If you want to generate the documentation site, check the [site module readme][hexagon_site]

[Docker setup documentation]: https://www.rabbitmq.com/install-debian.html

## Dependency verification

If you get a dependency verification error building the project after adding or changing a
dependency, you need to add the key fingerprint inside the `trusted-keys` element at the
`gradle/verification-metadata.xml` file.

Prior to trusting the key, you should verify it belongs to the person it claims to be on the
http://keys.gnupg.net key search tool.

For Continuous Integration runners, you need to import the keys inside the `gradle/verification
-keyring.gpg` file, you can do so with the following command:

```shell script
gpg --no-default-keyring --keyring ./gradle/verification-keyring.gpg --recv-keys $fingerprint
```

## Tools Used

* [Github]: Web hosting, project board, code hosting and continuous integration.
  * Secrets should be created inside the repository settings.
* [Bintray]: Artifact repository for JARs.
  * JCenter's synchronization must be done manually.

[GitHub Actions]: https://github.com/features/actions
[Github]: https://github.com
[Bintray]: https://bintray.com

## Contribute

* You can check available tasks in the [Project Board] and pick one of the issues with the
  `help wanted` tag available in the `Ready` column.

* Claim an issue you want to work in with a comment (after that I can assign it to you and move it
  to the `Working` column. If you want to contribute to a non tagged (or a non existing) tasks:
  write a comment, and we'll discuss the scope of the feature.

* New features should be discussed within an issue in the issue tracker before actual coding.

* For code, file names, tags and branches use either camel case or snake case only. Ie: avoid `-` or
  `.` in file names if it is possible.

* For a Pull Request to be accepted, follow the [pull request template] recommendations. Check the
  code follows the [Kotlin Coding Conventions], except final brace position in `else`, `catch` and
  `finally` (in its own line). If you use [IntelliJ] and [Editor Config] this will be checked for
  you.

* Commit format: the preferred commit format would have:
  - Summary: small summary of the change. In imperative form.
  - Issue ID: it should be written in Github's format: `#taskNumber`. Optional.
  - Description: a more complete description of the issue. It is optional.

  ```
  Summary [#Id]

  [Description]
  ```

* Bug format: when filing bugs please comply with the [bug template] requirements.

* A feature requests should follow the [enhancement template] rules.

[pull request template]: https://github.com/hexagonkt/hexagon/blob/master/.github/pull_request_template.md
[IntelliJ]: https://www.jetbrains.com/idea
[Editor Config]: https://editorconfig.org
[Kotlin Coding Conventions]: https://kotlinlang.org/docs/reference/coding-conventions.html
[bug template]: https://github.com/hexagonkt/hexagon/blob/master/.github/ISSUE_TEMPLATE/bug.md
[enhancement template]: https://github.com/hexagonkt/hexagon/blob/master/.github/ISSUE_TEMPLATE/enhancement.md

## Tasks and Milestones

Project's tasks and milestones are tracked in a [Github board][Project Board]. You can use that
board to check the roadmap, vote the features you want (using [issue reactions]) or to pick tasks
that you wish to contribute.

[issue reactions]: https://github.com/blog/2119-add-reactions-to-pull-requests-issues-and-comments
