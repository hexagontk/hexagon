
Extra tasks:

* buildInfo : add configuration file with build variables to the package
* runService : Run the service in another thread. This allow the possibility to 'watch' source
  changes. To run the services and watch for changes you need to execute this task with the
  `--continuous` (`-t`) Gradle flag. Ie: `gw -t runService`
