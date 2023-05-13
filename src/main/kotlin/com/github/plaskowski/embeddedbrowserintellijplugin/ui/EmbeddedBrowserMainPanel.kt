package com.github.plaskowski.embeddedbrowserintellijplugin.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.jcef.JBCefBrowser
import java.awt.BorderLayout
import javax.swing.JPanel

private const val ZOOM_IN_STEP = 1.25
private const val ZOOM_OUT_STEP = 0.75

@Suppress("UnstableApiUsage")
class EmbeddedBrowserMainPanel(private val initialUrl: String) : SimpleToolWindowPanel(true, true), Disposable {

    private val jbCefBrowser: JBCefBrowser = JBCefBrowser(initialUrl)
    private val toolbar: ActionToolbar

    init {
        val mainPanel = JPanel(BorderLayout())
        toolbar = buildToolbar()

        mainPanel.add(jbCefBrowser.component, BorderLayout.CENTER)
        mainPanel.add(toolbar.component, BorderLayout.NORTH)
        setContent(mainPanel)
    }

    private fun buildToolbar(): ActionToolbar {
        val am = ActionManager.getInstance()
        val urlFieldAction = UrlFieldAction(jbCefBrowser)
        jbCefBrowser.cefBrowser.client.addDisplayHandler(CefUrlChangeHandler { url -> urlFieldAction.text = url ?: "" })
        val actionToolbar = am.createActionToolbar(
            ActionPlaces.TOOLBAR, DefaultActionGroup(
                HomePageAction(jbCefBrowser, initialUrl),
                BackAction(jbCefBrowser),
                urlFieldAction,
                ReloadAction(jbCefBrowser),
                ZoomInAction(jbCefBrowser),
                ZoomOutAction(jbCefBrowser)
            ), true
        )
        actionToolbar.targetComponent = this
        return actionToolbar
    }

    class ZoomInAction(private val jbCefBrowser: JBCefBrowser) : AnAction(AllIcons.General.ZoomIn) {
        override fun actionPerformed(e: AnActionEvent) {
            jbCefBrowser.zoomLevelEx *= ZOOM_IN_STEP
        }
    }

    class ZoomOutAction(private val jbCefBrowser: JBCefBrowser) : AnAction(AllIcons.General.ZoomOut) {
        override fun actionPerformed(e: AnActionEvent) {
            jbCefBrowser.zoomLevelEx *= ZOOM_OUT_STEP
        }
    }

    class UrlFieldAction(private val jbCefBrowser: JBCefBrowser) : TextFieldAction("", "URL", null, 25) {
        override fun perform() {
            jbCefBrowser.loadURL(text)
        }
    }

    class BackAction(private val jbCefBrowser: JBCefBrowser) : AnAction(AllIcons.Actions.Back) {
        override fun actionPerformed(e: AnActionEvent) {
            if (jbCefBrowser.cefBrowser.canGoBack()) {
                jbCefBrowser.cefBrowser.goBack()
            }
        }
    }

    class ReloadAction(private val jbCefBrowser: JBCefBrowser) : AnAction(AllIcons.Actions.Refresh) {
        override fun actionPerformed(e: AnActionEvent) {
            jbCefBrowser.cefBrowser.reload()
        }
    }

    class HomePageAction(private val jbCefBrowser: JBCefBrowser, private val initialUrl: String) :
        AnAction(AllIcons.Nodes.HomeFolder) {
        override fun actionPerformed(e: AnActionEvent) {
            jbCefBrowser.cefBrowser.loadURL(initialUrl)
        }
    }

    override fun dispose() {
        jbCefBrowser.dispose()
    }
}
