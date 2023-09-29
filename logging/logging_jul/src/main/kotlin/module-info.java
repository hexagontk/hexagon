/**
 * This module holds utilities used in other libraries of the toolkit. Check the packages'
 * documentation for more details. You can find a quick recap of the main features in the sections
 * below.
 */
module com.hexagonkt.logging_jul {

    requires transitive java.logging;
    requires transitive com.hexagonkt.core;

    exports com.hexagonkt.logging.jul;
}
