package com.github.plaskowski.embeddedbrowserintellijplugin.ui

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.jetbrains.cef.JCefAppConfig

private const val CACHE_PATH_KEY = "com.github.plaskowski.embeddedbrowserintellijplugin.cache_path"
private const val INITIAL_URL_KEY = "com.github.plaskowski.embeddedbrowserintellijplugin.initial_url"

// I followed FavoritesViewToolWindowFactory as example
class EmbeddedBrowserToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val propertiesComponent = PropertiesComponent.getInstance(project)

        val cachePath = propertiesComponent.getValue(CACHE_PATH_KEY)
        if (cachePath != null) {
            // This is an ugly way to implement this. Won't work if the JCefApp is already initialized.
            JCefAppConfig.getInstance().cefSettings.cache_path = cachePath
        }
        val initialUrl = propertiesComponent.getValue(INITIAL_URL_KEY, "https://youtube.com")

        setupToolWindowContent(toolWindow, initialUrl)
    }

    private fun setupToolWindowContent(toolWindow: ToolWindow, initialUrl: String) {
        val contentManager = toolWindow.contentManager
        val rootComponent = EmbeddedBrowserMainPanel(initialUrl)
        val content = contentManager.factory.createContent(rootComponent, null, false)
        contentManager.addContent(content)
    }
}
