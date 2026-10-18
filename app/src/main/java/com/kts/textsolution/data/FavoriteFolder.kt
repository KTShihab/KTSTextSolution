package com.kts.textsolution.data

data class FavoriteFolder(
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val itemCount: Int = 0,
)
