package com.hexagonkt.server.undertow

import io.undertow.server.handlers.resource.*
import java.io.File

class FallbackResourceManager(filesPath: String?, resourcesPath: String?) : ResourceManager {
    val fileManager =
        if (filesPath != null) FileResourceManager (File (filesPath), 0L)
        else null

    val resourcesManager =
        if (resourcesPath != null)
            ClassPathResourceManager (ClassLoader.getSystemClassLoader (), resourcesPath)
        else
            null

    override fun getResource(path: String?): Resource? =
        fileManager?.getResource(path) ?: resourcesManager?.getResource(path)

    override fun isResourceChangeListenerSupported(): Boolean =
        fileManager?.isResourceChangeListenerSupported ?: true
            && resourcesManager?.isResourceChangeListenerSupported ?: true

    override fun registerResourceChangeListener(listener: ResourceChangeListener?) {
        fileManager?.registerResourceChangeListener (listener)
        resourcesManager?.registerResourceChangeListener (listener)
    }

    override fun removeResourceChangeListener(listener: ResourceChangeListener?) {
        fileManager?.removeResourceChangeListener (listener)
        resourcesManager?.removeResourceChangeListener (listener)
    }

    override fun close() {
        fileManager?.close ()
        resourcesManager?.close ()
    }
}
