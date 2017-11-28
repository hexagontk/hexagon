
## Build Hexagon

Requires [Docker Compose installed](https://docs.docker.com/compose/install)

You can build the project, generate the documentation and install it in your local repository
typing:

    git clone https://github.com/hexagonkt/hexagon.git
    cd hexagon
    ./gradlew installDist -x test
    docker-compose up -d
    ./gradlew clean check site installDist jacocoReport installAllTemplates publishToMavenLocal tfb

The binaries are located in the `/build` directory. And the site in `/hexagon_site/build`.

## Local Setup

You can define some useful aliases like:

    alias gw='./gradlew'
    alias dcupd='docker-compose up -d'

It is recommended that you add: `gradlew clean check site installDist installAllTemplates tfb` to
your `.git/hooks/pre-push` script. As this command will be executed before pushing code to the
repository (saving time fixing [Travis] build errors).

If you want to commit to the project. It is convenient to setup your own [Travis] account to execute
the CI job defined in `.travis.yml` when code is pushed to your fork.

To use IntelliJ Idea you need to enable `Build, Execution, Deployment > Build Tools > Gradle >
Create separate module per source set` in order to compile JMH tests.

Inside Idea IDE, you need to review Kotlin's settings to make sure JVM 1.8 and API 1.1 is used
(`Project Structure > Modules > <Any Module> > Kotlin > Target Platform`).

## Benchmarking

The benchmark are the same run inside [TechEmpower Framework Benchmarks][TFB], to run them:

1. Start the benchmark's compose file. From the project's root execute:
   `docker-compose -f docker-compose.yaml -f hexagon_benchmark/docker-compose.yaml up -d`
2. Run [JMeter] with the `hexagon_benchmark/load_test.jmx` file.
3. Tune benchmark variables in the `Load Test Plan` tree node.
4. Run and check results in the `Load Test Plan > Profile > Summary Report` node.

[TFB]: https://www.techempower.com/benchmarks
[JMeter]: http://jmeter.apache.org

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
  
## Project Structure

modules
site generation
benchmark
starters
gradle

## Tasks and Milestones

Project's tasks and milestones are tracked in a [Github board]. You can use that board to check the
roadmap, vote the features you want (using [issue ractions]) or to pick tasks that you wish to 
contribute.

[Github board]: https://github.com/hexagonkt/hexagon/projects/1
[issue reactions]: https://github.com/blog/2119-add-reactions-to-pull-requests-issues-and-comments
