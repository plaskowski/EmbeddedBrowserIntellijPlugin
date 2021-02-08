package com.github.plaskowski.embeddedbrowserintellijplugin.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.SideBorder
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.JToolBar

private const val ZOOM_IN_STEP = 1.25
private const val ZOOM_OUT_STEP = 0.75
private const val TOOLBAR_ICON_WIDTH = 32
private const val TOOLBAR_ICON_HEIGHT = 30

@Suppress("UnstableApiUsage")
class EmbeddedBrowserMainPanel(initialUrl: String) : SimpleToolWindowPanel(true, true), Disposable {

    private val jbCefBrowser: JBCefBrowser

    init {
        jbCefBrowser = JBCefBrowser(initialUrl)
        val border = IdeBorderFactory.createBorder(UIUtil.getBoundsColor(), SideBorder.ALL)
        val mainPanel = JPanel(BorderLayout())
        mainPanel.border = border
        val toolbar = buildToolbar(initialUrl)
        toolbar.border = border

        mainPanel.add(jbCefBrowser.component, BorderLayout.CENTER)
        mainPanel.add(toolbar, BorderLayout.NORTH)
        setContent(mainPanel)
    }

    private fun buildToolbar(initialUrl: String): JToolBar {
        val toolbar = JToolBar()
        val urlTextField = JTextField(initialUrl)
        val refreshButton = createToolbarIconButton("/actions/refresh.png")
        val zoomInButton = createToolbarIconButton("/general/zoomIn.png")
        val zoomOutButton = createToolbarIconButton("/general/zoomOut.png")

        urlTextField.addActionListener { jbCefBrowser.loadURL(urlTextField.text) }
        refreshButton.addActionListener { jbCefBrowser.cefBrowser.reload() }
        zoomInButton.addActionListener { jbCefBrowser.zoomLevelEx *= ZOOM_IN_STEP }
        zoomOutButton.addActionListener { jbCefBrowser.zoomLevelEx *= ZOOM_OUT_STEP }

        toolbar.add(urlTextField)
        toolbar.add(refreshButton)
        toolbar.add(zoomInButton)
        toolbar.add(zoomOutButton)
        return toolbar
    }

    private fun createToolbarIconButton(iconPath: String): JButton {
        val button = JButton()
        button.icon = ImageIcon(javaClass.getResource(iconPath))
        button.preferredSize = Dimension(TOOLBAR_ICON_WIDTH, TOOLBAR_ICON_HEIGHT)
        button.text = ""
        return button
    }

    override fun dispose() {
        jbCefBrowser.dispose()
    }
}
