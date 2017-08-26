
## Build

Requires [Docker Compose installed](https://docs.docker.com/compose/install)

You can build the project, generate the documentation and install it in your local repository
typing:

    git clone https://github.com/hexagonkt/hexagon.git
    cd hexagon
    docker-compose up -d
    ./gradlew clean site publishLocal installAllTemplates

The results are located in the `/build` directory. And the site in `/hexagon_site/build`.

## Local Setup

You can define some useful aliases like:

    alias gw='./gradlew'
    alias dcupd='docker-compose up -d'
    alias hx='cd ${hexagonHome}'
    alias hxall='hx && gw clean site installDist installAllTemplates'

It is recommended that you add: `gradlew clean site installDist installAllTemplates` to
your `.git/hooks/pre-push` script. As it will be checked by [Travis] before the PRs.

If you want to commit to the project. It is convenient to setup your own [Travis] account to execute
the CI job defined in `.travis.yml`.

To use IntelliJ Idea you need to enable `Build, Execution, Deployment > Build Tools > Gradle >
Create separate module per source set` in order to compile JMH tests.

Inside Idea IDE, you need to review Kotlin's settings to make sure JVM 1.8 and API 1.1 is used
(`Project Structure > Modules > <Any Module> > Kotlin > Target Platform`).

## Tools Used

* [Travis]: For continuous integration.
* [Codecov]: To check code coverage.
* [Github]: Web hosting, project board and code hosting.
* [Bintray]: Artifact repository for JARs.
* [Git Extras]: Git goodies.

[Travis]: https://travis-ci.org
[Codecov]: https://codecov.io
[Github]: https://github.com
[Bintray]: https://bintray.com
[Git Extras]: https://github.com/tj/git-extras

## Bintray Publish
 
To deploy on Bintray you need to set `bintrayUser` and `bintrayKey` in `local.gradle`. Ie:

```groovy
ext.bintrayUser = 'jaguililla'
ext.bintrayKey = 'cafebabe'
```

NOTE: For the package to be accepted in JCenter, you need to include sources (sourceJar task).

For continuous deployment, keys are encrypted using [Travis] file [encryption functionality].

[encryption functionality]: https://docs.travis-ci.com/user/encrypting-files

## Contribute

* For code, file names, tags and branches use either camel case or snake case only. Ie: avoid `-` in
  file names if it is possible.

* For a Pull Request to be accepted:
  - It has to pass all PR checks.
  - All public and internal methods and field must be documented using
    [Dokka](https://github.com/Kotlin/dokka).

* Commit format: the preferred commit format would have:
  - Summary: small summary of the change. In imperative form.
  - Description: a more complete description of the issue. It is optional.
  - Issue Id: it should be written in Github's format: `#taskNumber`. Optional.

  ```
  Summary

  [Description]

  [#Id]
  ```

* Bug format: when filing bugs please use the given, when, then format. Ie:

  ```
  Given a condition
  And another condition
  When an action is taken
  And other after the first
  Then something happened
  ```

* New features should be discussed as issues in the issue tracker before actual coding

* PRs should be done to `develop`

## Tasks and Milestones

Project's tasks and milestones are tracked in a [Github board]. You can use that board to check the
roadmap or pick tasks that you wish to contribute.

[Github board]: https://github.com/hexagonkt/hexagon/projects/1

## Lazybones Templates

The [Lazybones] templates are located in the `hexagon_starters` module. Each subdirectory is a
different template.

You can package and install the templates locally with the command:

    ./gradlew installAllTemplates

You'll then be able to use Lazybones to create new projects from these templates. To distribute
them, you will need to set up a Bintray account, populate the `repositoryUrl`, `repositoryUsername`
and `repositoryApiKey` settings in `build.gradle`, and finally publish the templates with:

    ./gradlew publishAllTemplates

You can find out more about creating templates on [the GitHub wiki].

[the GitHub wiki]: https://github.com/pledbrook/lazybones/wiki/Template-developers-guide
[Lazybones]: https://github.com/pledbrook/lazybones

## Release Process

Steps:

* Change version in gradle.properties
* Commit all changes
* Build and publish (binary and documentation)
* Tag changes
* Push changes and tag

Command:

    gw release && git push --tags
