title=Contribute
date=2016-04-13
type=page
status=published
~~~~~~

## Build

Requires [Docker Compose installed](https://docs.docker.com/compose/install)

You can build the project, generate the documentation and install it in your local repository
typing:

    git clone https://github.com/jaguililla/hexagon.git
    cd hexagon
    docker-compose up -d
    docker exec hexagon_mongodb_1 mongo /benchmark.js
    ./gradle/wrapper clean site publishLocal libraries installAllTemplates

The results are located in the `/build` directory. And the site in `/build/site`.

## Local setup

You can define some useful aliases like:

    alias gw='gradle/wrapper'
    alias dcupd='docker-compose up -d'
    alias hx='cd ${hexagonHome}'
    alias hxall='hx && gw clean site install libraries installAllTemplates'

It is recommended that you add: `gradle/wrapper clean site install libraries installAllTemplates` to
your `.git/hooks/pre-push` script. As it will be checked by [Travis] before the PRs.

If you want to commit to the project. It is convenient to setup your own [Travis] account to execute
the CI job defined in `.travis.yml`.

## Tools used

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

## Bintray publish
 
To deploy on Bintray you need to set `bintrayUser` and `bintrayKey` in
`~/.gradle/gradle.properties`.

NOTE: For the package to be accepted in JCenter, you need to include sources (sourceJar task).

## Contribute

* For code and file names, use either camel case or snake case only. Ie: avoid `-` in file names if
  it is possible.

* For a Pull Request to be accepted:
  - It has to pass all PR checks.
  - All public and internal methods and field must be documented using Dokka

* Commit format: the preferred commit format would have:
  - Summary: small summary of the change. In imperative form.
  - Description: a more complete description of the issue. It is optional.
  - issue #id: task Id. Optional.

        Summary

        [Description]

        [issue #id]

* Bug format: when filing bugs please use the given, when, then format. Ie:

        Given a condition
        And another condition
        When an action is taken
        And other after the first
        Then something happened

* PRs should be done to master

Gradle wrapper setup
--------------------

You can change Gradle version in `gradle/wrapper.properties`, but if you need to regenerate the
wrapper, follow the next steps:

1. Add this to `build.gradle`:

```groovy
    wrapper {
        String wrapperBaseFile = "$projectDir/gradle/wrapper"

        gradleVersion = '3.3'
        jarFile = wrapperBaseFile + '.jar'
        scriptFile = wrapperBaseFile
        distributionType = org.gradle.api.tasks.wrapper.Wrapper.DistributionType.ALL
    }
```

2. Execute `gradle wrapper`

3. Remove the lines added in point 1 as they may cause problems in continuous integration
   environments

Lazybones template project
--------------------------

The Lazybones templates are located in the `templates` directory. Each subdirectory is a different
template.

You can package and install the templates locally with the command:

    ./gradlew installAllTemplates

You'll then be able to use Lazybones to create new projects from these templates. To distribute
them, you will need to set up a Bintray account, populate the `repositoryUrl`, `repositoryUsername`
and `repositoryApiKey` settings in `build.gradle`, and finally publish the templates with:

    ./gradlew publishAllTemplates

You can find out more about creating templates on [the GitHub wiki].

[the GitHub wiki]: https://github.com/pledbrook/lazybones/wiki/Template-developers-guide

Release process
---------------

Steps:

* Change version in gradle.properties
* Commit all changes
* Build and publish (binary and documentation)
* Tag changes
* Push changes and tag

Command:

    gw release && git push --tags
