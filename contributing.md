
## Build Hexagon

Requires [Docker Compose installed](https://docs.docker.com/compose/install)

You can build the project, generate the documentation and install it in your local repository
typing:

    git clone https://github.com/hexagonkt/hexagon.git
    cd hexagon
    docker-compose up -d
    ./gradlew clean site installDist installAllTemplates publishLocal

The results are located in the `/build` directory. And the site in `/hexagon_site/build`.

## Local Setup

You can define some useful aliases like:

    alias gw='./gradlew'
    alias dcupd='docker-compose up -d'
    alias hx='cd ${hexagonHome}'
    alias hxall='hx && gw clean site installDist installAllTemplates'

It is recommended that you add: `gradlew clean site installDist installAllTemplates` to
your `.git/hooks/pre-push` script. As this command will be checked by [Travis] before the PRs.

If you want to commit to the project. It is convenient to setup your own [Travis] account to execute
the CI job defined in `.travis.yml` when code is pushed to your fork.

To use IntelliJ Idea you need to enable `Build, Execution, Deployment > Build Tools > Gradle >
Create separate module per source set` in order to compile JMH tests.

Inside Idea IDE, you need to review Kotlin's settings to make sure JVM 1.8 and API 1.1 is used
(`Project Structure > Modules > <Any Module> > Kotlin > Target Platform`).

## Tools Used

* [Travis]: For continuous integration.
* [Codecov]: To check code coverage.
* [Github]: Web hosting, project board and code hosting.
* [Bintray]: Artifact repository for JARs.

[Travis]: https://travis-ci.org
[Codecov]: https://codecov.io
[Github]: https://github.com
[Bintray]: https://bintray.com

## Contribute

* For code, file names, tags and branches use either camel case or snake case only. Ie: avoid `-` in
  file names if it is possible.

* For a Pull Request to be accepted:
  - It should be done to the `develop` branch.
  - The code has to pass all PR checks.
  - All public methods and field must be documented using [Dokka](https://github.com/Kotlin/dokka).

* Commit format: the preferred commit format would have:
  - Summary: small summary of the change. In imperative form.
  - Issue Id: it should be written in Github's format: `#taskNumber`. Optional.
  - Description: a more complete description of the issue. It is optional.

  ```
  Summary [#Id]

  [Description]
  ```

* Bug format: when filing bugs please use the given, when, then format. Ie:

  ```
  Given a condition
  And another condition
  When an action is taken
  And other after the first
  Then something happened
  ```

* New features should be discussed within an issue in the issue tracker before actual coding.

## Tasks and Milestones

Project's tasks and milestones are tracked in a [Github board]. You can use that board to check the
roadmap or pick tasks that you wish to contribute.

[Github board]: https://github.com/hexagonkt/hexagon/projects/1
