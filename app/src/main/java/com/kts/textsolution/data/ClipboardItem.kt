package com.kts.textsolution.data

data class ClipboardItem(
    val id: Long = 0,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isInHistory: Boolean = true,
    val folderId: Long? = null,
)
