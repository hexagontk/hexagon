/**
 * This module holds helpers useful in other applications, but not used inside the toolkit. Check
 * the packages' documentation for more details.
 */
module com.hexagontk.helpers {

    requires transitive kotlin.stdlib;
    requires transitive com.hexagontk.core;

    exports com.hexagontk.helpers;
    exports com.hexagontk.helpers.text;
}
