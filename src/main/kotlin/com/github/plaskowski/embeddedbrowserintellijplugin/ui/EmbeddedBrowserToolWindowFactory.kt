package com.github.plaskowski.embeddedbrowserintellijplugin.ui

import com.github.plaskowski.embeddedbrowserintellijplugin.EmbeddedBrowserSettings
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.jetbrains.cef.JCefAppConfig

// I followed FavoritesViewToolWindowFactory as example
class EmbeddedBrowserToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val settings = EmbeddedBrowserSettings.getInstance(project)
        configureCef(settings)
        setupToolWindowContent(toolWindow, project, settings)
    }

    private fun configureCef(settings: EmbeddedBrowserSettings) {
        // This is an ugly way to implement this as this is global configuration so other CEF usages will be affected.
        // It won't work if the JCefApp is already initialized.
        val cefSettings = JCefAppConfig.getInstance().cefSettings
        settings.cachePath?.let { cefSettings.cache_path = it }
        settings.userAgent?.let { cefSettings.user_agent = it }
    }

    private fun setupToolWindowContent(toolWindow: ToolWindow, project: Project, settings: EmbeddedBrowserSettings) {
        val contentManager = toolWindow.contentManager
        val rootComponent = EmbeddedBrowserMainPanel(settings.initialUrl, project, settings)
        val content = contentManager.factory.createContent(rootComponent, null, false)
        contentManager.addContent(content)
    }
}
