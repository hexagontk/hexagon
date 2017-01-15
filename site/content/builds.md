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
  * `service.gradle`: Gradle's script for a service or application.
    - Continuous run (AKA "Watch")
    - deployDir
    - serviceUser
    - serviceGroup
    - `systemdScript`: script that support start/stop/status
  * `site.gradle`: Adds support for site generation (with API documentation and reports).
    - siteSource
    - siteTarget
  
Templates:

  * curl -s "https://get.sdkman.io" | bash
  * source "$HOME/.sdkman/bin/sdkman-init.sh"
  * sdk i lazybones
  * lazybones config set bintrayRepositories "pledbrook/lazybones-templates" "jamming/maven"
  * lazybones create hexagon-service service
  

**TODO Complete documentation**
