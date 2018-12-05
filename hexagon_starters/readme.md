
# Lazybones Templates

Each subdirectory inside this module is a different [Lazybones] template. Right now, only
`hexagon-service` is taken, if you want to contribute more, you need to add them to `build.gradle`.

You can package and install the templates locally with the command:

    ./gradlew installAllTemplates

You'll then be able to use Lazybones to create new projects from these templates. To distribute
them, you will need to set up a Bintray account, populate the `repositoryUrl`, `repositoryUsername`
and `repositoryApiKey` settings in `build.gradle`, and finally publish the templates with:

    ./gradlew publishAllTemplates

You can find out more about creating templates on [the GitHub wiki].

[the GitHub wiki]: https://github.com/pledbrook/lazybones/wiki/Template-developers-guide
[Lazybones]: https://github.com/pledbrook/lazybones

## Starters Code

For convenience, the code used to generate the templates is in
`hexagon_benchmark/src/test/kotlin/starter`.
