package com.github.plaskowski.embeddedbrowserintellijplugin.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class EmbeddedBrowserToolWindowFactory : ToolWindowFactory {

    // I followed FavoritesViewToolWindowFactory as example
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentManager = toolWindow.contentManager
        val rootComponent = EmbeddedBrowserMainPanel("https://youtube.com")
        val content = contentManager.factory.createContent(rootComponent, null, false)
        contentManager.addContent(content)
    }
}
