
module com.hexagontk.serialization_jackson_xml {

    requires transitive com.hexagontk.core;
    requires transitive com.hexagontk.serialization_jackson;
    requires transitive tools.jackson.dataformat.xml;

    exports com.hexagontk.serialization.jackson.xml;
}
