package com.infusory.modelviewer.presentation.viewer

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ModelViewerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ModelViewerUiState())
    val uiState: StateFlow<ModelViewerUiState> = _uiState.asStateFlow()

    private var nextId = 0L

    val availableModels = listOf(
        ViewerModel(
            id = -1,
            name = "Lungs",
            assetPath = "models/Lungs.glb"
        ),
        ViewerModel(
            id = -1,
            name = "Bulb",
            assetPath = "models/Bulb.glb"
        ),
        ViewerModel(
            id = -1,
            name = "Microscope",
            assetPath = "models/Microscope.glb"
        ),
        ViewerModel(
            id = -1,
            name = "Solar System",
            assetPath = "models/solarsystem.glb"
        ),
        ViewerModel(
            id = -1,
            name = "Fiagena",
            assetPath = "models/Fiagena.glb"
        )
    )

    fun addModel(model: ViewerModel) {
        val newModel = model.copy(id = nextId++)

        _uiState.update { current ->
            current.copy(
                addedModels = current.addedModels + newModel
            )
        }
    }

    fun removeModel(id: Long) {
        _uiState.update { current ->
            current.copy(
                addedModels = current.addedModels.filterNot { it.id == id }
            )
        }
    }

    fun toggleInteraction(id: Long) {
        _uiState.update { current ->
            current.copy(
                addedModels = current.addedModels.map { model ->
                    if (model.id == id) {
                        model.copy(
                            interactionMode = !model.interactionMode
                        )
                    } else {
                        model
                    }
                }
            )
        }
    }

    fun toggleLabels(id: Long) {
        _uiState.update { current ->
            current.copy(
                addedModels = current.addedModels.map { model ->
                    if (model.id == id) {
                        model.copy(
                            labelsVisible = !model.labelsVisible
                        )
                    } else {
                        model
                    }
                }
            )
        }
    }
}