package com.hexagonkt.core.media

/**
 * Prebuilt application media types. Only validated once at start time (not constructed each time).
 */
enum class ApplicationMedia(
    override val group: MediaTypeGroup,
    override val type: String,
    override val fullType: String = "${group.text}/$type"
) : MediaType {

    AVRO(MediaTypeGroup.APPLICATION, "avro"),
    CBOR(MediaTypeGroup.APPLICATION, "cbor"),
    JSON(MediaTypeGroup.APPLICATION, "json"),
    YAML(MediaTypeGroup.APPLICATION, "yaml"),
    XML(MediaTypeGroup.APPLICATION, "xml"),
    GZIP(MediaTypeGroup.APPLICATION, "gzip"),
    COMPRESS(MediaTypeGroup.APPLICATION, "compress"),
    OCTET_STREAM(MediaTypeGroup.APPLICATION, "octet-stream"),
    PDF(MediaTypeGroup.APPLICATION, "pdf"),
    POSTSCRIPT(MediaTypeGroup.APPLICATION, "postscript"),
    RTF(MediaTypeGroup.APPLICATION, "rtf"),
    X_CSH(MediaTypeGroup.APPLICATION, "x-csh"),
    X_GTAR(MediaTypeGroup.APPLICATION, "x-gtar"),
    X_LATEX(MediaTypeGroup.APPLICATION, "x-latex"),
    X_SH(MediaTypeGroup.APPLICATION, "x-sh"),
    X_TAR(MediaTypeGroup.APPLICATION, "x-tar"),
    X_TCL(MediaTypeGroup.APPLICATION, "x-tcl"),
    X_TEX(MediaTypeGroup.APPLICATION, "x-tex"),
    X_TEXINFO(MediaTypeGroup.APPLICATION, "x-texinfo"),
    ZIP(MediaTypeGroup.APPLICATION, "zip"),
    EPUB_ZIP(MediaTypeGroup.APPLICATION, "epub+zip"),
    JAVA_ARCHIVE(MediaTypeGroup.APPLICATION, "java-archive"),
    OGG(MediaTypeGroup.APPLICATION, "ogg"),
    RAR(MediaTypeGroup.APPLICATION, "vnd.rar"),
    XHTML(MediaTypeGroup.APPLICATION, "xhtml+xml"),
    WEB_MANIFEST(MediaTypeGroup.APPLICATION, "manifest+json"),
    TOML(MediaTypeGroup.APPLICATION, "toml"),

    A7Z(MediaTypeGroup.APPLICATION, "x-7z-compressed"),
    BZIP(MediaTypeGroup.APPLICATION, "x-bzip"),
    BZIP2(MediaTypeGroup.APPLICATION, "x-bzip2"),
    CDF(MediaTypeGroup.APPLICATION, "x-cdf"),
    PHP(MediaTypeGroup.APPLICATION, "x-httpd-php"),
}
