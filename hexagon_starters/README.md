
# Application Templates

Each subdirectory starting with `hexagon-` inside this module is a different template.

You can package and install the templates locally with the command:

    # TODO Addapt to Maven Archetypes
    ./gradlew clean && ./gradlew installAllTemplates

You'll then be able to use Maven to create new projects from these templates. To distribute
them, you will need to set up a Bintray account, populate the `licenses`, `bintrayRepo`,
`bintrayUser` and `bintrayKey` settings in `gradle.properties`, and finally publish the templates
with:

    ./gradlew bintrayUpload

## Starters Code

For convenience, the code used to generate the templates is in `src/`. It is tested before being
copied to the template.
