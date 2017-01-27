title=Hexagon
date=2016-04-13
type=page
status=published
#~~~~~~


Service Lifecycle
=================

Create a service
----------------

To build Hexagon services you have some Gradle helpers that you can use on your own project. To
use them, you can use the online versions, or copy them to your `gradle` directory.

Directory structure: standard Gradle structure. Gradle wrapper changed

Configuration files: service.yaml (see configuration.md) and logback.xml

Templates: Pebble (optional dependencies)

Create from template
--------------------
  
Templates:

```bash
curl -s get.sdkman.io | bash && source ~/.sdkman/bin/sdkman-init.sh
sdk i lazybones
lazybones config set bintrayRepositories pledbrook/lazybones-templates jamming/maven
lazybones create hexagon-service service
gradle/wrapper
```

Running and Testing
-------------------

Docker compose

Packaging and Deployment
------------------------

Build scripts:

  * `kotlin.gradle`: Sets up Kotlin's Gradle plugin: Adds Kotlin libraries, setup coverage report,
    filter project resources with build variables. To use it you need to:

    - Define the `kotlinVersion` variable to `gradle.properties` file.
    - Add the following code to `build.gradle`
 
```Groovy
      buildscript {
          repositories {
              jcenter ()
          }
      
          dependencies {
              classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
          }
      }
```




  * `service.gradle`: Gradle's script for a service or application.

Tasks:
- runService: for continuous run (AKA "Watch")
- install: `systemdService`: script that support start/stop/status
- buildInfo / processResources

Variables:
- deployDir
- serviceUser
- serviceGroup

Use service:

#
# Copy this file to '/etc/systemd/system' and then:
#   - To start the service execute: sudo systemctl start ${projectName}
#   - To run the service at boot type: sudo systemctl enable ${projectName}
#


  * `site.gradle`: Adds support for site generation (with API documentation and reports).
    - siteSource
    - siteTarget

/*
 * To apply this script, you need to add the JBake plugin manually at the top of your build script
 * as that is not possible in included scripts like this one. These are the required lines to do so:
 *
 * plugins {
 *     id 'org.xbib.gradle.plugin.jbake' version '1.2.1'
 * }
 */
