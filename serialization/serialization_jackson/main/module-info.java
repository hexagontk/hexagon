
module com.hexagontk.serialization_jackson {

    requires transitive kotlin.stdlib;
    requires transitive tools.jackson.databind;
    requires transitive com.hexagontk.serialization;

    exports com.hexagontk.serialization.jackson;
}
