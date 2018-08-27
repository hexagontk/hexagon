package com.hexagonkt.helpers

import javax.activation.MimetypesFileTypeMap

// TODO Move types to resources: application.types, audio.types... YAML and JSON are *REQUIRED HERE*
// TODO Add extension function to load resources/files
val mimeTypes = MimetypesFileTypeMap().also {
    it.addMimeTypes("""
        application/json json
        application/yaml yaml yml

        application/msword doc
        application/octet-stream bin dms lha lzh exe class
        application/pdf pdf
        application/postscript ai eps ps
        application/powerpoint ppt
        application/rtf rtf
        application/x-bcpio bcpio
        application/x-cdlink vcd
        application/x-compress Z
        application/x-cpio cpio
        application/x-csh csh
        application/x-dvi dvi
        application/x-gtar gtar
        application/x-gzip gz
        application/x-hdf hdf
        application/x-httpd-cgi cgi
        application/x-latex latex
        application/x-mif mif
        application/x-netcdf nc cdf
        application/x-sh sh
        application/x-shar shar
        application/x-tar tar
        application/x-tcl tcl
        application/x-tex tex
        application/x-texinfo texinfo texi
        application/x-troff t tr roff
        application/x-troff-man man
        application/x-troff-me me
        application/x-troff-ms ms
        application/zip zip

        audio/basic au snd
        audio/mpeg mpga mp2
        audio/x-aiff aif aiff aifc
        audio/x-pn-realaudio ram
        audio/x-pn-realaudio-plugin rpm
        audio/x-realaudio ra
        audio/x-wav wav

        image/gif gif
        image/jpeg jpeg jpg jpe
        image/png png
        image/tiff tiff tif
        image/x-cmu-raster ras
        image/x-portable-anymap pnm
        image/x-portable-bitmap pbm
        image/x-portable-graymap pgm
        image/x-portable-pixmap ppm
        image/x-rgb rgb
        image/x-xbitmap xbm
        image/x-xpixmap xpm
        image/x-xwindowdump xwd

        multipart/alternative
        multipart/appledouble
        multipart/digest
        multipart/mixed
        multipart/parallel

        text/html html htm
        text/plain txt
        text/richtext rtx
        text/tab-separated-values tsv
        text/x-setext etx
        text/x-sgml sgml sgm

        video/mpeg mpeg mpg mpe
        video/quicktime qt mov
        video/x-msvideo avi
        video/x-sgi-movie movie

        chemical/x-pdb pdb xyz
        x-world/x-vrml wrl vrml
    """.trim().trimIndent())
}

