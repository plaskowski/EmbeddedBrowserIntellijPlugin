package com.github.plaskowski.embeddedbrowserintellijplugin.services

import com.github.plaskowski.embeddedbrowserintellijplugin.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
