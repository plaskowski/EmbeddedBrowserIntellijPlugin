package com.github.plaskowski.embeddedbrowserintellijplugin.ui

import com.github.plaskowski.embeddedbrowserintellijplugin.EmbeddedBrowserAppSettings
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel

class EmbeddedBrowserConfigurable : BoundConfigurable("Embedded Browser") {

    private val settings = EmbeddedBrowserAppSettings.getInstance()

    override fun createPanel(): DialogPanel = panel {
        row("Seed profile path:") {
            textFieldWithBrowseButton(
                fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
            ).bindText(
                getter = { settings.seedProfilePath ?: "" },
                setter = { settings.seedProfilePath = it.ifBlank { null } }
            )
        }
        row("User agent:") {
            textField().bindText(
                getter = { settings.userAgent ?: "" },
                setter = { settings.userAgent = it.ifBlank { null } }
            )
        }
        row {
            comment("Runtime cache path: ${EmbeddedBrowserAppSettings.resolveCachePath()}")
        }
    }
}
