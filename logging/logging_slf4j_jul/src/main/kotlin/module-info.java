/**
 * This module holds utilities used in other libraries of the toolkit. Check the packages'
 * documentation for more details. You can find a quick recap of the main features in the sections
 * below.
 */
module com.hexagonkt.logging_slf4j_jul {

    requires transitive kotlin.stdlib;
    requires transitive com.hexagonkt.logging_jul;
    requires transitive org.slf4j;

    exports com.hexagonkt.logging.slf4j.jul;
}
