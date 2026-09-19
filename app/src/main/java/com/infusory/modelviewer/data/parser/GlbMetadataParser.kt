package com.infusory.modelviewer.data.parser

import android.content.Context
import com.infusory.modelviewer.data.model.ModelLabel
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.ByteOrder

object GlbMetadataParser {

    fun parseLabels(
        context: Context,
        assetPath: String
    ): List<ModelLabel> {

        val bytes = context.assets
            .open(assetPath)
            .use { it.readBytes() }

        val buffer = ByteBuffer
            .wrap(bytes)
            .order(ByteOrder.LITTLE_ENDIAN)

        // GLB header
        val magic = buffer.int
        val version = buffer.int
        val totalLength = buffer.int

        // "glTF" little-endian magic
        require(magic == 0x46546C67) {
            "Invalid GLB file"
        }

        require(version == 2) {
            "Unsupported GLB version: $version"
        }

        // First chunk should be JSON
        val jsonChunkLength = buffer.int
        val jsonChunkType = buffer.int

        // JSON chunk type = "JSON"
        require(jsonChunkType == 0x4E4F534A) {
            "First GLB chunk is not JSON"
        }

        val jsonBytes = ByteArray(jsonChunkLength)
        buffer.get(jsonBytes)

        val jsonString = jsonBytes
            .toString(Charsets.UTF_8)
            .trimEnd('\u0000', ' ')

        val root = JSONObject(jsonString)

        val nodes = root.optJSONArray("nodes")
            ?: return emptyList()

        val labels = mutableListOf<ModelLabel>()

        for (index in 0 until nodes.length()) {

            val node = nodes.optJSONObject(index)
                ?: continue

            val extras = node.optJSONObject("extras")
                ?: continue

            val prop = extras.optString("prop")
                .takeIf { it.isNotBlank() }
                ?: continue

            labels += ModelLabel(
                nodeIndex = index,
                nodeName = node.optString("name")
                    .takeIf { it.isNotBlank() },
                text = prop
            )
        }

        return labels
    }
}