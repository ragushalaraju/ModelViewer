package com.infusory.modelviewer.presentation.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ModelViewerScreen(
    viewModel: ModelViewerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showMenu by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
    ) {

        uiState.addedModels.forEach { model ->

            key(model.id) {
                ModelContainer(
                    model = model,
                    onToggleInteraction = {
                        viewModel.toggleInteraction(model.id)
                    },
                    onToggleLabels = {
                        viewModel.toggleLabels(model.id)
                    },
                    onClose = {
                        viewModel.removeModel(model.id)
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {

            FloatingActionButton(
                onClick = {
                    showMenu = true
                }
            ) {
                Text("+")
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = {
                    showMenu = false
                }
            ) {

                viewModel.availableModels.forEach { model ->

                    DropdownMenuItem(
                        text = {
                            Text(model.name)
                        },
                        onClick = {
                            viewModel.addModel(model)
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}