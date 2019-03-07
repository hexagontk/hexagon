
# Lazybones Templates

Each subdirectory starting with `hexagon-` inside this module is a different [Lazybones] template.

You can package and install the templates locally with the command:

    ./gradlew clean && ./gradlew installAllTemplates

IMPORTANT You can not do a `clean` before `installAllTemplates`, clean must be done in a previous
Gradle run by itself

You'll then be able to use Lazybones to create new projects from these templates. To distribute
them, you will need to set up a Bintray account, populate the `licenses`, `bintrayRepo`,
`bintrayUser` and `bintrayKey` settings in `gradle.properties`, and finally publish the templates
with:

    ./gradlew publishAllTemplates

You can find out more about creating templates on [the Lazybones GitHub wiki].

[the Lazybones GitHub wiki]: https://github.com/pledbrook/lazybones/wiki/Template-developers-guide
[Lazybones]: https://github.com/pledbrook/lazybones

## Starters Code

For convenience, the code used to generate the templates is in `src/`. It is tested before being
copied to the template.
