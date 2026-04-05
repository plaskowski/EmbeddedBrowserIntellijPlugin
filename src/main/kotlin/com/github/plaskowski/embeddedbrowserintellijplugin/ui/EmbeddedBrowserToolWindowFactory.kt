package com.github.plaskowski.embeddedbrowserintellijplugin.ui

import com.github.plaskowski.embeddedbrowserintellijplugin.EmbeddedBrowserAppSettings
import com.github.plaskowski.embeddedbrowserintellijplugin.EmbeddedBrowserSettings
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.jetbrains.cef.JCefAppConfig

// I followed FavoritesViewToolWindowFactory as example
class EmbeddedBrowserToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val appSettings = EmbeddedBrowserAppSettings.getInstance()
        val projectSettings = EmbeddedBrowserSettings.getInstance(project)
        configureCef(appSettings)
        setupToolWindowContent(toolWindow, project, projectSettings, appSettings)
    }

    private fun configureCef(appSettings: EmbeddedBrowserAppSettings) {
        // This is an ugly way to implement this as this is global configuration so other CEF usages will be affected.
        // It won't work if the JCefApp is already initialized.
        val cefSettings = JCefAppConfig.getInstance().cefSettings
        cefSettings.cache_path = EmbeddedBrowserAppSettings.resolveCachePath()
        appSettings.userAgent?.let { cefSettings.user_agent = it }
    }

    private fun setupToolWindowContent(
        toolWindow: ToolWindow,
        project: Project,
        projectSettings: EmbeddedBrowserSettings,
        appSettings: EmbeddedBrowserAppSettings
    ) {
        val contentManager = toolWindow.contentManager
        val rootComponent = EmbeddedBrowserMainPanel(project, projectSettings, appSettings)
        val content = contentManager.factory.createContent(rootComponent, null, false)
        contentManager.addContent(content)
    }
}
