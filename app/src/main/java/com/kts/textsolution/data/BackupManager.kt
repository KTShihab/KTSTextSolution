package com.kts.textsolution.data

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.kts.textsolution.AppTab
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

data class BackupSettings(
    val autoReadInBackground: Boolean = false,
    val speechRate: Float = 1.0f,
    val ttsEngine: String = "",
    val ttsLanguage: String = "auto",
    val ttsVoice: String = "",
    val ttsPitch: Float = 1.0f,
    val ttsVolume: Float = 1.0f,
    val announceChunkNumbers: Boolean = true,
    val speechFilterMode: SpeechFilterMode = SpeechFilterMode.Some,
    val clipboardHistoryEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val autoClipboard: Boolean = true,
    val highContrast: Boolean = false,
    val fontSize: Float = 18f,
    val darkTheme: Boolean = true,
)

data class BackupPayload(
    val backupVersion: Int = CURRENT_BACKUP_VERSION,
    val createdAt: Long = System.currentTimeMillis(),
    val settings: BackupSettings = BackupSettings(),
    val folders: List<FavoriteFolder> = emptyList(),
    val items: List<ClipboardItem> = emptyList(),
    val folderNamesByItemIndex: Map<Int, String?> = emptyMap(),
    val sessions: List<TextSession> = emptyList(),
    val activeSessionIndex: Int = 0,
) {
    val favoriteCount: Int
        get() = items.count { it.isFavorite }

    val historyCount: Int
        get() = items.count { it.isInHistory }

    val folderCount: Int
        get() = folders.size

    val sessionCount: Int
        get() = sessions.count { it.sourceText.isNotBlank() }

    companion object {
        const val CURRENT_BACKUP_VERSION = 1
    }
}

object BackupManager {
    private const val APP_IDENTIFIER = "KTSTextSolution"
    private const val LATEST_BACKUP_FILENAME = "KTS_Backup_Latest.json"

    fun defaultBackupFileName(timestamp: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
        return "KTS_Backup_${sdf.format(Date(timestamp))}.json"
    }

    fun formatTimestamp(timestamp: Long): String {
        if (timestamp <= 0L) return ""
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun createBackupPayload(
        context: Context,
        sessions: List<TextSession>,
        activeSessionIndex: Int,
        autoClipboard: Boolean,
        highContrast: Boolean,
        fontSize: Float,
        darkTheme: Boolean,
    ): BackupPayload {
        val repo = ClipboardRepository.getInstance(context)
        val (folders, items) = repo.getAllDataForBackup()
        val folderNameMap = folders.associate { it.id to it.name }
        val folderNamesByItemIndex = mutableMapOf<Int, String?>()
        items.forEachIndexed { idx, item ->
            val fName = item.folderId?.let { folderNameMap[it] }
            if (fName != null) {
                folderNamesByItemIndex[idx] = fName
            }
        }

        val settings = BackupSettings(
            autoReadInBackground = KtsPreferences.isAutoReadBackgroundEnabled(context),
            speechRate = KtsPreferences.getSpeechRate(context),
            ttsEngine = KtsPreferences.getTtsEngine(context),
            ttsLanguage = KtsPreferences.getTtsLanguage(context),
            ttsVoice = KtsPreferences.getTtsVoice(context),
            ttsPitch = KtsPreferences.getTtsPitch(context),
            ttsVolume = KtsPreferences.getTtsVolume(context),
            announceChunkNumbers = KtsPreferences.isAnnounceChunkNumbersEnabled(context),
            speechFilterMode = KtsPreferences.getSpeechFilterMode(context),
            clipboardHistoryEnabled = KtsPreferences.isClipboardHistoryEnabled(context),
            hapticFeedbackEnabled = KtsPreferences.isHapticFeedbackEnabled(context),
            autoClipboard = autoClipboard,
            highContrast = highContrast,
            fontSize = fontSize,
            darkTheme = darkTheme,
        )

        val validSessions = sessions.filter { it.sourceText.isNotBlank() }
        val safeActiveIndex = if (validSessions.isEmpty()) 0 else activeSessionIndex.coerceIn(0, validSessions.lastIndex)

        return BackupPayload(
            backupVersion = BackupPayload.CURRENT_BACKUP_VERSION,
            createdAt = System.currentTimeMillis(),
            settings = settings,
            folders = folders,
            items = items,
            folderNamesByItemIndex = folderNamesByItemIndex,
            sessions = validSessions,
            activeSessionIndex = safeActiveIndex,
        )
    }

    fun serializeSessionsToJson(sessions: List<TextSession>, activeSessionIndex: Int): String {
        val validSessions = sessions.filter { it.sourceText.isNotBlank() }
        val root = JSONObject()
        root.put("activeSessionIndex", if (validSessions.isEmpty()) 0 else activeSessionIndex.coerceIn(0, validSessions.lastIndex))
        val sessionsArr = JSONArray()
        for (session in validSessions) {
            sessionsArr.put(sessionToJson(session))
        }
        root.put("sessions", sessionsArr)
        return root.toString()
    }

    fun deserializeSessionsFromJson(jsonStr: String): Pair<List<TextSession>, Int> {
        if (jsonStr.isBlank()) return Pair(emptyList(), 0)
        return try {
            val root = JSONObject(jsonStr)
            val activeIdx = root.optInt("activeSessionIndex", 0)
            val sessionsArr = root.optJSONArray("sessions") ?: JSONArray()
            val list = mutableListOf<TextSession>()
            for (i in 0 until sessionsArr.length()) {
                val obj = sessionsArr.optJSONObject(i) ?: continue
                val parsed = jsonToSession(obj)
                if (parsed.sourceText.isNotBlank()) {
                    list.add(parsed)
                }
            }
            val safeIdx = if (list.isEmpty()) 0 else activeIdx.coerceIn(0, list.lastIndex)
            Pair(list, safeIdx)
        } catch (e: Exception) {
            Pair(emptyList(), 0)
        }
    }

    private fun sessionToJson(session: TextSession): JSONObject {
        val obj = JSONObject()
        obj.put("id", session.id)
        obj.put("sourceText", session.sourceText)
        obj.put("splitMode", session.splitMode?.name ?: JSONObject.NULL)
        obj.put("focusedChunkIndex", session.focusedChunkIndex)
        if (session.editingSourceItemId != null) {
            obj.put("editingSourceItemId", session.editingSourceItemId)
        }
        if (session.returnTabAfterOpen != null) {
            obj.put("returnTabAfterOpen", session.returnTabAfterOpen?.name)
        }
        val selectedArr = JSONArray()
        session.selectedIds.forEach { selectedArr.put(it) }
        obj.put("selectedIds", selectedArr)

        val chunksArr = JSONArray()
        session.chunks.forEach { chunk ->
            val cObj = JSONObject()
            cObj.put("id", chunk.id)
            cObj.put("text", chunk.text)
            if (chunk.fontSizeSp != null) cObj.put("fontSizeSp", chunk.fontSizeSp.toDouble())
            if (chunk.isBold) cObj.put("isBold", true)
            if (chunk.isItalic) cObj.put("isItalic", true)
            if (chunk.isUnderline) cObj.put("isUnderline", true)
            if (!chunk.textColorHex.isNullOrBlank()) cObj.put("textColorHex", chunk.textColorHex)
            if (!chunk.alignment.isNullOrBlank()) cObj.put("alignment", chunk.alignment)
            chunksArr.put(cObj)
        }
        obj.put("chunks", chunksArr)
        return obj
    }

    private fun jsonToSession(obj: JSONObject): TextSession {
        val id = obj.optString("id", UUID.randomUUID().toString()).ifBlank { UUID.randomUUID().toString() }
        val sourceText = obj.optString("sourceText", "")
        val splitModeStr = if (obj.isNull("splitMode")) "" else obj.optString("splitMode", "")
        val splitMode = try {
            if (splitModeStr.isNotBlank()) SplitMode.valueOf(splitModeStr) else null
        } catch (e: Exception) {
            null
        }
        val focusedChunkIndex = obj.optInt("focusedChunkIndex", 0).coerceAtLeast(0)
        val editingSourceItemId = if (obj.has("editingSourceItemId") && !obj.isNull("editingSourceItemId")) {
            obj.optLong("editingSourceItemId")
        } else {
            null
        }
        val returnTabStr = if (obj.isNull("returnTabAfterOpen")) "" else obj.optString("returnTabAfterOpen", "")
        val returnTabAfterOpen = try {
            if (returnTabStr.isNotBlank()) AppTab.valueOf(returnTabStr) else null
        } catch (e: Exception) {
            null
        }

        val selectedIds = mutableSetOf<String>()
        val selectedArr = obj.optJSONArray("selectedIds")
        if (selectedArr != null) {
            for (i in 0 until selectedArr.length()) {
                val sId = selectedArr.optString(i, "")
                if (sId.isNotBlank()) selectedIds.add(sId)
            }
        }

        val chunks = mutableListOf<Chunk>()
        val chunksArr = obj.optJSONArray("chunks")
        if (chunksArr != null && chunksArr.length() > 0) {
            for (i in 0 until chunksArr.length()) {
                val cObj = chunksArr.optJSONObject(i) ?: continue
                val cText = cObj.optString("text", "")
                if (cText.isNotEmpty()) {
                    val fontSizeSp = if (cObj.has("fontSizeSp") && !cObj.isNull("fontSizeSp")) {
                        cObj.optDouble("fontSizeSp").toFloat()
                    } else {
                        null
                    }
                    val isBold = cObj.optBoolean("isBold", false)
                    val isItalic = cObj.optBoolean("isItalic", false)
                    val isUnderline = cObj.optBoolean("isUnderline", false)
                    val textColorHex = if (cObj.has("textColorHex") && !cObj.isNull("textColorHex")) cObj.optString("textColorHex") else null
                    val alignment = if (cObj.has("alignment") && !cObj.isNull("alignment")) cObj.optString("alignment") else null
                    chunks.add(
                        Chunk(
                            id = cObj.optString("id", UUID.randomUUID().toString()),
                            text = cText,
                            fontSizeSp = fontSizeSp,
                            isBold = isBold,
                            isItalic = isItalic,
                            isUnderline = isUnderline,
                            textColorHex = textColorHex,
                            alignment = alignment,
                        )
                    )
                }
            }
        } else if (splitMode != null && sourceText.isNotBlank()) {
            chunks.addAll(TextSplitter.split(sourceText, splitMode))
        }

        val safeFocus = if (chunks.isEmpty()) 0 else focusedChunkIndex.coerceIn(0, chunks.lastIndex)

        return TextSession(
            id = id,
            sourceText = sourceText,
            chunks = chunks,
            splitMode = splitMode,
            selectedIds = selectedIds,
            rangeAnchorId = null,
            focusedChunkIndex = safeFocus,
            editingSourceItemId = editingSourceItemId,
            returnTabAfterOpen = returnTabAfterOpen,
            history = emptyList(),
        )
    }

    fun serializeToJson(payload: BackupPayload): String {
        val root = JSONObject()
        root.put("app", APP_IDENTIFIER)
        root.put("backupVersion", payload.backupVersion)
        root.put("createdAt", payload.createdAt)

        // 1. Settings
        val s = payload.settings
        val settingsObj = JSONObject().apply {
            put("autoReadInBackground", s.autoReadInBackground)
            put("speechRate", s.speechRate.toDouble())
            put("ttsEngine", s.ttsEngine)
            put("ttsLanguage", s.ttsLanguage)
            put("ttsVoice", s.ttsVoice)
            put("ttsPitch", s.ttsPitch.toDouble())
            put("ttsVolume", s.ttsVolume.toDouble())
            put("announceChunkNumbers", s.announceChunkNumbers)
            put("speechFilterMode", s.speechFilterMode.name)
            put("clipboardHistoryEnabled", s.clipboardHistoryEnabled)
            put("hapticFeedbackEnabled", s.hapticFeedbackEnabled)
            put("autoClipboard", s.autoClipboard)
            put("highContrast", s.highContrast)
            put("fontSize", s.fontSize.toDouble())
            put("darkTheme", s.darkTheme)
        }
        root.put("settings", settingsObj)

        // 2. Favorite Folders
        val folderNameById = payload.folders.associate { it.id to it.name }
        val foldersArr = JSONArray()
        for (folder in payload.folders) {
            val fObj = JSONObject().apply {
                put("id", folder.id)
                put("name", folder.name)
                put("createdAt", folder.createdAt)
            }
            foldersArr.put(fObj)
        }
        root.put("folders", foldersArr)

        // 3. Clipboard & Favorite Items
        val itemsArr = JSONArray()
        payload.items.forEachIndexed { idx, item ->
            val folderName = payload.folderNamesByItemIndex[idx]
                ?: item.folderId?.let { folderNameById[it] }
            val iObj = JSONObject().apply {
                put("id", item.id)
                put("text", item.text)
                put("timestamp", item.timestamp)
                put("isFavorite", item.isFavorite)
                put("isInHistory", item.isInHistory)
                if (item.folderId != null) {
                    put("folderId", item.folderId)
                } else {
                    put("folderId", JSONObject.NULL)
                }
                if (!folderName.isNullOrBlank()) {
                    put("folderName", folderName)
                }
            }
            itemsArr.put(iObj)
        }
        root.put("clipboardItems", itemsArr)

        // 4. Open Text Sessions
        val openSessionsObj = JSONObject()
        openSessionsObj.put("activeSessionIndex", payload.activeSessionIndex)
        val sessionsArr = JSONArray()
        for (session in payload.sessions) {
            if (session.sourceText.isNotBlank()) {
                sessionsArr.put(sessionToJson(session))
            }
        }
        openSessionsObj.put("sessions", sessionsArr)
        root.put("openSessions", openSessionsObj)

        return root.toString(2)
    }

    fun deserializeFromJson(jsonStr: String): Result<BackupPayload> {
        return try {
            val trimmed = jsonStr.trim()
            if (trimmed.isEmpty()) {
                return Result.failure(IllegalArgumentException("Empty backup file"))
            }
            val root = JSONObject(trimmed)
            val appId = root.optString("app", "")
            val hasRecognizedKeys = appId == APP_IDENTIFIER ||
                root.has("clipboardItems") ||
                root.has("folders") ||
                root.has("settings") ||
                root.has("openSessions")
            if (!hasRecognizedKeys) {
                return Result.failure(IllegalArgumentException("Invalid backup file format"))
            }

            val version = root.optInt("backupVersion", 1)
            val createdAt = root.optLong("createdAt", System.currentTimeMillis())

            // 1. Parse Settings
            val sObj = root.optJSONObject("settings")
            val settings = if (sObj != null) {
                val filterModeName = sObj.optString("speechFilterMode", SpeechFilterMode.Some.name)
                val filterMode = try {
                    SpeechFilterMode.valueOf(filterModeName)
                } catch (e: Exception) {
                    SpeechFilterMode.Some
                }
                BackupSettings(
                    autoReadInBackground = sObj.optBoolean("autoReadInBackground", false),
                    speechRate = sObj.optDouble("speechRate", 1.0).toFloat().coerceIn(0.5f, 2.5f),
                    ttsEngine = sObj.optString("ttsEngine", ""),
                    ttsLanguage = sObj.optString("ttsLanguage", "auto"),
                    ttsVoice = sObj.optString("ttsVoice", ""),
                    ttsPitch = sObj.optDouble("ttsPitch", 1.0).toFloat().coerceIn(0.5f, 2.0f),
                    ttsVolume = sObj.optDouble("ttsVolume", 1.0).toFloat().coerceIn(0.1f, 1.0f),
                    announceChunkNumbers = sObj.optBoolean("announceChunkNumbers", true),
                    speechFilterMode = filterMode,
                    clipboardHistoryEnabled = sObj.optBoolean("clipboardHistoryEnabled", true),
                    hapticFeedbackEnabled = sObj.optBoolean("hapticFeedbackEnabled", true),
                    autoClipboard = sObj.optBoolean("autoClipboard", true),
                    highContrast = sObj.optBoolean("highContrast", false),
                    fontSize = sObj.optDouble("fontSize", 18.0).toFloat().coerceIn(16f, 28f),
                    darkTheme = sObj.optBoolean("darkTheme", true),
                )
            } else {
                BackupSettings()
            }

            // 2. Parse Folders
            val folders = mutableListOf<FavoriteFolder>()
            val foldersArr = root.optJSONArray("folders")
            if (foldersArr != null) {
                for (i in 0 until foldersArr.length()) {
                    val fObj = foldersArr.optJSONObject(i) ?: continue
                    val name = fObj.optString("name", "").trim()
                    if (name.isNotEmpty()) {
                        folders.add(
                            FavoriteFolder(
                                id = fObj.optLong("id", (i + 1).toLong()),
                                name = name,
                                createdAt = fObj.optLong("createdAt", createdAt),
                            )
                        )
                    }
                }
            }

            // 3. Parse Clipboard & Favorite Items
            val items = mutableListOf<ClipboardItem>()
            val folderNamesByItemIndex = mutableMapOf<Int, String?>()
            val itemsArr = root.optJSONArray("clipboardItems")
            if (itemsArr != null) {
                for (i in 0 until itemsArr.length()) {
                    val iObj = itemsArr.optJSONObject(i) ?: continue
                    val text = iObj.optString("text", "").trim()
                    if (text.isEmpty()) continue
                    val folderId = if (iObj.has("folderId") && !iObj.isNull("folderId")) {
                        iObj.optLong("folderId")
                    } else {
                        null
                    }
                    val folderName = if (iObj.has("folderName") && !iObj.isNull("folderName")) {
                        iObj.optString("folderName", "").trim().ifEmpty { null }
                    } else {
                        null
                    }
                    val itemIdx = items.size
                    items.add(
                        ClipboardItem(
                            id = iObj.optLong("id", (itemIdx + 1).toLong()),
                            text = text,
                            timestamp = iObj.optLong("timestamp", createdAt),
                            isFavorite = iObj.optBoolean("isFavorite", false),
                            isInHistory = iObj.optBoolean("isInHistory", true),
                            folderId = folderId,
                        )
                    )
                    if (folderName != null) {
                        folderNamesByItemIndex[itemIdx] = folderName
                    }
                }
            }

            // 4. Parse Open Sessions
            val openSessionsObj = root.optJSONObject("openSessions")
            val sessions = mutableListOf<TextSession>()
            var activeSessionIndex = 0
            if (openSessionsObj != null) {
                activeSessionIndex = openSessionsObj.optInt("activeSessionIndex", 0)
                val sessionsArr = openSessionsObj.optJSONArray("sessions")
                if (sessionsArr != null) {
                    for (i in 0 until sessionsArr.length()) {
                        val sItemObj = sessionsArr.optJSONObject(i) ?: continue
                        val parsed = jsonToSession(sItemObj)
                        if (parsed.sourceText.isNotBlank()) {
                            sessions.add(parsed)
                        }
                    }
                }
            }
            val safeActiveIdx = if (sessions.isEmpty()) 0 else activeSessionIndex.coerceIn(0, sessions.lastIndex)

            Result.success(
                BackupPayload(
                    backupVersion = version,
                    createdAt = createdAt,
                    settings = settings,
                    folders = folders,
                    items = items,
                    folderNamesByItemIndex = folderNamesByItemIndex,
                    sessions = sessions,
                    activeSessionIndex = safeActiveIdx,
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun saveInternalLatestBackup(context: Context, jsonContent: String, createdAt: Long) {
        try {
            val dir = File(context.filesDir, "backups")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, LATEST_BACKUP_FILENAME)
            file.writeText(jsonContent, Charsets.UTF_8)
            KtsPreferences.setLastBackupTime(context, createdAt)
        } catch (e: Exception) {
            // Ignore internal mirror failure
        }
    }

    fun hasLocalLatestBackup(context: Context): Boolean {
        val file = File(File(context.filesDir, "backups"), LATEST_BACKUP_FILENAME)
        return file.exists() && file.length() > 0L
    }

    fun readLocalLatestBackup(context: Context): Result<BackupPayload> {
        return try {
            val file = File(File(context.filesDir, "backups"), LATEST_BACKUP_FILENAME)
            if (!file.exists()) {
                return Result.failure(IllegalStateException("No local backup found"))
            }
            val json = file.readText(Charsets.UTF_8)
            deserializeFromJson(json)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun saveBackupToDownloads(context: Context, payload: BackupPayload): Result<String> {
        return try {
            val jsonContent = serializeToJson(payload)
            val fileName = defaultBackupFileName(payload.createdAt)
            saveInternalLatestBackup(context, jsonContent, payload.createdAt)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/json")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return Result.failure(IllegalStateException("Could not create file in Downloads"))
                resolver.openOutputStream(uri)?.use { out ->
                    out.write(jsonContent.toByteArray(Charsets.UTF_8))
                    out.flush()
                } ?: return Result.failure(IllegalStateException("Could not open output stream"))
                Result.success(fileName)
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val outFile = File(downloadsDir, fileName)
                outFile.writeText(jsonContent, Charsets.UTF_8)
                Result.success(fileName)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun writeBackupToUri(context: Context, uri: Uri, payload: BackupPayload): Result<Unit> {
        return try {
            val jsonContent = serializeToJson(payload)
            saveInternalLatestBackup(context, jsonContent, payload.createdAt)
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(jsonContent.toByteArray(Charsets.UTF_8))
                out.flush()
            } ?: return Result.failure(IllegalStateException("Could not open URI for writing"))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun readBackupFromUri(context: Context, uri: Uri): Result<BackupPayload> {
        return try {
            val jsonContent = context.contentResolver.openInputStream(uri)?.use { input ->
                input.bufferedReader(Charsets.UTF_8).readText()
            } ?: return Result.failure(IllegalStateException("Could not read backup file"))
            deserializeFromJson(jsonContent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun createShareBackupIntent(context: Context, payload: BackupPayload, chooserTitle: String): Result<Intent> {
        return try {
            val jsonContent = serializeToJson(payload)
            saveInternalLatestBackup(context, jsonContent, payload.createdAt)
            val cacheDir = File(context.cacheDir, "backups")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val fileName = defaultBackupFileName(payload.createdAt)
            val file = File(cacheDir, fileName)
            file.writeText(jsonContent, Charsets.UTF_8)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, fileName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            Result.success(Intent.createChooser(shareIntent, chooserTitle))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
