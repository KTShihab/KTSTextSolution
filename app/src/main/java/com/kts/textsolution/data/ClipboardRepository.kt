package com.kts.textsolution.data

import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClipboardRepository private constructor(context: Context) {

    private val dbHelper = DatabaseHelper(context.applicationContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _historyFlow = MutableStateFlow<List<ClipboardItem>>(emptyList())
    val historyFlow: StateFlow<List<ClipboardItem>> = _historyFlow.asStateFlow()

    private val _favoritesFlow = MutableStateFlow<List<ClipboardItem>>(emptyList())
    val favoritesFlow: StateFlow<List<ClipboardItem>> = _favoritesFlow.asStateFlow()

    private val _foldersFlow = MutableStateFlow<List<FavoriteFolder>>(emptyList())
    val foldersFlow: StateFlow<List<FavoriteFolder>> = _foldersFlow.asStateFlow()

    init {
        refreshData()
    }

    fun refreshData() {
        scope.launch {
            try {
                val all = loadAllHistoryFromDb()
                val favs = loadFavoritesFromDb()
                val folders = loadFoldersFromDb()
                _historyFlow.value = all
                _favoritesFlow.value = favs
                _foldersFlow.value = folders
            } catch (t: Throwable) {
                Log.e(TAG, "Error refreshing clipboard data", t)
            }
        }
    }

    fun addOrUpdateClip(context: Context, rawText: String): Boolean {
        if (!KtsPreferences.isClipboardHistoryEnabled(context)) {
            return false
        }
        val text = rawText.trim()
        if (text.isEmpty()) {
            return false
        }

        scope.launch {
            try {
                val db = dbHelper.writableDatabase
                // Check latest item to avoid consecutive duplicate entries
                var latestId: Long? = null
                var latestText: String? = null
                val cursor = db.rawQuery(
                    "SELECT id, text FROM clipboard_items ORDER BY id DESC LIMIT 1",
                    null
                )
                cursor.use {
                    if (it.moveToFirst()) {
                        latestId = it.getLong(0)
                        latestText = it.getString(1)
                    }
                }

                val now = System.currentTimeMillis()
                if (latestText == text && latestId != null) {
                    // Update timestamp of existing latest item and ensure is_in_history = 1
                    val cv = ContentValues().apply {
                        put("timestamp", now)
                        put("is_in_history", 1)
                    }
                    db.update("clipboard_items", cv, "id = ?", arrayOf(latestId.toString()))
                } else {
                    // Insert new item
                    val cv = ContentValues().apply {
                        put("text", text)
                        put("timestamp", now)
                        put("is_favorite", 0)
                        put("is_in_history", 1)
                    }
                    db.insert("clipboard_items", null, cv)
                }

                // Clean up any orphan rows (neither history nor favorite)
                db.execSQL("DELETE FROM clipboard_items WHERE is_favorite = 0 AND is_in_history = 0")

                // Trim non-favorites in history to max 500
                db.execSQL(
                    "DELETE FROM clipboard_items WHERE is_favorite = 0 AND id NOT IN " +
                            "(SELECT id FROM clipboard_items WHERE is_in_history = 1 ORDER BY id DESC LIMIT 500)"
                )

                refreshData()
            } catch (t: Throwable) {
                Log.e(TAG, "Error adding clip to history", t)
            }
        }
        return true
    }

    fun toggleFavorite(item: ClipboardItem) {
        setFavorite(item.id, !item.isFavorite)
    }

    fun setFavorite(id: Long, isFavorite: Boolean) {
        scope.launch {
            try {
                val db = dbHelper.writableDatabase
                if (!isFavorite) {
                    val cv = ContentValues().apply {
                        put("is_favorite", 0)
                    }
                    db.update("clipboard_items", cv, "id = ?", arrayOf(id.toString()))
                    // If it was already cleared from history, remove row completely
                    db.delete("clipboard_items", "id = ? AND is_in_history = 0", arrayOf(id.toString()))
                } else {
                    val cv = ContentValues().apply {
                        put("is_favorite", 1)
                    }
                    db.update("clipboard_items", cv, "id = ?", arrayOf(id.toString()))
                }
                refreshData()
            } catch (t: Throwable) {
                Log.e(TAG, "Error setting favorite for id $id", t)
            }
        }
    }

    fun removeFromHistory(id: Long) {
        scope.launch {
            try {
                val db = dbHelper.writableDatabase
                // If it is a favorite, keep it in DB but set is_in_history = 0
                val cv = ContentValues().apply {
                    put("is_in_history", 0)
                }
                db.update("clipboard_items", cv, "id = ? AND is_favorite = 1", arrayOf(id.toString()))
                // If it was NOT a favorite, delete completely
                db.delete("clipboard_items", "id = ? AND is_favorite = 0", arrayOf(id.toString()))
                refreshData()
            } catch (t: Throwable) {
                Log.e(TAG, "Error removing item $id from history", t)
            }
        }
    }

    fun deleteItem(id: Long) {
        scope.launch {
            try {
                val db = dbHelper.writableDatabase
                db.delete("clipboard_items", "id = ?", arrayOf(id.toString()))
                refreshData()
            } catch (t: Throwable) {
                Log.e(TAG, "Error deleting item $id", t)
            }
        }
    }

    fun updateItemText(id: Long, newText: String) {
        val trimmed = newText.trim()
        if (trimmed.isEmpty()) return
        scope.launch {
            try {
                val db = dbHelper.writableDatabase
                val cv = ContentValues().apply {
                    put("text", trimmed)
                    put("timestamp", System.currentTimeMillis())
                }
                db.update("clipboard_items", cv, "id = ?", arrayOf(id.toString()))
                refreshData()
            } catch (t: Throwable) {
                Log.e(TAG, "Error updating item $id text", t)
            }
        }
    }

    fun saveOrUpdateText(
        text: String,
        existingId: Long? = null,
        isFavorite: Boolean? = null,
        setFolderId: Boolean = false,
        folderId: Long? = null,
        onResult: ((Long) -> Unit)? = null,
    ) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        scope.launch {
            try {
                val db = dbHelper.writableDatabase
                val now = System.currentTimeMillis()
                var targetId: Long = existingId ?: -1L
                var updated = false
                if (existingId != null) {
                    val cv = ContentValues().apply {
                        put("text", trimmed)
                        put("timestamp", now)
                        if (isFavorite != null) {
                            put("is_favorite", if (isFavorite) 1 else 0)
                        }
                        if (setFolderId) {
                            if (folderId != null) put("folder_id", folderId) else putNull("folder_id")
                        }
                    }
                    val rows = db.update("clipboard_items", cv, "id = ?", arrayOf(existingId.toString()))
                    if (rows > 0) {
                        updated = true
                        if (isFavorite == false) {
                            db.delete("clipboard_items", "id = ? AND is_in_history = 0", arrayOf(existingId.toString()))
                        }
                        targetId = existingId
                    }
                }
                if (!updated) {
                    val cv = ContentValues().apply {
                        put("text", trimmed)
                        put("timestamp", now)
                        put("is_favorite", if (isFavorite == true) 1 else 0)
                        put("is_in_history", 1)
                        if (setFolderId && folderId != null) {
                            put("folder_id", folderId)
                        }
                    }
                    targetId = db.insert("clipboard_items", null, cv)
                }
                refreshData()
                onResult?.invoke(targetId)
            } catch (t: Throwable) {
                Log.e(TAG, "Error saving or updating text", t)
            }
        }
    }

    fun clearHistory(keepFavorites: Boolean = true) {
        scope.launch {
            try {
                val db = dbHelper.writableDatabase
                if (keepFavorites) {
                    // Mark favorites as not in history so they disappear from History list but STAY in Favorites!
                    val cv = ContentValues().apply {
                        put("is_in_history", 0)
                    }
                    db.update("clipboard_items", cv, "is_favorite = 1", null)
                    // Delete all non-favorites completely
                    db.delete("clipboard_items", "is_favorite = 0", null)
                } else {
                    db.delete("clipboard_items", null, null)
                }
                refreshData()
            } catch (t: Throwable) {
                Log.e(TAG, "Error clearing history", t)
            }
        }
    }

    fun createFolder(name: String, onResult: ((Boolean) -> Unit)? = null) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            onResult?.invoke(false)
            return
        }
        scope.launch {
            try {
                val db = dbHelper.writableDatabase
                val cv = ContentValues().apply {
                    put("name", trimmed)
                    put("created_at", System.currentTimeMillis())
                }
                val rowId = db.insertWithOnConflict("favorite_folders", null, cv, SQLiteDatabase.CONFLICT_IGNORE)
                val success = rowId != -1L
                if (success) {
                    refreshData()
                }
                onResult?.invoke(success)
            } catch (t: Throwable) {
                Log.e(TAG, "Error creating folder $name", t)
                onResult?.invoke(false)
            }
        }
    }

    fun renameFolder(id: Long, newName: String, onResult: ((Boolean) -> Unit)? = null) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) {
            onResult?.invoke(false)
            return
        }
        scope.launch {
            try {
                val db = dbHelper.writableDatabase
                val cv = ContentValues().apply {
                    put("name", trimmed)
                }
                val count = db.updateWithOnConflict("favorite_folders", cv, "id = ?", arrayOf(id.toString()), SQLiteDatabase.CONFLICT_IGNORE)
                val success = count > 0
                if (success) {
                    refreshData()
                }
                onResult?.invoke(success)
            } catch (t: Throwable) {
                Log.e(TAG, "Error renaming folder $id", t)
                onResult?.invoke(false)
            }
        }
    }

    fun deleteFolder(id: Long) {
        scope.launch {
            try {
                val db = dbHelper.writableDatabase
                // Safely detach items from this folder to General (null) so they are preserved
                val cv = ContentValues().apply {
                    putNull("folder_id")
                }
                db.update("clipboard_items", cv, "folder_id = ?", arrayOf(id.toString()))
                db.delete("favorite_folders", "id = ?", arrayOf(id.toString()))
                refreshData()
            } catch (t: Throwable) {
                Log.e(TAG, "Error deleting folder $id", t)
            }
        }
    }

    fun moveItemToFolder(itemId: Long, folderId: Long?) {
        scope.launch {
            try {
                val db = dbHelper.writableDatabase
                val cv = ContentValues().apply {
                    if (folderId != null) {
                        put("folder_id", folderId)
                    } else {
                        putNull("folder_id")
                    }
                    put("is_favorite", 1)
                }
                db.update("clipboard_items", cv, "id = ?", arrayOf(itemId.toString()))
                refreshData()
            } catch (t: Throwable) {
                Log.e(TAG, "Error moving item $itemId to folder $folderId", t)
            }
        }
    }

    fun copyItemToFolder(item: ClipboardItem, targetFolderId: Long?) {
        scope.launch {
            try {
                val db = dbHelper.writableDatabase
                val cv = ContentValues().apply {
                    put("text", item.text)
                    put("timestamp", System.currentTimeMillis())
                    put("is_favorite", 1)
                    put("is_in_history", 0)
                    if (targetFolderId != null) {
                        put("folder_id", targetFolderId)
                    } else {
                        putNull("folder_id")
                    }
                }
                db.insert("clipboard_items", null, cv)
                refreshData()
            } catch (t: Throwable) {
                Log.e(TAG, "Error copying item ${item.id} to folder $targetFolderId", t)
            }
        }
    }

    private fun loadFoldersFromDb(): List<FavoriteFolder> {
        val list = mutableListOf<FavoriteFolder>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT f.id, f.name, f.created_at, COUNT(c.id) AS item_count
            FROM favorite_folders f
            LEFT JOIN clipboard_items c ON c.folder_id = f.id AND c.is_favorite = 1
            GROUP BY f.id
            ORDER BY f.name COLLATE NOCASE ASC
            """.trimIndent(),
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    FavoriteFolder(
                        id = it.getLong(0),
                        name = it.getString(1),
                        createdAt = it.getLong(2),
                        itemCount = it.getInt(3)
                    )
                )
            }
        }
        return list
    }

    private fun loadAllHistoryFromDb(): List<ClipboardItem> {
        val list = mutableListOf<ClipboardItem>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, text, timestamp, is_favorite, is_in_history, folder_id FROM clipboard_items WHERE is_in_history = 1 ORDER BY timestamp DESC",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                val folderId = if (it.isNull(5)) null else it.getLong(5)
                list.add(
                    ClipboardItem(
                        id = it.getLong(0),
                        text = it.getString(1),
                        timestamp = it.getLong(2),
                        isFavorite = it.getInt(3) == 1,
                        isInHistory = it.getInt(4) == 1,
                        folderId = folderId
                    )
                )
            }
        }
        return list
    }

    private fun loadFavoritesFromDb(): List<ClipboardItem> {
        val list = mutableListOf<ClipboardItem>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, text, timestamp, is_favorite, is_in_history, folder_id FROM clipboard_items WHERE is_favorite = 1 ORDER BY timestamp DESC",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                val folderId = if (it.isNull(5)) null else it.getLong(5)
                list.add(
                    ClipboardItem(
                        id = it.getLong(0),
                        text = it.getString(1),
                        timestamp = it.getLong(2),
                        isFavorite = it.getInt(3) == 1,
                        isInHistory = it.getInt(4) == 1,
                        folderId = folderId
                    )
                )
            }
        }
        return list
    }

    fun getAllDataForBackup(): Pair<List<FavoriteFolder>, List<ClipboardItem>> {
        val folders = loadFoldersFromDb()
        val items = mutableListOf<ClipboardItem>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, text, timestamp, is_favorite, is_in_history, folder_id FROM clipboard_items ORDER BY id ASC",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                val folderId = if (it.isNull(5)) null else it.getLong(5)
                items.add(
                    ClipboardItem(
                        id = it.getLong(0),
                        text = it.getString(1),
                        timestamp = it.getLong(2),
                        isFavorite = it.getInt(3) == 1,
                        isInHistory = it.getInt(4) == 1,
                        folderId = folderId
                    )
                )
            }
        }
        return Pair(folders, items)
    }

    fun restoreFromBackup(
        folders: List<FavoriteFolder>,
        items: List<ClipboardItem>,
        folderNamesByItemIndex: Map<Int, String?> = emptyMap(),
        replaceExisting: Boolean = false,
    ) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            if (replaceExisting) {
                db.delete("clipboard_items", null, null)
                db.delete("favorite_folders", null, null)
            }

            val existingByName = mutableMapOf<String, Long>()
            val folderCursor = db.rawQuery("SELECT id, name FROM favorite_folders", null)
            folderCursor.use {
                while (it.moveToNext()) {
                    val fId = it.getLong(0)
                    val fName = it.getString(1).trim().lowercase()
                    existingByName[fName] = fId
                }
            }

            val folderIdMap = mutableMapOf<Long, Long>()
            for (folder in folders) {
                val cleanName = folder.name.trim()
                if (cleanName.isEmpty()) continue
                val key = cleanName.lowercase()
                val existingId = existingByName[key]
                if (existingId != null) {
                    folderIdMap[folder.id] = existingId
                } else {
                    val cv = ContentValues().apply {
                        put("name", cleanName)
                        put("created_at", folder.createdAt)
                    }
                    val rowId = db.insertWithOnConflict("favorite_folders", null, cv, SQLiteDatabase.CONFLICT_IGNORE)
                    if (rowId != -1L) {
                        existingByName[key] = rowId
                        folderIdMap[folder.id] = rowId
                    }
                }
            }

            for ((index, item) in items.withIndex()) {
                val cleanText = item.text.trim()
                if (cleanText.isEmpty()) continue

                val resolvedFolderId: Long? = item.folderId?.let { folderIdMap[it] }
                    ?: folderNamesByItemIndex[index]?.trim()?.lowercase()?.let { existingByName[it] }

                if (replaceExisting) {
                    val cv = ContentValues().apply {
                        put("text", cleanText)
                        put("timestamp", item.timestamp)
                        put("is_favorite", if (item.isFavorite) 1 else 0)
                        put("is_in_history", if (item.isInHistory) 1 else 0)
                        if (resolvedFolderId != null) {
                            put("folder_id", resolvedFolderId)
                        } else {
                            putNull("folder_id")
                        }
                    }
                    db.insert("clipboard_items", null, cv)
                } else {
                    var matchedId: Long? = null
                    var existingTs = 0L
                    var existingFav = false
                    var existingHist = false
                    var existingFolderId: Long? = null

                    val checkCursor = db.rawQuery(
                        "SELECT id, timestamp, is_favorite, is_in_history, folder_id FROM clipboard_items WHERE text = ?",
                        arrayOf(cleanText)
                    )
                    checkCursor.use {
                        while (it.moveToNext()) {
                            val rId = it.getLong(0)
                            val rTs = it.getLong(1)
                            val rFav = it.getInt(2) == 1
                            val rHist = it.getInt(3) == 1
                            val rFolder = if (it.isNull(4)) null else it.getLong(4)
                            if (rFolder == resolvedFolderId || rFolder == null || resolvedFolderId == null) {
                                matchedId = rId
                                existingTs = rTs
                                existingFav = rFav
                                existingHist = rHist
                                existingFolderId = rFolder
                                break
                            }
                        }
                    }

                    if (matchedId != null) {
                        val finalFolder = resolvedFolderId ?: existingFolderId
                        val cv = ContentValues().apply {
                            put("timestamp", maxOf(existingTs, item.timestamp))
                            put("is_favorite", if (existingFav || item.isFavorite) 1 else 0)
                            put("is_in_history", if (existingHist || item.isInHistory) 1 else 0)
                            if (finalFolder != null) {
                                put("folder_id", finalFolder)
                            } else {
                                putNull("folder_id")
                            }
                        }
                        db.update("clipboard_items", cv, "id = ?", arrayOf(matchedId.toString()))
                    } else {
                        val cv = ContentValues().apply {
                            put("text", cleanText)
                            put("timestamp", item.timestamp)
                            put("is_favorite", if (item.isFavorite) 1 else 0)
                            put("is_in_history", if (item.isInHistory) 1 else 0)
                            if (resolvedFolderId != null) {
                                put("folder_id", resolvedFolderId)
                            } else {
                                putNull("folder_id")
                            }
                        }
                        db.insert("clipboard_items", null, cv)
                    }
                }
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        val all = loadAllHistoryFromDb()
        val favs = loadFavoritesFromDb()
        val updatedFolders = loadFoldersFromDb()
        _historyFlow.value = all
        _favoritesFlow.value = favs
        _foldersFlow.value = updatedFolders
    }

    private class DatabaseHelper(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE clipboard_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    text TEXT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    is_favorite INTEGER NOT NULL DEFAULT 0,
                    is_in_history INTEGER NOT NULL DEFAULT 1,
                    folder_id INTEGER DEFAULT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_clipboard_timestamp ON clipboard_items(timestamp DESC)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_clipboard_favorite ON clipboard_items(is_favorite)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_clipboard_history ON clipboard_items(is_in_history)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_clipboard_folder ON clipboard_items(folder_id)")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS favorite_folders (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE,
                    created_at INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            if (oldVersion < 2) {
                try {
                    db.execSQL("ALTER TABLE clipboard_items ADD COLUMN is_in_history INTEGER NOT NULL DEFAULT 1")
                    db.execSQL("CREATE INDEX IF NOT EXISTS idx_clipboard_history ON clipboard_items(is_in_history)")
                } catch (t: Throwable) {
                    Log.w(TAG, "Error upgrading database to v2: ${t.message}")
                }
            }
            if (oldVersion < 3) {
                try {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS favorite_folders (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL UNIQUE,
                            created_at INTEGER NOT NULL
                        )
                        """.trimIndent()
                    )
                    db.execSQL("ALTER TABLE clipboard_items ADD COLUMN folder_id INTEGER DEFAULT NULL")
                    db.execSQL("CREATE INDEX IF NOT EXISTS idx_clipboard_folder ON clipboard_items(folder_id)")
                } catch (t: Throwable) {
                    Log.w(TAG, "Error upgrading database to v3: ${t.message}")
                }
            }
        }
    }

    companion object {
        private const val TAG = "ClipboardRepository"
        private const val DATABASE_NAME = "kts_clipboard.db"
        private const val DATABASE_VERSION = 3

        @Volatile
        private var INSTANCE: ClipboardRepository? = null

        fun getInstance(context: Context): ClipboardRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ClipboardRepository(context.applicationContext).also { INSTANCE = it }
            }
        }

        fun extractClipText(clip: ClipData?, context: Context): String {
            if (clip == null || clip.itemCount == 0) return ""
            val sb = StringBuilder()
            for (i in 0 until clip.itemCount) {
                val item = clip.getItemAt(i) ?: continue
                val itemText = item.coerceToText(context)?.toString()
                if (!itemText.isNullOrEmpty()) {
                    if (sb.isNotEmpty()) sb.append("\n")
                    sb.append(itemText)
                }
            }
            return sb.toString()
        }
    }
}
