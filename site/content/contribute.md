title=Contribute
date=2016-04-13
type=page
status=published
~~~~~~


Release process
===============

Steps:

* Change version in gradle.properties
* Commit and push all changes
* Build and deploy (binary and documentation)
* Push changes

Used aliases:

    alias gw='gradle/wrapper'

Command:

    gw release && git push

Tools used: git-extras + bintray


Build
=====

Requires [Docker Compose installed](https://docs.docker.com/compose/install)

You can build the project, generate the documentation and install it in your local repository
typing:

    git clone https://github.com/jaguililla/hexagon.git
    cd hexagon
    docker-compose up -d
    docker exec hexagon_mongodb_1 mongo /benchmark.js
    ./gradle/wrapper clean site publishLocal

The results are located in the `/build` directory. And the site in `/build/site`.

For more details about Hexagon's development. Read the [contribute] section.

Code coverage grid:

![coverage](https://codecov.io/gh/jaguililla/hexagon/branch/master/graphs/tree.svg)

Contribute
==========

* gw clean site install libraries installAllTemplates should work ok. It is useful to add this
  command to the Git push hook.
  
* Check `systems/aliases` for utility aliases.

* The code should be formatted accordingly to the `codeStyleSettings.xml` file.
  For code and file names, etc. Use either camel case or snake case (if possible)
  avoid `-` in file names, etc.

* For a Pull Request to be accepted:
  * It has to pass all existing tests.
  * The coverage of the new code should be at least 90%
  * All public and protected methods and field must be documented using Dokka

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
        jarFile = wrapperBaseFile + ".jar"
        scriptFile = wrapperBaseFile
        distributionType = org.gradle.api.tasks.wrapper.Wrapper.DistributionType.ALL
    }
```

2. Execute `gradle wrapper`

3. Remove the lines added in point 1 as they may cause problems in continuous integration
   environments

Lazybones template project
--------------------------

You have just created a simple project for managing your own Lazybones project
templates. You get a build file (`build.gradle`) and a directory for putting
your templates in (`templates`).

To get started, simply create new directories under the `templates` directory
and put the source of the different project templates into them. You can then
package and install the templates locally with the command:

    ./gradlew installAllTemplates

You'll then be able to use Lazybones to create new projects from these templates.
If you then want to distribute them, you will need to set up a Bintray account,
populate the `repositoryUrl`, `repositoryUsername` and `repositoryApiKey` settings
in `build.gradle`, add new Bintray packages in the repository via the Bintray
UI, and finally publish the templates with

    ./gradlew publishAllTemplates

You can find out more about creating templates on [the GitHub wiki][1].

[1]: https://github.com/pledbrook/lazybones/wiki/Template-developers-guide
