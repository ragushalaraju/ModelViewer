package com.infusory.modelviewer.presentation.viewer

data class ViewerModel(
    val id: Long,
    val name: String,
    val assetPath: String,
    val interactionMode: Boolean = false,
    val labelsVisible: Boolean = false
)