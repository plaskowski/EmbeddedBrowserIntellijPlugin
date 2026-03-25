package com.github.plaskowski.embeddedbrowserintellijplugin.ui

import com.github.plaskowski.embeddedbrowserintellijplugin.EmbeddedBrowserSettings
import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ex.ApplicationEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefBrowserBuilder
import java.awt.BorderLayout
import java.io.File
import javax.swing.JPanel

private const val ZOOM_IN_STEP = 1.25
private const val ZOOM_OUT_STEP = 0.75

@Suppress("UnstableApiUsage")
class EmbeddedBrowserMainPanel(
    private val initialUrl: String,
    private val project: Project,
    private val settings: EmbeddedBrowserSettings
) : SimpleToolWindowPanel(true, true), Disposable {

    private val jbCefBrowser: JBCefBrowser = JBCefBrowserBuilder().setUrl(initialUrl).build()
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
        jbCefBrowser.cefBrowser.client.addDisplayHandler(CefUrlChangeHandler { url ->
            if (url != null && url != "about:blank") {
                urlFieldAction.text = url
                settings.initialUrl = url
            }
        })
        val actionToolbar = am.createActionToolbar(
            ActionPlaces.TOOLBAR, DefaultActionGroup(
                HomePageAction(jbCefBrowser, initialUrl),
                BackAction(jbCefBrowser),
                urlFieldAction,
                ReloadAction(jbCefBrowser),
                ZoomInAction(jbCefBrowser),
                ZoomOutAction(jbCefBrowser),
                RefreshProfileAction(project, settings)
            ), true
        )
        actionToolbar.targetComponent = this
        return actionToolbar
    }

    class ZoomInAction(private val jbCefBrowser: JBCefBrowser) : AnAction(AllIcons.General.ZoomIn) {
        override fun actionPerformed(e: AnActionEvent) {
            jbCefBrowser.zoomLevel = jbCefBrowser.zoomLevel * ZOOM_IN_STEP
        }
        override fun getActionUpdateThread() = ActionUpdateThread.BGT
    }

    class ZoomOutAction(private val jbCefBrowser: JBCefBrowser) : AnAction(AllIcons.General.ZoomOut) {
        override fun actionPerformed(e: AnActionEvent) {
            jbCefBrowser.zoomLevel = jbCefBrowser.zoomLevel * ZOOM_OUT_STEP
        }
        override fun getActionUpdateThread() = ActionUpdateThread.BGT
    }

    class UrlFieldAction(private val jbCefBrowser: JBCefBrowser) : TextFieldAction("", "URL", null, 25) {
        override fun perform() {
            jbCefBrowser.loadURL(text)
        }
        override fun getActionUpdateThread() = ActionUpdateThread.BGT
    }

    class BackAction(private val jbCefBrowser: JBCefBrowser) : AnAction(AllIcons.Actions.Back) {
        override fun actionPerformed(e: AnActionEvent) {
            if (jbCefBrowser.cefBrowser.canGoBack()) {
                jbCefBrowser.cefBrowser.goBack()
            }
        }
        override fun getActionUpdateThread() = ActionUpdateThread.BGT
    }

    class ReloadAction(private val jbCefBrowser: JBCefBrowser) : AnAction(AllIcons.Actions.Refresh) {
        override fun actionPerformed(e: AnActionEvent) {
            jbCefBrowser.cefBrowser.reload()
        }
        override fun getActionUpdateThread() = ActionUpdateThread.BGT
    }

    class HomePageAction(private val jbCefBrowser: JBCefBrowser, private val initialUrl: String) :
        AnAction(AllIcons.Nodes.HomeFolder) {
        override fun actionPerformed(e: AnActionEvent) {
            jbCefBrowser.cefBrowser.loadURL(initialUrl)
        }
        override fun getActionUpdateThread() = ActionUpdateThread.BGT
    }

    class RefreshProfileAction(
        private val project: Project,
        private val settings: EmbeddedBrowserSettings
    ) : AnAction("Refresh Profile", "Copy seed profile to runtime directory and restart IDE", AllIcons.Actions.ForceRefresh) {

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabledAndVisible =
                settings.seedProfilePath != null && settings.cachePath != null
        }

        override fun actionPerformed(e: AnActionEvent) {
            val seedPath = settings.seedProfilePath ?: return
            val cachePath = settings.cachePath ?: return
            val confirmed = Messages.showOkCancelDialog(
                project,
                "This will copy the seed profile from:\n$seedPath\n\nto:\n$cachePath\n\nThe IDE will restart immediately after. Proceed?",
                "Refresh Browser Profile",
                "Refresh & Restart",
                "Cancel",
                Messages.getQuestionIcon()
            ) == Messages.OK
            if (!confirmed) return

            ApplicationManager.getApplication().executeOnPooledThread {
                try {
                    val src = File(seedPath)
                    val dst = File(cachePath)
                    dst.deleteRecursively()
                    src.copyRecursively(dst, overwrite = true)
                    (ApplicationManager.getApplication() as ApplicationEx).restart(true)
                } catch (ex: Exception) {
                    ApplicationManager.getApplication().invokeLater {
                        NotificationGroupManager.getInstance()
                            .getNotificationGroup("EmbeddedBrowser")
                            .createNotification("Profile refresh failed: ${ex.message}", NotificationType.ERROR)
                            .notify(project)
                    }
                }
            }
        }

        override fun getActionUpdateThread() = ActionUpdateThread.BGT
    }

    override fun dispose() {
        jbCefBrowser.dispose()
    }
}
