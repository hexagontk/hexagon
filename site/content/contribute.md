title=Contribute
date=2016-04-13
type=page
status=published
~~~~~~


Release process
===============

Steps:

* Commit and push all changes
* Change version in gradle.properties
* Build and deploy (binary and documentation)
* Commit and tag the release
* Update version in gradle.properties
* Commit and push
* Confirm publishing of artifacts within Bintray (this is a manual step)

Used aliases:

    alias gw='gradle/wrapper'
    alias gwnd='gw --no-daemon'

Command:

    git flow release start -F '$version' && \
    sed -i "s/^version=.*$/version=$version/" gradle.properties && \
    gwnd release && \
    git flow release finish -Fp '$version'

git-extras + git-flow + hub + huboard + bintray

Contribute
==========

* The code should be formatted accordingly to the `codeStyleSettings.xml` file.
For code and file names, etc. Use either camel case or snake case (if possible)
avoid `-` in file names, etc.

* For a Pull Request to be accepted:
  * It has to pass all existing tests.
  * The coverage of the new code should be at least 70%
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


* PRs to master
