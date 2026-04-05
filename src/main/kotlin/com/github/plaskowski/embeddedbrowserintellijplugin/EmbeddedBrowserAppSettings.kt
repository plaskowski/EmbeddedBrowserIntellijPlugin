package com.github.plaskowski.embeddedbrowserintellijplugin

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "EmbeddedBrowserAppSettings",
    storages = [Storage("embeddedBrowser.xml")]
)
@Service(Service.Level.APP)
class EmbeddedBrowserAppSettings : PersistentStateComponent<EmbeddedBrowserAppSettings> {

    var seedProfilePath: String? = null
    var userAgent: String? = null

    override fun getState(): EmbeddedBrowserAppSettings = this

    override fun loadState(state: EmbeddedBrowserAppSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): EmbeddedBrowserAppSettings = service()

        /** Runtime JCEF profile directory — always under IDE system path, managed by the plugin. */
        fun resolveCachePath(): String = PathManager.getSystemPath() + "/jcef_profile"
    }
}
