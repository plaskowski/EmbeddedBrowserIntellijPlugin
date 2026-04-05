package com.github.plaskowski.embeddedbrowserintellijplugin.ui

import com.github.plaskowski.embeddedbrowserintellijplugin.EmbeddedBrowserAppSettings
import com.github.plaskowski.embeddedbrowserintellijplugin.EmbeddedBrowserSettings
import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefBrowserBuilder
import java.awt.BorderLayout
import java.io.File
import javax.swing.JPanel

private const val ZOOM_IN_STEP = 1.25
private const val ZOOM_OUT_STEP = 0.75

@Suppress("UnstableApiUsage")
class EmbeddedBrowserMainPanel(
    private val project: Project,
    private val projectSettings: EmbeddedBrowserSettings,
    private val appSettings: EmbeddedBrowserAppSettings
) : SimpleToolWindowPanel(true, true), Disposable {

    private var jbCefBrowser: JBCefBrowser = JBCefBrowserBuilder().setUrl(projectSettings.initialUrl).build()
    private var toolbar: ActionToolbar
    private val contentPanel = JPanel(BorderLayout())

    init {
        toolbar = buildToolbar()
        contentPanel.add(jbCefBrowser.component, BorderLayout.CENTER)
        contentPanel.add(toolbar.component, BorderLayout.NORTH)
        setContent(contentPanel)
    }

    private fun destroyBrowser() {
        contentPanel.remove(jbCefBrowser.component)
        contentPanel.remove(toolbar.component)
        jbCefBrowser.dispose()
        contentPanel.revalidate()
        contentPanel.repaint()
    }

    private fun recreateBrowser() {
        jbCefBrowser = JBCefBrowserBuilder().setUrl(projectSettings.initialUrl).build()
        toolbar = buildToolbar()
        contentPanel.add(jbCefBrowser.component, BorderLayout.CENTER)
        contentPanel.add(toolbar.component, BorderLayout.NORTH)
        contentPanel.revalidate()
        contentPanel.repaint()
    }

    private fun buildToolbar(): ActionToolbar {
        val am = ActionManager.getInstance()
        val urlFieldAction = UrlFieldAction(jbCefBrowser)
        jbCefBrowser.cefBrowser.client.addDisplayHandler(CefUrlChangeHandler { url ->
            if (url != null && url != "about:blank") {
                urlFieldAction.text = url
                projectSettings.initialUrl = url
            }
        })
        val actionToolbar = am.createActionToolbar(
            ActionPlaces.TOOLBAR, DefaultActionGroup(
                HomePageAction(jbCefBrowser, projectSettings.initialUrl),
                BackAction(jbCefBrowser),
                urlFieldAction,
                ReloadAction(jbCefBrowser),
                ZoomInAction(jbCefBrowser),
                ZoomOutAction(jbCefBrowser),
                Separator.getInstance(),
                RefreshProfileAction(project, appSettings, ::destroyBrowser, ::recreateBrowser)
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
        private val appSettings: EmbeddedBrowserAppSettings,
        private val destroyBrowser: () -> Unit,
        private val recreateBrowser: () -> Unit
    ) : AnAction("Refresh Profile", "Copy seed profile to runtime directory and refresh browser", AllIcons.Actions.ForceRefresh) {

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabledAndVisible = appSettings.seedProfilePath != null
        }

        override fun actionPerformed(e: AnActionEvent) {
            val seedPath = appSettings.seedProfilePath ?: return
            val cachePath = EmbeddedBrowserAppSettings.resolveCachePath()
            val isOutOfProcess = isJCEFRemoteEnabled()
            val message = if (isOutOfProcess)
                "This will copy the seed profile from:\n$seedPath\n\nto:\n$cachePath\n\nThe browser will restart without closing the IDE. Proceed?"
            else
                "This will copy the seed profile from:\n$seedPath\n\nto:\n$cachePath\n\nYou will need to restart the IDE afterwards to apply changes. Proceed?"
            val confirmed = Messages.showOkCancelDialog(
                project,
                message,
                "Refresh Browser Profile",
                "Refresh",
                "Cancel",
                Messages.getQuestionIcon()
            ) == Messages.OK
            if (!confirmed) return

            destroyBrowser()
            ApplicationManager.getApplication().executeOnPooledThread {
                try {
                    val src = File(seedPath)
                    val dst = File(cachePath)
                    dst.deleteRecursively()
                    src.copyRecursively(dst, overwrite = true)
                    if (tryRestartJCEF()) {
                        ApplicationManager.getApplication().invokeLater { recreateBrowser() }
                    } else {
                        ApplicationManager.getApplication().invokeLater {
                            NotificationGroupManager.getInstance()
                                .getNotificationGroup("EmbeddedBrowser")
                                .createNotification("Profile copied. Restart the IDE to apply changes.", NotificationType.INFORMATION)
                                .notify(project)
                        }
                    }
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

        private fun isJCEFRemoteEnabled(): Boolean {
            return try {
                val method = JBCefApp::class.java.getDeclaredMethod("isRemoteEnabled")
                method.isAccessible = true
                method.invoke(null) as Boolean
            } catch (_: Exception) {
                false
            }
        }

        private fun tryRestartJCEF(): Boolean {
            return try {
                val jbCefApp = JBCefApp.getInstance()
                val method = jbCefApp.javaClass.getDeclaredMethod(
                    "restartJCEF", Boolean::class.java, Boolean::class.java
                )
                method.isAccessible = true
                method.invoke(jbCefApp, false, false) as Boolean
            } catch (_: Exception) {
                false
            }
        }

        override fun getActionUpdateThread() = ActionUpdateThread.BGT
    }

    override fun dispose() {
        jbCefBrowser.dispose()
    }
}
