package com.hexagonkt.settings

import com.hexagonkt.serialization.parse
import java.io.File
import java.io.FileNotFoundException

class FileSource(val file: File) : SettingsSource {

    constructor(file: String) : this(File(file))

    override fun toString(): String = "File with path: ${file.absolutePath}"

    override fun load(): Map<String, *> =
        try {
            file.parse<Map<*, *>>().mapKeys { e -> e.key.toString() }
        }
        catch (e: FileNotFoundException) {
            emptyMap<String, Any>()
        }
}
