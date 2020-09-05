
Contributing
============
You can contribute code or documentation to the toolkit. This document will guide you through the
process or picking a task and building the code.

You can ask any question, make suggestions or complaints at the project's
[Slack channel][Slack]. You can also be up to date of project's news following [@hexagon_kt] on
Twitter.

[Project Board]: https://github.com/hexagonkt/hexagon/projects/1
[Organization Board]: https://github.com/orgs/hexagonkt/projects/1
[Slack]: https://kotlinlang.slack.com/messages/hexagon
[@hexagon_kt]: https://twitter.com/hexagon_kt
[help wanted]: https://github.com/hexagonkt/hexagon/issues?q=is%3Aissue+is%3Aopen+label%3A%22help+wanted%22

Contribution Steps
------------------
1. You can check available tasks in the [Project Board] or the [Organization Board]. Issues with the
   [help wanted] tag in the `Ready` column are recommended for a first time contribution.
2. Claim an issue you want to work in with a comment (after that I can assign it to you and move it
   to the `Working` column. If you want to contribute to a non tagged (or a non existing) tasks:
   write a comment, and we'll discuss the scope of the feature.
3. New features should be discussed within an issue in the issue tracker before actual coding. You
   may do a PR directly, but you take the risk of it being not suitable and discarded.
4. For code, file names, tags and branches use either camel case or snake case only. I.e.: avoid `-`
   or `.` in file names if it is possible.
5. For a Pull Request to be accepted, follow the [pull request template] recommendations. Check the
   code follows the [Kotlin Coding Conventions], except final brace position in `else`, `catch` and
   `finally` (in its own line). If you use [IntelliJ] and [Editor Config] this will be checked for
   you.
6. Follow the commit rules defined at the [commit template].
7. Bug format: when filing bugs please comply with the [bug template] requirements.
8. A feature requests should follow the [enhancement template] rules.

[pull request template]: https://github.com/hexagonkt/hexagon/blob/master/.github/pull_request_template.md
[IntelliJ]: https://www.jetbrains.com/idea
[Editor Config]: https://editorconfig.org
[Kotlin Coding Conventions]: https://kotlinlang.org/docs/reference/coding-conventions.html
[commit template]: https://github.com/hexagonkt/hexagon/blob/master/.github/commit_template.txt
[bug template]: https://github.com/hexagonkt/hexagon/blob/master/.github/ISSUE_TEMPLATE/bug.md
[enhancement template]: https://github.com/hexagonkt/hexagon/blob/master/.github/ISSUE_TEMPLATE/enhancement.md

Project Structure
-----------------
The Hexagon project is composed of several modules. Most of the modules publish libraries for their
use by other projects (check the [Hexagon Structure] section of the readme file for more details).

Aside of that kind of modules, you can also find infrastructure modules: components used by the
project itself. These are internal modules not intended to be directly used by users (like the
[hexagon_starters] or the [hexagon_site]).

[Hexagon Structure]: https://github.com/hexagonkt/hexagon/blob/master/README.md#hexagon-structure
[hexagon_starters]: https://github.com/hexagonkt/hexagon/blob/master/hexagon_starters/README.md
[hexagon_site]: https://github.com/hexagonkt/hexagon/blob/master/hexagon_site/README.md

Local Setup
-----------
Hexagon build process requires [Docker Compose installed](https://docs.docker.com/compose/install)

You can check the required software, build the project, generate the documentation and install it in
your local repository typing:

```bash
git clone https://github.com/hexagonkt/hexagon.git
cd hexagon
./gradlew setUp build buildSite publishToMavenLocal
```

The binaries are located in the `/build` directory of each module. The documentation site is in
`/hexagon_site/build`.

To work more comfortable, you can define some useful aliases like:

```bash
alias gw='./gradlew'
alias dcup='docker-compose up -d'
```

Other useful Gradle commands (assuming `alias gw='./gradlew'`) are:

* Help: `gw help`
* Tasks: `gw tasks`
* Module Tasks: `gw [module:]tasks [--all]`
* Task details: `gw help --task <task>`
* Package: `gw clean assemble`
* Build: `gw build`
* Rebuild: `gw clean build`
* Documentation: `gw javadoc`
* Test: `gw test`
* Run: `gw ${MODULE}:run`

It is recommended that you create a Git pre-push script to check the code before pushing it. As
this command will be executed before pushing code to the repository (saving you time fixing
[GitHub Actions] build errors).

This can be done executing the `setUp` task by running: `./gradlew setUp`

IMPORTANT: For `rabbitmq` container to work properly, you should follow the
[Docker setup documentation] (inside the "With Docker" section)

If you want to generate the documentation site, check the [site module readme][hexagon_site]

[Docker setup documentation]: https://www.rabbitmq.com/install-debian.html
[GitHub Actions]: https://github.com/features/actions

Dependency verification
-----------------------
If you get a dependency verification error building the project after adding or changing a
dependency, you need to add the key fingerprint inside the `trusted-keys` element at the
`gradle/verification-metadata.xml` file.

Prior to trusting the key, you should verify it belongs to the person it claims to be on the
http://keys.gnupg.net key search tool. I.e.: you can check the Hexagon fingerprint
(`792B D37F F598 91C4 AC6F  8D92 3B26 711D 2AEE 3721`) issuing [this search] (don't forget to add
`0x` before the fingerprint without spaces).

For Continuous Integration runners, you need to import the keys inside the `gradle/verification
-keyring.gpg` file, you can do so with the following command:

```shell script
gpg --no-default-keyring --keyring ./gradle/verification-keyring.gpg --recv-keys $fingerprint
```

[this search]: http://keys.gnupg.net/pks/lookup?search=0x792BD37FF59891C4AC6F8D923B26711D2AEE3721

Major Release Checklist
-----------------------
1. Release site's dependent projects (`store`, `messaging`)
2. Publish their packages using the [Nexus Repository Manager]
3. Merge Hexagon main project to `master` in GitHub
4. Check the site deployment is OK ([https://hexagonkt.com])
5. Publish Hexagon modules using the [Nexus Repository Manager]
6. Update starter repositories (Gradle and Maven ones)
7. Update TFB benchmark
8. Update example projects inside the organization
9. Create a changelog to announce the release
10. Publish changelog on:
 * Dev.to
 * Kotlin Slack
 * Reddit
 * Twitter
 * Kotlin Weekly Newsletter
 * LinkedIn

[Nexus Repository Manager]: https://oss.sonatype.org

### Changelog commands

Commit messages can be filtered by types (check [commit_template.txt](/.github/commit_template.txt))
for details.

```shell script
git log 1.2.0...1.3.0 \
  --date=iso8601 \
  --reverse \
  --pretty=format:'* %ad %ar <%an %ae> [View](http://github.com/hexagonkt/hexagon/commit/%H) · %s' \
  >>CHANGELOG.md

git log 1.2.0...1.3.0 --date=iso8601 --reverse --pretty=format:'%an %ae'|sort|uniq >>CHANGELOG.md
```
