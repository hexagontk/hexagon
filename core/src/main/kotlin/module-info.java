/**
 * This module holds utilities used in other libraries of the toolkit. Check the packages'
 * documentation for more details. You can find a quick recap of the main features in the sections
 * below.
 */
module com.hexagontk.core {

    requires transitive kotlin.stdlib;

    exports com.hexagontk.core;
    exports com.hexagontk.core.logging;
    exports com.hexagontk.core.media;
    exports com.hexagontk.core.security;
    exports com.hexagontk.core.text;

    provides java.net.spi.URLStreamHandlerProvider with com.hexagontk.core.ClasspathHandlerProvider;
}
