package com.kts.textsolution.data

import com.kts.textsolution.AppTab
import com.kts.textsolution.Snapshot
import java.util.UUID

data class TextSession(
    val id: String = UUID.randomUUID().toString(),
    var sourceText: String = "",
    var chunks: List<Chunk> = emptyList(),
    var splitMode: SplitMode? = null,
    var selectedIds: Set<String> = emptySet(),
    var rangeAnchorId: String? = null,
    var focusedChunkIndex: Int = 0,
    var editingSourceItemId: Long? = null,
    var returnTabAfterOpen: AppTab? = null,
    var history: List<Snapshot> = emptyList(),
) {
    val title: String
        get() {
            val firstLine = sourceText.lineSequence().firstOrNull { it.isNotBlank() }?.trim() ?: ""
            return when {
                firstLine.length > 30 -> firstLine.take(30).trimEnd() + "…"
                firstLine.isNotEmpty() -> firstLine
                else -> "Text"
            }
        }
}
