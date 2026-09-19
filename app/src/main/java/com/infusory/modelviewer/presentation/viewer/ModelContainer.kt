package com.infusory.modelviewer.presentation.viewer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.filament.gltfio.FilamentInstance
import com.infusory.modelviewer.data.parser.GlbMetadataParser
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberView
import io.github.sceneview.utils.worldToScreen
import kotlin.math.roundToInt

@Composable
fun ModelContainer(
    model: ViewerModel,
    onToggleInteraction: () -> Unit,
    onToggleLabels: () -> Unit,
    onClose: () -> Unit
) {
    var offset by remember(model.id) {
        mutableStateOf(
            Offset(
                x = 80f,
                y = 150f
            )
        )
    }

    var containerScale by remember(model.id) {
        mutableFloatStateOf(1f)
    }

    var rotationX by remember(model.id) {
        mutableFloatStateOf(0f)
    }

    var rotationY by remember(model.id) {
        mutableFloatStateOf(0f)
    }

    var modelScale by remember(model.id) {
        mutableFloatStateOf(1f)
    }

    val minContainerScale = 0.6f
    val maxContainerScale = 2.0f

    val minModelScale = 0.5f
    val maxModelScale = 3.0f

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = offset.x.roundToInt(),
                    y = offset.y.roundToInt()
                )
            }
            .size(
                (220 * containerScale).dp
            )
            .border(
                width = 1.dp,
                color = if (model.interactionMode) {
                    Color.Cyan
                } else {
                    Color.White.copy(alpha = 0.5f)
                }
            )
            .background(
                Color.Black.copy(alpha = 0.15f)
            )
            .pointerInput(
                model.id,
                model.interactionMode
            ) {

                detectTransformGestures { _, pan, zoom, _ ->

                    if (model.interactionMode) {

                        if (
                            kotlin.math.abs(
                                zoom - 1f
                            ) > 0.01f
                        ) {

                            // 🤏 Model zoom

                            modelScale =
                                (modelScale * zoom)
                                    .coerceIn(
                                        minModelScale,
                                        maxModelScale
                                    )

                        } else {


                            rotationY +=
                                pan.x * 0.4f

                            rotationX +=
                                pan.y * 0.4f
                        }

                    } else {


                        if (
                            kotlin.math.abs(
                                zoom - 1f
                            ) > 0.01f
                        ) {

                            containerScale =
                                (containerScale * zoom)
                                    .coerceIn(
                                        minContainerScale,
                                        maxContainerScale
                                    )

                        } else {

                            offset += pan
                        }
                    }
                }
            }
    ) {

        val context = LocalContext.current

        val engine = rememberEngine()

        val sceneView = rememberView(
            engine
        )

        var loadedInstance by remember(model.id) {
            mutableStateOf<FilamentInstance?>(null)
        }

        var screenLabels by remember(model.id) {
            mutableStateOf<Map<String, Offset>>(
                emptyMap()
            )
        }

        DisposableEffect(model.id) {

            onDispose {
                loadedInstance = null
                screenLabels = emptyMap()
            }
        }

        val labels = remember(model.assetPath) {

            GlbMetadataParser.parseLabels(
                context = context,
                assetPath = model.assetPath
            )
        }

        SceneView(
            modifier = Modifier.fillMaxSize(),

            engine = engine,

            view = sceneView,

            onFrame = {

                if (model.labelsVisible) {

                    val instance =
                        loadedInstance
                            ?: return@SceneView

                    val asset =
                        instance.asset

                    val transformManager =
                        engine.transformManager

                    val projected =
                        mutableMapOf<String, Offset>()


                    labels.forEach { label ->

                        val nodeName =
                            label.nodeName
                                ?: return@forEach


                        val entity =
                            asset.getFirstEntityByName(
                                nodeName
                            )

                        if (entity == 0) {
                            return@forEach
                        }

                        val transformInstance =
                            transformManager
                                .getInstance(entity)

                        if (transformInstance == 0) {
                            return@forEach
                        }

                        val matrix =
                            FloatArray(16)

                        transformManager.getWorldTransform(
                            transformInstance,
                            matrix
                        )

                        val worldPosition =
                            Position(
                                x = matrix[12],
                                y = matrix[13],
                                z = matrix[14]
                            )

                        val screenPosition =
                            sceneView.worldToScreen(
                                worldPosition
                            )
                                ?: return@forEach

                        projected[label.text] =
                            Offset(
                                x = screenPosition.x,
                                y = screenPosition.y
                            )
                    }

                    if (
                        labelPositionsChanged(
                            screenLabels,
                            projected
                        )
                    ) {
                        screenLabels = projected
                    }

                } else {

                    if (screenLabels.isNotEmpty()) {

                        screenLabels =
                            emptyMap()
                    }
                }
            }
        ) {

            val modelInstance =
                rememberModelInstance(
                    modelLoader = modelLoader,
                    assetFileLocation =
                        model.assetPath
                )

            modelInstance?.let { instance ->


                loadedInstance =
                    instance

                ModelNode(
                    modelInstance = instance,

                    scaleToUnits = 1.0f,

                    autoAnimate = false,

                    rotation =
                        Rotation(
                            x = rotationX,
                            y = rotationY,
                            z = 0f
                        ),


                    scale =
                        Scale(
                            x = modelScale,
                            y = modelScale,
                            z = modelScale
                        )
                )
            }
        }

        if (model.labelsVisible) {

            val density = LocalDensity.current

            val containerSizePx = with(density) {
                (220 * containerScale).dp.toPx()
            }

            val labelOffsetX = with(density) {
                20.dp.toPx()
            }

            val labelOffsetY = with(density) {
                (-28).dp.toPx()
            }

            val anchorRadius = with(density) {
                3.dp.toPx()
            }

            val lineWidth = with(density) {
                1.dp.toPx()
            }

            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {

                screenLabels.forEach { (label, anchor) ->

                    val rawLabelX =
                        anchor.x + labelOffsetX

                    val rawLabelY =
                        anchor.y + labelOffsetY

                    val labelX =
                        rawLabelX.coerceIn(
                            4f,
                            containerSizePx - 100f
                        )

                    val labelY =
                        rawLabelY.coerceIn(
                            4f,
                            containerSizePx - 35f
                        )

                    drawLine(
                        color = Color.White.copy(alpha = 0.8f),
                        start = anchor,
                        end = Offset(
                            x = labelX,
                            y = labelY + 12.dp.toPx()
                        ),
                        strokeWidth = lineWidth
                    )

                    drawCircle(
                        color = Color.Cyan,
                        radius = anchorRadius,
                        center = anchor
                    )
                }
            }

            screenLabels.forEach { (label, anchor) ->

                val rawLabelX =
                    anchor.x + labelOffsetX

                val rawLabelY =
                    anchor.y + labelOffsetY

                val labelX =
                    rawLabelX.coerceIn(
                        4f,
                        containerSizePx - 100f
                    )

                val labelY =
                    rawLabelY.coerceIn(
                        4f,
                        containerSizePx - 35f
                    )

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = labelX.roundToInt(),
                                y = labelY.roundToInt()
                            )
                        }
                        .background(
                            color = Color.Black.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(
                            horizontal = 7.dp,
                            vertical = 3.dp
                        )
                ) {

                    Text(
                        text = label,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .align(
                    Alignment.BottomCenter
                )
                .padding(8.dp),

            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {


            IconButton(
                onClick =
                    onToggleInteraction
            ) {

                Icon(
                    imageVector =
                        Icons.Default.TouchApp,

                    contentDescription =
                        "Interaction",

                    tint =
                        if (
                            model.interactionMode
                        ) {
                            Color.Cyan
                        } else {
                            Color.White
                        }
                )
            }

            IconButton(
                onClick =
                    onToggleLabels
            ) {

                Icon(
                    imageVector =
                        Icons.Default.Label,

                    contentDescription =
                        "Labels",

                    tint =
                        if (
                            model.labelsVisible
                        ) {
                            Color.Yellow
                        } else {
                            Color.White
                        }
                )
            }

            IconButton(
                onClick = onClose
            ) {

                Icon(
                    imageVector =
                        Icons.Default.Close,

                    contentDescription =
                        "Close",

                    tint =
                        Color.White
                )
            }
        }
    }
}

private fun labelPositionsChanged(
    old: Map<String, Offset>,
    new: Map<String, Offset>,
    threshold: Float = 0.5f
): Boolean {

    if (old.size != new.size) {
        return true
    }

    new.forEach { (key, newPosition) ->

        val oldPosition =
            old[key] ?: return true

        if (
            kotlin.math.abs(
                oldPosition.x - newPosition.x
            ) > threshold ||
            kotlin.math.abs(
                oldPosition.y - newPosition.y
            ) > threshold
        ) {
            return true
        }
    }

    return false
}
