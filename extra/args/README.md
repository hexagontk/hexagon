
# Module args
TODO .

## Command processing
1. Read system.in (check if there is input piped from other command)
2. Parse command line tokens into options and positional parameters
3. Check command line definition for invalid options, etc.
4. Apply defaults: option defaults, defaults in configuration files -user dir, and current dir-
5. Check missing options: fail if no interactive, ask for input if interactive
6. Return result
7. Support - and --
8. Take care of environment variables and .env files in current dir
9. Support interactive settings prompt if mandatory options are missing
10. Support @files for long list of parameters

Program, can have stream input, config files (XDG dirs), and parameters

Check:
* https://clig.dev/#the-basics
* https://medium.com/@jdxcode/12-factor-cli-apps-dd3c227a0e46
* https://github.com/TeXitoi/structopt
* https://github.com/spf13/cobra
* https://ajalt.github.io/clikt

## Install the Dependency

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    implementation("com.hexagontk:args:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagontk</groupId>
      <artifactId>args</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

## TODO
* Know when it is a TTY or not
* Handle stdin (pipes)
* Load properties from (`./.program.properties`, `~/.config/program.properties`)
* Allow missing options to be searched in System Settings (See Jvm.systemSetting)
* Load command line options from files with `@file` parameters
* Add color to default formatters, and honor NO_COLOR environment variable
* Handle standard options like --help, help command -q --quiet, -v verbose, etc.
* Control rendering with flags on `ArgsManager`
* Check documentation rendering of `http` (httpie) and `sdk` (sdkman)
* Allow interactive commands and options when something is missing
* Add command aliases (convert name to a list)
* Set default command on program definition (as a list or names to resolve the command)
* Add a parameter to fetch parameters from System Properties on missing parameters
* Loading of options from files (see below) must be done outside cli processing
* '--' stops parsing options and threat everything from that point as positional parameters
* Tags can be used to define groups (i.e.: of mutually exclusive options)
* Add files and environment variables documentation on the program arguments
* Add examples to program documentation
* When an option can pick the value from an environment variable, manually load that env into System
  properties and let the program pick values from there (see above)

Allows to load a parameter from command line, environment variable or a set of files with a
priority:

1. command line
2. environment variable
3. java property (only for testing)
4. $HOME/.config/app/*.{json,yaml}
5. $HOME/.config/app/*.{json,yaml}

> Discoverable CLIs have comprehensive help texts, provide lots of examples, suggest what command to
> run next, suggest what to do when there is an error. There are lots of ideas that can be stolen
> from GUIs to make CLIs easier to learn and use, even for power users.
>
> the CLI has embodied an accidental metaphor all along: it’s a conversation.
>
>

# Package com.hexagontk.args
TODO .
