package com.github.plaskowski.embeddedbrowserintellijplugin.ui

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.jetbrains.cef.JCefAppConfig

private const val CACHE_PATH_KEY = "com.github.plaskowski.embeddedbrowserintellijplugin.cache_path"
private const val INITIAL_URL_KEY = "com.github.plaskowski.embeddedbrowserintellijplugin.initial_url"
private const val USER_AGENT_KEY = "com.github.plaskowski.embeddedbrowserintellijplugin.user_agent"

private const val DEFAULT_INITIAL_URL = "https://youtube.com"

// I followed FavoritesViewToolWindowFactory as example
class EmbeddedBrowserToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val propertiesComponent = PropertiesComponent.getInstance(project)
        configureCef(propertiesComponent)
        setupToolWindowContent(toolWindow, propertiesComponent)
    }

    private fun configureCef(propertiesComponent: PropertiesComponent) {
        // This is an ugly way to implement this as this is global configuration so other CEF usages will be affected.
        // It won't work if the JCefApp is already initialized.
        val cefSettings = JCefAppConfig.getInstance().cefSettings
        val cachePath = propertiesComponent.getValue(CACHE_PATH_KEY)
        if (cachePath != null) {
            cefSettings.cache_path = cachePath
        }
        val userAgent = propertiesComponent.getValue(USER_AGENT_KEY)
        if (userAgent != null) {
            cefSettings.user_agent = userAgent
        }
    }

    private fun setupToolWindowContent(toolWindow: ToolWindow, propertiesComponent: PropertiesComponent) {
        val initialUrl = propertiesComponent.getValue(INITIAL_URL_KEY, DEFAULT_INITIAL_URL)
        val contentManager = toolWindow.contentManager
        val rootComponent = EmbeddedBrowserMainPanel(initialUrl)
        val content = contentManager.factory.createContent(rootComponent, null, false)
        contentManager.addContent(content)
    }
}
