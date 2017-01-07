
# Hexagon Service

This is an Hexagon template.


## Local setup


## Tests


## Infrastructure Software Versions

* [Hexagon 0.3.2](http://there4.co/hexagon)


## Run on Systemd

https://blog.openshift.com/run-gradle-builds-on-openshift/


## Run on Servlet engine

    rm -f db.txz
    
## Run in development

    toolset/run-tests.py --install server --mode verify --test hexagon
    


## Gradle wrapper setup

You can change Gradle version in `gradle/wrapper.properties`, but if you need to regenerate the
wrapper, follow the next steps:

1. Add this to `build.gradle`:

```groovy
    import static org.gradle.api.tasks.wrapper.Wrapper.DistributionType.*

    wrapper {
        String wrapperBaseFile = "$projectDir/gradle/wrapper"

        gradleVersion = '3.3'
        jarFile = wrapperBaseFile + ".jar"
        scriptFile = wrapperBaseFile
        distributionType = ALL
    }
```

2. Execute `gradle wrapper`

3. Remove the lines added in point 1 as they may cause problems in continuous integration
   environments

