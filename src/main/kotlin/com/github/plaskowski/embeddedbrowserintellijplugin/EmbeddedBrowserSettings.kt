package com.github.plaskowski.embeddedbrowserintellijplugin

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "EmbeddedBrowserSettings",
    storages = [Storage(StoragePathMacros.WORKSPACE_FILE)]
)
@Service(Service.Level.PROJECT)
class EmbeddedBrowserSettings : PersistentStateComponent<EmbeddedBrowserSettings> {

    var initialUrl: String = "https://youtube.com"

    override fun getState(): EmbeddedBrowserSettings = this

    override fun loadState(state: EmbeddedBrowserSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(project: Project): EmbeddedBrowserSettings =
            project.getService(EmbeddedBrowserSettings::class.java)
    }
}
