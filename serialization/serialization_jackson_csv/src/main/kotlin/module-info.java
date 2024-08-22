
module com.hexagontk.serialization_jackson_csv {

    requires transitive com.hexagontk.core;
    requires transitive com.hexagontk.serialization_jackson;
    requires transitive com.fasterxml.jackson.dataformat.csv;

    exports com.hexagontk.serialization.jackson.csv;
}
