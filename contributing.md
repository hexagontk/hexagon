
# Contributing
You can contribute code or documentation to the toolkit. This document will guide you through the
process or picking a task and building the code.

## Make a Question
You can use the repository's [Discussions tab][discussion] to ask questions or resolve problems.

You can also ask any question, make suggestions or complaints at the project's
[Slack channel][Slack]. You can also be up-to-date of project's news following [@hexagontk] on
X (Twitter).

[discussion]: https://github.com/hexagonkt/hexagon/discussions
[Slack]: https://kotlinlang.slack.com/messages/hexagon
[@hexagontk]: https://twitter.com/hexagontk

## Report a Bug
To file a bug, create an issue with the [bug template].

[bug template]: https://github.com/hexagonkt/hexagon/issues/new?template=bug.md

## Request a Feature
Create a new issue using the [enhancement template] to file an improvement.

[enhancement template]: https://github.com/hexagonkt/hexagon/issues/new?template=enhancement.md

## Contribution Steps
1. You can check available tasks in the [Organization Board]. Issues with the [help wanted] tag in
   the `Ready` column are recommended for a first time contribution.
2. Claim an issue you want to work in with a comment, after that I can assign it to you and move it
   to the `Working` column. If you want to contribute to a non tagged (or a not existing) tasks:
   write a comment, and we'll discuss the scope of the feature.
3. New features should be discussed within a post in the [GitHub ideas discussions][ideas]
   before actual coding. You may do a PR directly, but you take the risk of it being not suitable
   and discarded.
4. For code, file names, tags and branches use either camel case or snake case only. I.e.: avoid `-`
   or `.` in file names if it is possible.
5. For a Pull Request to be accepted, follow the [pull request template] recommendations. Check the
   code follows the [Kotlin Coding Conventions], except final brace position in `else`, `catch` and
   `finally` (in its own line). If you use [IntelliJ] and [Editor Config] this will be checked for
   you.
6. Packages must have the same folder structure as in Java (to avoid problems with tools and Java
   module definition).
7. Follow the commit rules defined at the [commit guidelines].

[Organization Board]: https://github.com/orgs/hexagonkt/projects/2
[help wanted]: https://github.com/hexagonkt/hexagon/issues?q=is%3Aissue+is%3Aopen+label%3A%22help+wanted%22
[pull request template]: https://github.com/hexagonkt/.github/blob/master/pull_request_template.md
[IntelliJ]: https://www.jetbrains.com/idea
[Editor Config]: https://editorconfig.org
[Kotlin Coding Conventions]: https://kotlinlang.org/docs/reference/coding-conventions.html
[commit guidelines]: https://github.com/hexagonkt/.github/blob/master/commits.md
[ideas]: https://github.com/hexagonkt/hexagon/discussions/categories/ideas

## Project Structure
The Hexagon project is composed of several modules. Most of the modules publish libraries for their
use by other projects (check the [Hexagon Structure] section of the readme file for more details).

Aside of that kind of modules, you can also find infrastructure modules: components used by the
project itself. These are internal modules not intended to be directly used by users (like the
[starters] or the [site]).

[Hexagon Structure]: https://github.com/hexagonkt/hexagon/blob/master/README.md#hexagon-structure
[starters]: https://github.com/hexagonkt/hexagon/blob/master/starters/README.md
[site]: https://github.com/hexagonkt/hexagon/blob/master/site/README.md

## Local Setup
Hexagon build process requires a JDK 21+ to compile. You can check the required software, build the
project, generate the documentation and install it in your local repository typing:

```bash
git clone https://github.com/hexagonkt/hexagon.git
cd hexagon
./gradlew setUp clean build
./gradlew buildSite publishToMavenLocal
```

The binaries are located in the `/build` directory of each module. The documentation site is in
`/site/build`.

Other useful Gradle commands are:

* Help: `./gradlew help`
* Tasks: `./gradlew tasks`
* Module Tasks: `./gradlew [module:]tasks [--all]`
* Task details: `./gradlew help --task <task>`
* Package: `./gradlew clean assemble`
* Build: `./gradlew build`
* Rebuild: `./gradlew clean build`
* Documentation: `./gradlew javadoc`
* Code Analysis: `./gradlew detekt`
* Test: `./gradlew test`
* Run: `./gradlew ${MODULE}:run`
* Profile Build: `./gradlew ${TASK} --profile`
* Project Dependencies: `./gradlew dependencyReport` or `./gradlew htmlDependencyReport`
* Project Tasks: `./gradlew taskReport`

It is recommended that you create a Git pre-push script to check the code before pushing it. As
this command will be executed before pushing code to the repository (saving you time fixing
[GitHub Actions] build errors).

This can be done executing the `setUp` task by running: `./gradlew setUp`

If you want to generate the documentation site, check the Hexagon's site module readme.

[GitHub Actions]: https://github.com/features/actions

## Major Release Checklist
1. Release site's dependent projects (`hexagon_extra`)
2. Publish their packages using the [Nexus Repository Manager]
3. Merge Hexagon main project to `master` in GitHub
4. Check the site deployment is OK ([https://hexagonkt.com])
5. Publish Hexagon modules using the [Nexus Repository Manager]
6. Create a GitHub release
7. Update starter repositories (Gradle and Maven ones)
8. Update TFB benchmark
9. Update example projects inside the organization
10. Create a changelog to announce the release
11. Publish changelog on:
  * Dev.to
  * Kotlin Slack
  * Reddit Kotlin
  * X (Twitter)
  * Kotlin Weekly Newsletter
  * LinkedIn
  * Mailing lists (Awesome Kotlin, Kotlin Weekly)

[Nexus Repository Manager]: https://oss.sonatype.org

### Changelog commands
Commit messages can be filtered by types (check the [commit guidelines]) for details.

```bash
git log 1.2.0...1.3.0 \
  --date=iso8601 \
  --reverse \
  --pretty=format:'* %ad %ar <%an %ae> [View](http://github.com/hexagonkt/hexagon/commit/%H) · %s' \
  >>CHANGELOG.md

git log 1.2.0...1.3.0 --date=iso8601 --reverse --pretty=format:'%an %ae'|sort|uniq >>CHANGELOG.md
```

## Documentation Guidelines
ONLY public members require documentation.

Some hints to write the comments are:
* Use the imperative form.
* Capitalize descriptions (parameters, receivers, return types, etc.) and end sentences with a dot.
* Complete all KDoc tags (I.e.: `@param`, `@receiver`, etc. for the methods that have them.
* Focus on what they do, not how, neither what it is.
* Not saying *method* or *property* (that is clear from the context).
* Comments to ignore warnings (false positives) should include an explanation.
* Explain corner cases, default values and allowed values/formats if it applies.
* These are recommendations, feel free to make an exception if you think it is required to explain
  the use/structure of the code better.

## Logging Guidelines
Take care of the level assigned to the log statements:
* `error` some error stopped the correct processing of the request or the process.
* `warn` something failed and was ignored (it wasn't a big deal to stop request or process), but it
  could be an issue later or with other data.
* `info` only for really useful information that is not written very often.
* `debug` for information with useful information to diagnose problems or failures (that could be
  used to diagnose client code bugs).
* `trace` for low level details that are logged very often (information that could be used to fix
  this library's bugs).

Rules of thumb:
* Prefer to group related information in a single log statement rather than using many of them.
* Do not log re-thrown exceptions. If this is done, chances are that this exception is logged twice
  (making diagnosis harder). All not handled exceptions are logged at entry points (main or request
  handlers), there is no need to log them in every catch, or where they are thrown.
* Catching an exception doesn't mean it is an error or warning. Assign categories based on the rules
  above.
* Generally is a good idea to log the places where the program makes a decision (adding the
  information that lead to the program flow selection). I.e.: "User <id> not deleted (not found in
  the data store)".
* If some condition leads to default return values, it is a good place to put a logging statement to
  add more information about it. I.e.: HTTP Request timeout (<relevant call information>), returning
  empty array.
