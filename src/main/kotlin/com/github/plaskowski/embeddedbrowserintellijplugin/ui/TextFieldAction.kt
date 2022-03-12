package com.github.plaskowski.embeddedbrowserintellijplugin.ui

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.ClickListener
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.border.Border
import javax.swing.border.CompoundBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

abstract class TextFieldAction protected constructor(
    var text: String,
    private val myDescription: String,
    private val myIcon: Icon?,
    private val initSize: Int
) :
    AnAction(text, myDescription, myIcon), CustomComponentAction, DumbAware {

    override fun actionPerformed(e: AnActionEvent) {
        perform()
    }

    open fun perform() {}

    override fun update(e: AnActionEvent) {
        val component = e.presentation.getClientProperty(CustomComponentAction.COMPONENT_KEY) as JPanel?
        if (component != null) {
            val textField = component.getComponent(0) as JTextField
            if (text != textField.text) {
                textField.text = text
            }
        }
    }

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        // honestly borrowed from SearchTextField
        val myField = JTextField(initSize)
        myField.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER) {
                    e.consume()
                    perform()
                }
            }
        })
        myField.document.addDocumentListener(DocumentChangeListener {
            text = myField.text
        })
        val panel = JPanel(BorderLayout())
        val label = JLabel(myIcon)
        label.isOpaque = true
        label.background = myField.background
        myField.isOpaque = true
        panel.add(myField, BorderLayout.WEST)
        panel.add(label, BorderLayout.EAST)
        myField.toolTipText = myDescription
        label.toolTipText = myDescription
        val originalBorder: Border
        originalBorder = if (SystemInfo.isMac) {
            BorderFactory.createLoweredBevelBorder()
        } else {
            myField.border
        }
        panel.border = CompoundBorder(JBUI.Borders.empty(4, 0, 4, 0), originalBorder)
        myField.isOpaque = true
        myField.border = JBUI.Borders.empty(0, 5, 0, 5)
        object : ClickListener() {
            override fun onClick(e: MouseEvent, clickCount: Int): Boolean {
                perform()
                return true
            }
        }.installOn(label)
        return panel
    }

    class DocumentChangeListener(private val callback: Runnable) : DocumentListener {
        override fun insertUpdate(e: DocumentEvent?) {
            callback.run()
        }

        override fun removeUpdate(e: DocumentEvent?) {
            callback.run()
        }

        override fun changedUpdate(e: DocumentEvent?) {
            callback.run()
        }

    }
}