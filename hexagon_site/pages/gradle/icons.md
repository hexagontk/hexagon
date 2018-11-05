
Icons
=====

Create web icons (favicons, and thumbnails for browsers/mobile) from image SVGs (logos).

For image rendering you will need [rsvg] (librsvg2-bin) and [imagemagick] installed in the
development machine.

To use it you should add `apply from: $gradleScripts/icons.gradle` to your `build.gradle`.

To setup this script's parameters, check the [build variables section]. This helper settings are:

* logoSmall
* logoLarge
* logoWide

[rsvg]: https://
[imagemagick]: https://
[build variables section]: /gradle/variables.html
