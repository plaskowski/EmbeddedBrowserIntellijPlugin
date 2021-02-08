package com.github.plaskowski.embeddedbrowserintellijplugin.ui

import com.intellij.ui.jcef.JBCefBrowser

// Copied from newer version of JBCefBrowser

private const val ZOOM_COMMON_RATIO = 1.2
private val LOG_ZOOM = Math.log(ZOOM_COMMON_RATIO)

var JBCefBrowser.zoomLevelEx: Double
    get() = Math.pow(ZOOM_COMMON_RATIO, cefBrowser.getZoomLevel())
    set(zoomLevel) = cefBrowser.setZoomLevel(Math.log(zoomLevel) / LOG_ZOOM)
