title=Hexagon
date=2016-04-13
type=page
status=published
~~~~~~


Builds
======

Build scripts:

  * `kotlin.gradle`: Sets up Kotlin's Gradle plugin.
    - kotlinVersion:
 
 * Setup Kotlin for a Gradle project.
 *
 * This scripts:
 *
 * - Adds Kotlin libraries
 * - Setup code coverage report
 * - Filter project resources with Gradle build variables
 * - Apply better defaults for running tests and cleaning the project
 *
 * You need to add the `kotlinVersion` variable to `gradle.properties` to use this script.
 *
 * buildscript {
 *     repositories {
 *         jcenter ()
 *     }
 *
 *     dependencies {
 *         classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
 *     }
 * }




  * `service.gradle`: Gradle's script for a service or application.
    - Continuous run (AKA "Watch")
    - deployDir
    - serviceUser
    - serviceGroup
    - `systemdScript`: script that support start/stop/status

 * Tasks:
 *
 * - runService
 * - install
 * - buildInfo / processResources
 *
 * Variables:
 *
 * - deployDir
 * - serviceUser
 * - serviceGroup


#
# Copy this file to '/etc/systemd/system' and run:
# sudo systemctl start ${projectName}
# sudo systemctl enable ${projectName}
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
  
Templates:

  * curl -s "https://get.sdkman.io" | bash
  * source "$HOME/.sdkman/bin/sdkman-init.sh"
  * sdk i lazybones
  * lazybones config set bintrayRepositories "pledbrook/lazybones-templates" "jamming/maven"
  * lazybones create hexagon-service service
  

**TODO Complete documentation**
