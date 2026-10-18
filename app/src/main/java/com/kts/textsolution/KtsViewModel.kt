package com.kts.textsolution

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kts.textsolution.data.BackupManager
import com.kts.textsolution.data.BackupPayload
import com.kts.textsolution.data.Chunk
import com.kts.textsolution.data.ClipboardItem
import com.kts.textsolution.data.ClipboardRepository
import com.kts.textsolution.data.FavoriteFolder
import com.kts.textsolution.data.KtsPreferences
import com.kts.textsolution.data.SpeechFilterMode
import com.kts.textsolution.data.SplitMode
import com.kts.textsolution.data.TextSession
import com.kts.textsolution.data.TextSplitter
import com.kts.textsolution.data.TtsEngineInfo
import com.kts.textsolution.data.TtsVoiceInfo
import com.kts.textsolution.data.WebContentExtractor
import com.kts.textsolution.service.BackgroundTtsManager
import com.kts.textsolution.service.KtsAccessibilityService
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AppTab { Reading, Editing, Selection, History, Favorite, Settings, Help, About }

data class Snapshot(
    val sourceText: String,
    val chunks: List<Chunk>,
    val splitMode: SplitMode?,
    val selectedIds: Set<String>,
)

class KtsViewModel : ViewModel() {
    private var appContext: Context? = null

    var tab by mutableStateOf(AppTab.Reading)
    var previousTab by mutableStateOf(AppTab.Reading)
    var returnTabAfterOpen by mutableStateOf<AppTab?>(null)

    // Multi-text Sessions
    var sessions by mutableStateOf<List<TextSession>>(emptyList())
    var activeSessionIndex by mutableIntStateOf(0)

    private fun persistSessions() {
        val ctx = appContext ?: return
        try {
            val json = BackupManager.serializeSessionsToJson(sessions, activeSessionIndex)
            KtsPreferences.setSavedSessionsJson(ctx, json)
        } catch (_: Exception) {
        }
    }

    fun saveActiveSessionState() {
        if (sourceText.isBlank() && sessions.isEmpty()) return
        val currentId = if (activeSessionIndex in sessions.indices) sessions[activeSessionIndex].id else java.util.UUID.randomUUID().toString()
        val updated = TextSession(
            id = currentId,
            sourceText = sourceText,
            chunks = chunks,
            splitMode = splitMode,
            selectedIds = selectedIds,
            rangeAnchorId = rangeAnchorId,
            focusedChunkIndex = focusedChunkIndex,
            editingSourceItemId = editingSourceItemId,
            returnTabAfterOpen = returnTabAfterOpen,
            history = history.toList(),
        )
        if (activeSessionIndex in sessions.indices) {
            val mutable = sessions.toMutableList()
            mutable[activeSessionIndex] = updated
            sessions = mutable
        } else if (sourceText.isNotBlank()) {
            sessions = sessions + updated
            activeSessionIndex = sessions.lastIndex
        }
        persistSessions()
    }

    private fun loadSessionState(index: Int) {
        if (index !in sessions.indices) return
        val session = sessions[index]
        activeSessionIndex = index
        sourceText = session.sourceText
        chunks = session.chunks
        splitMode = session.splitMode
        selectedIds = session.selectedIds
        rangeAnchorId = session.rangeAnchorId
        focusedChunkIndex = session.focusedChunkIndex
        editingSourceItemId = session.editingSourceItemId
        returnTabAfterOpen = session.returnTabAfterOpen
        history.clear()
        history.addAll(session.history)
        persistSessions()
    }

    fun switchToSession(index: Int, announceTemplate: ((Int, Int, String) -> String)? = null) {
        if (index !in sessions.indices) return
        if (index == activeSessionIndex) return
        if (isSpeaking) {
            BackgroundTtsManager.stop()
        }
        saveActiveSessionState()
        loadSessionState(index)
        if (announceTemplate != null) {
            announce = announceTemplate(activeSessionIndex + 1, sessions.size, sessions[activeSessionIndex].title)
        }
    }

    fun switchToNextSession(announceTemplate: (Int, Int, String) -> String): Boolean {
        saveActiveSessionState()
        if (sessions.size <= 1) return false
        val nextIndex = (activeSessionIndex + 1) % sessions.size
        switchToSession(nextIndex, announceTemplate)
        return true
    }

    fun switchToPrevSession(announceTemplate: (Int, Int, String) -> String): Boolean {
        saveActiveSessionState()
        if (sessions.size <= 1) return false
        val prevIndex = if (activeSessionIndex - 1 < 0) sessions.lastIndex else activeSessionIndex - 1
        switchToSession(prevIndex, announceTemplate)
        return true
    }

    fun closeSession(index: Int, announceTemplate: ((Int, Int, String) -> String)? = null) {
        saveActiveSessionState()
        if (index !in sessions.indices) return
        if (isSpeaking && index == activeSessionIndex) {
            BackgroundTtsManager.stop()
        }
        val mutable = sessions.toMutableList()
        mutable.removeAt(index)
        sessions = mutable

        if (sessions.isEmpty()) {
            activeSessionIndex = 0
            sourceText = ""
            chunks = emptyList()
            splitMode = null
            selectedIds = emptySet()
            rangeAnchorId = null
            focusedChunkIndex = 0
            editingSourceItemId = null
            returnTabAfterOpen = null
            history.clear()
            persistSessions()
        } else {
            val newIndex = when {
                index < activeSessionIndex -> activeSessionIndex - 1
                activeSessionIndex >= sessions.size -> sessions.lastIndex
                else -> activeSessionIndex
            }
            loadSessionState(newIndex)
            if (announceTemplate != null) {
                announce = announceTemplate(activeSessionIndex + 1, sessions.size, sessions[activeSessionIndex].title)
            }
        }
    }

    fun closeAllSessions(announceMessage: String? = null) {
        if (isSpeaking) {
            BackgroundTtsManager.stop()
        }
        sessions = emptyList()
        activeSessionIndex = 0
        sourceText = ""
        chunks = emptyList()
        splitMode = null
        selectedIds = emptySet()
        rangeAnchorId = null
        focusedChunkIndex = 0
        editingSourceItemId = null
        returnTabAfterOpen = null
        history.clear()
        persistSessions()
        if (announceMessage != null) {
            announce = announceMessage
        }
    }

    fun createNewText(announceMsg: String) {
        if (isSpeaking) {
            BackgroundTtsManager.stop()
        }
        saveActiveSessionState()
        val newSession = TextSession(
            id = java.util.UUID.randomUUID().toString(),
            sourceText = "",
            chunks = emptyList(),
            splitMode = null,
            selectedIds = emptySet(),
            rangeAnchorId = null,
            focusedChunkIndex = 0,
            editingSourceItemId = null,
            returnTabAfterOpen = null,
            history = emptyList(),
        )
        if (sourceText.isNotBlank() || chunks.isNotEmpty() || sessions.isEmpty()) {
            sessions = sessions + newSession
            activeSessionIndex = sessions.lastIndex
        }
        sourceText = ""
        chunks = emptyList()
        splitMode = null
        selectedIds = emptySet()
        rangeAnchorId = null
        focusedChunkIndex = 0
        editingSourceItemId = null
        returnTabAfterOpen = null
        history.clear()
        persistSessions()
        tab = AppTab.Editing
        announce = announceMsg
    }

    fun openSettings() {
        if (tab != AppTab.Settings) {
            previousTab = tab
            tab = AppTab.Settings
        }
    }

    fun closeSettings() {
        tab = previousTab
    }

    fun openHelp() {
        if (tab != AppTab.Help) {
            previousTab = tab
            tab = AppTab.Help
        }
    }

    fun closeHelp() {
        tab = previousTab
    }

    fun openAbout() {
        if (tab != AppTab.About) {
            previousTab = tab
            tab = AppTab.About
        }
    }

    fun closeAbout() {
        tab = previousTab
    }

    fun navigateBackFromOpenedSource(): Boolean {
        val target = returnTabAfterOpen ?: return false
        returnTabAfterOpen = null
        tab = target
        return true
    }
    var sourceText by mutableStateOf("")
    var chunks by mutableStateOf<List<Chunk>>(emptyList())
    var splitMode by mutableStateOf<SplitMode?>(null)
    var selectedIds by mutableStateOf<Set<String>>(emptySet())
    var rangeAnchorId by mutableStateOf<String?>(null)
    var lastImported by mutableStateOf("")
    var autoClipboard by mutableStateOf(true)
    var highContrast by mutableStateOf(false)
    var fontSize by mutableFloatStateOf(18f)
    var darkTheme by mutableStateOf(true)
    var announceTrigger by mutableIntStateOf(0)
        private set
    private var _announce by mutableStateOf("")
    var announce: String
        get() = _announce
        set(value) {
            _announce = value
            announceTrigger++
        }
    var focusedChunkIndex by mutableIntStateOf(0)
    var editingId by mutableStateOf<String?>(null)
    var windowId by mutableStateOf<String?>(null)
    var customWindowData by mutableStateOf<Pair<String, (String) -> Unit>?>(null)
    var sheetId by mutableStateOf<String?>(null)
    var selectionSheetId by mutableStateOf<String?>(null)
    var pendingClipboard by mutableStateOf<String?>(null)
    var isSpeaking by mutableStateOf(false)
    var speechRate by mutableFloatStateOf(1.0f)
    var showFindReplaceDialog by mutableStateOf(false)
    var findQuery by mutableStateOf("")
    var replaceQuery by mutableStateOf("")
    var showFormatDialog by mutableStateOf(false)
    var formatInfoChunk by mutableStateOf<Chunk?>(null)
    var formatInfoChunkIndex by mutableIntStateOf(0)
    var showFormatInfoDialog by mutableStateOf(false)
    var isLoadingWebPage by mutableStateOf(false)

    // Background Reading State
    var autoReadInBackground by mutableStateOf(false)
    var isAccessibilityEnabled by mutableStateOf(false)
    var isBatteryOptIgnored by mutableStateOf(false)
    var showAccessibilityDialog by mutableStateOf(false)

    // Advanced TTS Settings State
    var ttsEngines by mutableStateOf<List<TtsEngineInfo>>(emptyList())
    var selectedEngine by mutableStateOf("")
    var selectedTtsLanguage by mutableStateOf("auto")
    var availableTtsLanguages by mutableStateOf<List<Locale>>(emptyList())
    var availableVoices by mutableStateOf<List<TtsVoiceInfo>>(emptyList())
    var selectedVoice by mutableStateOf("")
    var pitch by mutableFloatStateOf(1.0f)
    var volume by mutableFloatStateOf(1.0f)
    var announceChunkNumbers by mutableStateOf(true)
    var speechFilterMode by mutableStateOf(SpeechFilterMode.Some)

    // Clipboard History & Favorites State
    var clipboardHistoryEnabled by mutableStateOf(true)
    var clipboardHistoryItems by mutableStateOf<List<ClipboardItem>>(emptyList())
    var favoriteItems by mutableStateOf<List<ClipboardItem>>(emptyList())
    var favoriteFolders by mutableStateOf<List<FavoriteFolder>>(emptyList())
    var currentFolder by mutableStateOf<FavoriteFolder?>(null) // null = Root view, non-null = Inside that folder
    var showCreateFolderDialog by mutableStateOf(false)
    var folderToManage by mutableStateOf<FavoriteFolder?>(null)
    var showRenameFolderDialog by mutableStateOf(false)
    var showDeleteFolderDialog by mutableStateOf(false)
    var itemToMove by mutableStateOf<ClipboardItem?>(null)
    var itemToCopy by mutableStateOf<ClipboardItem?>(null)
    var showMainFavoriteFolderDialog by mutableStateOf(false)

    val rootFavoriteItems: List<ClipboardItem>
        get() = favoriteItems.filter { it.folderId == null }

    val currentFolderItems: List<ClipboardItem>
        get() = currentFolder?.let { f -> favoriteItems.filter { it.folderId == f.id } } ?: emptyList()

    var activeClipboardSheetItem by mutableStateOf<ClipboardItem?>(null)
    var editingClipboardItem by mutableStateOf<ClipboardItem?>(null)
    var editingSourceItemId by mutableStateOf<Long?>(null)
    var showClearHistoryDialog by mutableStateOf(false)
    var hapticFeedbackEnabled by mutableStateOf(true)

    // Backup & Restore State
    var lastBackupTimestamp by mutableStateOf(0L)
    var hasLocalBackup by mutableStateOf(false)
    var pendingRestorePayload by mutableStateOf<BackupPayload?>(null)

    fun initSettings(context: Context) {
        appContext = context.applicationContext
        autoReadInBackground = KtsPreferences.isAutoReadBackgroundEnabled(context)
        speechRate = KtsPreferences.getSpeechRate(context)
        pitch = KtsPreferences.getTtsPitch(context)
        volume = KtsPreferences.getTtsVolume(context)
        selectedEngine = KtsPreferences.getTtsEngine(context)
        selectedTtsLanguage = KtsPreferences.getTtsLanguage(context)
        selectedVoice = KtsPreferences.getTtsVoice(context)
        announceChunkNumbers = KtsPreferences.isAnnounceChunkNumbersEnabled(context)
        speechFilterMode = KtsPreferences.getSpeechFilterMode(context)
        clipboardHistoryEnabled = KtsPreferences.isClipboardHistoryEnabled(context)
        hapticFeedbackEnabled = KtsPreferences.isHapticFeedbackEnabled(context)
        autoClipboard = KtsPreferences.isAutoClipboardEnabled(context)
        highContrast = KtsPreferences.isHighContrastEnabled(context)
        fontSize = KtsPreferences.getFontSize(context)
        darkTheme = KtsPreferences.isDarkThemeEnabled(context)
        lastBackupTimestamp = KtsPreferences.getLastBackupTime(context)
        hasLocalBackup = BackupManager.hasLocalLatestBackup(context)
        lastImported = KtsPreferences.getLastImportedClip(context)

        if (sessions.isEmpty() && sourceText.isBlank()) {
            val savedSessionsJson = KtsPreferences.getSavedSessionsJson(context)
            if (savedSessionsJson.isNotBlank()) {
                val (restoredSessions, restoredIdx) = BackupManager.deserializeSessionsFromJson(savedSessionsJson)
                if (restoredSessions.isNotEmpty()) {
                    sessions = restoredSessions
                    loadSessionState(restoredIdx)
                }
            }
        }

        refreshPermissionStates(context)
        refreshTtsEnginesAndVoices(context)
        initClipboardObserver(context)
    }

    fun updateAutoClipboard(context: Context, enabled: Boolean) {
        autoClipboard = enabled
        KtsPreferences.setAutoClipboardEnabled(context, enabled)
    }

    fun updateHighContrast(context: Context, enabled: Boolean) {
        highContrast = enabled
        KtsPreferences.setHighContrastEnabled(context, enabled)
    }

    fun updateFontSize(context: Context, size: Float) {
        fontSize = size
        KtsPreferences.setFontSize(context, size)
    }

    fun updateDarkTheme(context: Context, dark: Boolean) {
        darkTheme = dark
        KtsPreferences.setDarkThemeEnabled(context, dark)
    }

    fun setHapticFeedbackEnabled(context: Context, enabled: Boolean) {
        hapticFeedbackEnabled = enabled
        KtsPreferences.setHapticFeedbackEnabled(context, enabled)
    }

    fun initClipboardObserver(context: Context) {
        val repo = ClipboardRepository.getInstance(context)
        viewModelScope.launch {
            repo.historyFlow.collect { items ->
                clipboardHistoryItems = items
            }
        }
        viewModelScope.launch {
            repo.favoritesFlow.collect { items ->
                favoriteItems = items
            }
        }
        viewModelScope.launch {
            repo.foldersFlow.collect { folders ->
                favoriteFolders = folders
                val active = currentFolder
                if (active != null) {
                    val updated = folders.find { it.id == active.id }
                    if (updated != null) {
                        currentFolder = updated
                    } else {
                        currentFolder = null
                    }
                }
            }
        }
    }

    fun openFolder(context: Context, folder: FavoriteFolder, openedMsg: String) {
        currentFolder = folder
        announce = String.format(Locale.getDefault(), openedMsg, folder.name)
    }

    fun closeFolder() {
        currentFolder = null
    }

    fun createFolder(context: Context, name: String, successMsg: String, existsMsg: String) {
        val repo = ClipboardRepository.getInstance(context)
        repo.createFolder(name) { success ->
            announce = if (success) successMsg else existsMsg
        }
        showCreateFolderDialog = false
    }

    fun renameFolder(context: Context, id: Long, newName: String, successMsg: String, existsMsg: String) {
        val repo = ClipboardRepository.getInstance(context)
        repo.renameFolder(id, newName) { success ->
            announce = if (success) successMsg else existsMsg
            if (success && currentFolder?.id == id) {
                currentFolder = currentFolder?.copy(name = newName.trim())
            }
        }
        showRenameFolderDialog = false
        folderToManage = null
    }

    fun deleteFolder(context: Context, id: Long, successMsg: String) {
        val repo = ClipboardRepository.getInstance(context)
        repo.deleteFolder(id)
        if (currentFolder?.id == id) {
            currentFolder = null
        }
        announce = successMsg
        showDeleteFolderDialog = false
        folderToManage = null
    }

    fun moveItemToFolder(context: Context, item: ClipboardItem, targetFolderId: Long?, folderName: String, movedMsg: String) {
        val repo = ClipboardRepository.getInstance(context)
        repo.moveItemToFolder(item.id, targetFolderId)
        itemToMove = null
        activeClipboardSheetItem = null
        announce = String.format(Locale.getDefault(), movedMsg, folderName)
    }

    fun copyItemToFolder(context: Context, item: ClipboardItem, targetFolderId: Long?, folderName: String, copiedMsg: String) {
        val repo = ClipboardRepository.getInstance(context)
        repo.copyItemToFolder(item, targetFolderId)
        itemToCopy = null
        activeClipboardSheetItem = null
        announce = String.format(Locale.getDefault(), copiedMsg, folderName)
    }

    fun setClipboardHistoryEnabled(context: Context, enabled: Boolean) {
        clipboardHistoryEnabled = enabled
        KtsPreferences.setClipboardHistoryEnabled(context, enabled)
    }

    fun toggleHistoryFavorite(context: Context, item: ClipboardItem, addedMsg: String, removedMsg: String) {
        val repo = ClipboardRepository.getInstance(context)
        repo.toggleFavorite(item)
        announce = if (!item.isFavorite) addedMsg else removedMsg
    }

    fun removeFromHistory(context: Context, item: ClipboardItem, deletedMsg: String) {
        val repo = ClipboardRepository.getInstance(context)
        repo.removeFromHistory(item.id)
        if (activeClipboardSheetItem?.id == item.id) {
            activeClipboardSheetItem = null
        }
        announce = deletedMsg
    }

    fun deleteHistoryItem(context: Context, item: ClipboardItem, deletedMsg: String) {
        val repo = ClipboardRepository.getInstance(context)
        if (tab == AppTab.History) {
            repo.removeFromHistory(item.id)
        } else {
            repo.deleteItem(item.id)
        }
        if (activeClipboardSheetItem?.id == item.id) {
            activeClipboardSheetItem = null
        }
        announce = deletedMsg
    }

    fun deleteCompletely(context: Context, item: ClipboardItem, deletedMsg: String) {
        val repo = ClipboardRepository.getInstance(context)
        repo.deleteItem(item.id)
        if (activeClipboardSheetItem?.id == item.id) {
            activeClipboardSheetItem = null
        }
        announce = deletedMsg
    }

    fun updateClipboardItemText(context: Context, id: Long, newText: String) {
        val trimmed = newText.trim()
        val repo = ClipboardRepository.getInstance(context)
        repo.updateItemText(id, trimmed)
        if (editingSourceItemId == id) {
            sourceText = trimmed
            chunks = emptyList()
            splitMode = null
            saveActiveSessionState()
        }
        val matchIdx = sessions.indexOfFirst { it.editingSourceItemId == id }
        if (matchIdx >= 0 && matchIdx != activeSessionIndex) {
            val mutable = sessions.toMutableList()
            mutable[matchIdx] = mutable[matchIdx].copy(sourceText = trimmed, chunks = emptyList(), splitMode = null)
            sessions = mutable
        }
    }

    fun clearHistory(context: Context) {
        val repo = ClipboardRepository.getInstance(context)
        repo.clearHistory(keepFavorites = true)
        showClearHistoryDialog = false
    }

    fun openInReadingMode(item: ClipboardItem, message: String) {
        if (tab == AppTab.History || tab == AppTab.Favorite) {
            returnTabAfterOpen = tab
        }
        importText(item.text, fromClipboard = false, message = message, sourceItemId = item.id)
        editingSourceItemId = item.id
        if (chunks.isEmpty() && sourceText.isNotBlank()) {
            split(splitMode ?: SplitMode.Paragraph, "", "")
        }
        activeClipboardSheetItem = null
        tab = AppTab.Reading
    }

    fun openInEditingMode(item: ClipboardItem, message: String) {
        if (tab == AppTab.History || tab == AppTab.Favorite) {
            returnTabAfterOpen = tab
        }
        importText(item.text, fromClipboard = false, message = message, sourceItemId = item.id)
        editingSourceItemId = item.id
        activeClipboardSheetItem = null
        tab = AppTab.Editing
    }

    fun saveDirectClipboardEdit(context: Context, id: Long, newText: String, savedMsg: String) {
        val trimmed = newText.trim()
        if (trimmed.isNotEmpty()) {
            val repo = ClipboardRepository.getInstance(context)
            repo.updateItemText(id, trimmed)
            if (editingSourceItemId == id) {
                sourceText = trimmed
                chunks = emptyList()
                splitMode = null
                saveActiveSessionState()
            }
            val matchIdx = sessions.indexOfFirst { it.editingSourceItemId == id }
            if (matchIdx >= 0 && matchIdx != activeSessionIndex) {
                val mutable = sessions.toMutableList()
                mutable[matchIdx] = mutable[matchIdx].copy(sourceText = trimmed, chunks = emptyList(), splitMode = null)
                sessions = mutable
            }
        }
        editingClipboardItem = null
        announce = savedMsg
    }

    fun currentTargetText(): String {
        return if (tab == AppTab.Selection && selectedIds.isNotEmpty()) {
            TextSplitter.join(chunks.filter { it.id in selectedIds }, splitMode ?: SplitMode.Paragraph).trim()
        } else {
            currentText().trim()
        }
    }

    val isCurrentTextFavorite: Boolean
        get() {
            val target = currentTargetText()
            if (target.isEmpty()) return false
            val sourceId = editingSourceItemId
            if (sourceId != null && (tab != AppTab.Selection || selectedIds.isEmpty())) {
                if (favoriteItems.any { it.id == sourceId }) return true
            }
            return favoriteItems.any { it.text == target }
        }

    fun saveCurrentEditingText(context: Context, savedMsg: String) {
        val textToSave = currentTargetText()
        if (textToSave.isEmpty()) return
        val repo = ClipboardRepository.getInstance(context)
        val isPartialSelection = tab == AppTab.Selection && selectedIds.isNotEmpty()
        val isFav = isCurrentTextFavorite
        var sourceId = if (isPartialSelection) null else editingSourceItemId
        if (sourceId == null && !isPartialSelection) {
            sourceId = favoriteItems.find { it.text.trim() == textToSave.trim() }?.id
                ?: clipboardHistoryItems.find { it.text.trim() == textToSave.trim() }?.id
        }
        val targetFavStatus = if (isFav) true else null
        repo.saveOrUpdateText(text = textToSave, existingId = sourceId, isFavorite = targetFavStatus) { newId ->
            if (!isPartialSelection) {
                editingSourceItemId = newId
            }
        }
        announce = savedMsg
    }

    fun toggleCurrentTextFavorite(context: Context, addedMsg: String, removedMsg: String) {
        val textToSave = currentTargetText()
        if (textToSave.isEmpty()) return
        val repo = ClipboardRepository.getInstance(context)
        val isFav = isCurrentTextFavorite
        val isPartialSelection = tab == AppTab.Selection && selectedIds.isNotEmpty()
        val sourceId = if (isPartialSelection) null else editingSourceItemId

        if (isFav) {
            val favItem = if (sourceId != null) {
                favoriteItems.find { it.id == sourceId } ?: favoriteItems.find { it.text == textToSave }
            } else {
                favoriteItems.find { it.text == textToSave }
            }
            if (favItem != null) {
                repo.setFavorite(favItem.id, false)
            }
            announce = removedMsg
        } else {
            repo.saveOrUpdateText(text = textToSave, existingId = sourceId, isFavorite = true) { newId ->
                if (!isPartialSelection) {
                    editingSourceItemId = newId
                }
            }
            announce = addedMsg
        }
    }

    fun saveCurrentTextToFolder(context: Context, targetFolderId: Long?, folderName: String, msg: String) {
        val textToSave = currentTargetText()
        if (textToSave.isEmpty()) return
        val repo = ClipboardRepository.getInstance(context)
        val isPartialSelection = tab == AppTab.Selection && selectedIds.isNotEmpty()
        val sourceId = if (isPartialSelection) null else editingSourceItemId

        repo.saveOrUpdateText(
            text = textToSave,
            existingId = sourceId,
            isFavorite = true,
            setFolderId = true,
            folderId = targetFolderId,
        ) { newId ->
            if (!isPartialSelection) {
                editingSourceItemId = newId
            }
        }
        showMainFavoriteFolderDialog = false
        announce = msg
    }

    fun openInSelectionMode(item: ClipboardItem, message: String) {
        if (tab == AppTab.History || tab == AppTab.Favorite) {
            returnTabAfterOpen = tab
        }
        importText(item.text, fromClipboard = false, message = message, sourceItemId = item.id)
        editingSourceItemId = item.id
        split(SplitMode.Paragraph, "", "")
        activeClipboardSheetItem = null
        tab = AppTab.Selection
        announce = message
    }

    fun setAnnounceChunkNumbers(context: Context, enabled: Boolean) {
        announceChunkNumbers = enabled
        KtsPreferences.setAnnounceChunkNumbersEnabled(context, enabled)
    }

    fun setSpeechFilterMode(context: Context, mode: SpeechFilterMode) {
        speechFilterMode = mode
        BackgroundTtsManager.updateFilterMode(context, mode)
    }

    fun refreshTtsEnginesAndVoices(context: Context) {
        viewModelScope.launch {
            ttsEngines = BackgroundTtsManager.getInstalledEngines()
            availableTtsLanguages = BackgroundTtsManager.getAvailableLanguages()
            val loc = when {
                selectedTtsLanguage.isBlank() || selectedTtsLanguage == "auto" -> null
                selectedTtsLanguage == "en" -> Locale.ENGLISH
                else -> Locale(selectedTtsLanguage)
            }
            availableVoices = BackgroundTtsManager.getAvailableVoices(loc)
        }
    }

    fun selectEngine(context: Context, enginePkg: String) {
        selectedEngine = enginePkg
        selectedVoice = ""
        BackgroundTtsManager.switchEngine(context, enginePkg) { success ->
            if (success) {
                refreshTtsEnginesAndVoices(context)
            }
        }
    }

    fun selectTtsLanguage(context: Context, lang: String) {
        selectedTtsLanguage = lang
        selectedVoice = ""
        BackgroundTtsManager.updateLanguage(context, lang)
        val loc = when {
            lang.isBlank() || lang == "auto" -> null
            lang == "en" -> Locale.ENGLISH
            else -> Locale(lang)
        }
        availableVoices = BackgroundTtsManager.getAvailableVoices(loc)
    }

    fun selectVoice(context: Context, voiceName: String) {
        selectedVoice = voiceName
        BackgroundTtsManager.updateVoice(context, voiceName)
    }

    fun updatePitch(context: Context, newPitch: Float) {
        pitch = newPitch
        BackgroundTtsManager.updatePitch(context, newPitch)
    }

    fun updateVolume(context: Context, newVolume: Float) {
        volume = newVolume
        BackgroundTtsManager.updateVolume(context, newVolume)
    }

    fun toggleTestVoice(context: Context, sampleText: String) {
        if (isSpeaking) {
            BackgroundTtsManager.stop()
        } else {
            BackgroundTtsManager.speak(sampleText, "TestVoice")
        }
    }

    fun refreshPermissionStates(context: Context) {
        isAccessibilityEnabled = KtsPreferences.isAccessibilityServiceEnabled(
            context,
            KtsAccessibilityService::class.java,
        )
        isBatteryOptIgnored = KtsPreferences.isBatteryOptimizationIgnored(context)
    }

    fun setAutoReadBackground(
        context: Context,
        enabled: Boolean,
        onStartService: () -> Unit,
        onStopService: () -> Unit,
    ) {
        autoReadInBackground = enabled
        KtsPreferences.setAutoReadBackgroundEnabled(context, enabled)
        if (enabled) {
            onStartService()
            refreshPermissionStates(context)
            if (!isAccessibilityEnabled) {
                showAccessibilityDialog = true
            }
        } else {
            onStopService()
        }
    }

    fun updateSpeechRate(context: Context, rate: Float) {
        speechRate = rate
        BackgroundTtsManager.updateSpeechRate(context, rate)
    }

    private val history = ArrayDeque<Snapshot>()

    fun currentText(): String = sourceText

    fun importText(text: String, fromClipboard: Boolean, message: String, sourceItemId: Long? = null) {
        if (text.isBlank()) return
        saveActiveSessionState()

        val trimmed = text.trim()
        val matchedFav = favoriteItems.find { it.text.trim() == trimmed }
        val matchedHistory = if (matchedFav == null) clipboardHistoryItems.find { it.text.trim() == trimmed } else null
        val resolvedSourceId = sourceItemId ?: matchedFav?.id ?: matchedHistory?.id

        // Check if this item or text is already open in one of the sessions
        val existingIndex = sessions.indexOfFirst {
            (resolvedSourceId != null && it.editingSourceItemId == resolvedSourceId) ||
                it.sourceText.trim() == trimmed
        }

        if (existingIndex >= 0) {
            loadSessionState(existingIndex)
            if (tab == AppTab.History || tab == AppTab.Favorite) {
                returnTabAfterOpen = tab
                saveActiveSessionState()
            }
        } else {
            if (sessions.size <= 1 && sourceText.isBlank()) {
                sourceText = text
                chunks = emptyList()
                focusedChunkIndex = 0
                splitMode = null
                selectedIds = emptySet()
                rangeAnchorId = null
                editingSourceItemId = resolvedSourceId
                history.clear()
                push()
                saveActiveSessionState()
            } else {
                val newSession = TextSession(
                    sourceText = text,
                    chunks = emptyList(),
                    splitMode = null,
                    selectedIds = emptySet(),
                    rangeAnchorId = null,
                    focusedChunkIndex = 0,
                    editingSourceItemId = resolvedSourceId,
                    returnTabAfterOpen = if (tab == AppTab.History || tab == AppTab.Favorite) tab else null,
                )
                sessions = sessions + newSession
                loadSessionState(sessions.lastIndex)
                push()
                saveActiveSessionState()
            }
        }

        pendingClipboard = null
        if (fromClipboard) {
            lastImported = text
            appContext?.let { KtsPreferences.setLastImportedClip(it, text) }
            returnTabAfterOpen = null
            saveActiveSessionState()
        }
        announce = if (sessions.size > 1 && activeSessionIndex in sessions.indices) {
            "$message. Text ${activeSessionIndex + 1} of ${sessions.size}: ${sessions[activeSessionIndex].title}"
        } else {
            message
        }
    }

    fun loadWebPage(url: String, loadingMsg: String, successMsg: String, errorMsg: String) {
        if (url.isBlank()) return
        viewModelScope.launch {
            isLoadingWebPage = true
            announce = loadingMsg
            val result = WebContentExtractor.extractText(url)
            isLoadingWebPage = false
            result.onSuccess { content ->
                importText(content, fromClipboard = true, message = successMsg)
            }.onFailure {
                importText(url, fromClipboard = true, message = errorMsg)
            }
        }
    }

    fun importOrFetch(
        text: String,
        fromClipboard: Boolean,
        importedMsg: String,
        loadingMsg: String,
        webSuccessMsg: String,
        webErrorMsg: String,
    ) {
        if (text.isBlank()) return
        if (WebContentExtractor.isUrl(text)) {
            loadWebPage(text, loadingMsg, webSuccessMsg, webErrorMsg)
        } else {
            importText(text, fromClipboard, importedMsg)
        }
    }

    fun considerClipboard(
        text: String,
        importedMsg: String,
        loadingMsg: String = "",
        webSuccessMsg: String = "",
        webErrorMsg: String = "",
    ) {
        if (text.isBlank() || text == lastImported) return
        if (sessions.any { it.sourceText.trim() == text.trim() }) {
            lastImported = text
            appContext?.let { KtsPreferences.setLastImportedClip(it, text) }
            return
        }
        if (tab == AppTab.Reading || tab == AppTab.Editing || tab == AppTab.Selection) {
            importOrFetch(text, true, importedMsg, loadingMsg, webSuccessMsg, webErrorMsg)
        }
    }

    fun split(mode: SplitMode, emptyMsg: String, doneMsg: String) {
        val base = currentText()
        if (base.isBlank()) {
            announce = emptyMsg
            return
        }
        push()
        chunks = TextSplitter.split(base, mode)
        focusedChunkIndex = 0
        splitMode = mode
        sourceText = base
        selectedIds = emptySet()
        rangeAnchorId = null
        saveActiveSessionState()
        announce = "$doneMsg. ${chunks.size}"
    }

    fun toggleSelect(id: String, selectedMsg: String? = null, deselectedMsg: String? = null) {
        val isNowSelected = id !in selectedIds
        selectedIds = if (isNowSelected) selectedIds + id else selectedIds - id
        saveActiveSessionState()
        if (isNowSelected && selectedMsg != null) {
            announce = selectedMsg
        } else if (!isNowSelected && deselectedMsg != null) {
            announce = deselectedMsg
        }
    }

    fun startRange(id: String, startMsg: String) {
        rangeAnchorId = id
        selectedIds = selectedIds + id
        saveActiveSessionState()
        announce = startMsg
    }

    fun finishRange(id: String, doneMsg: (Int) -> String) {
        val anchor = rangeAnchorId
        if (anchor == null) {
            startRange(id, "")
            return
        }
        val ids = TextSplitter.rangeIds(chunks, anchor, id)
        selectedIds = selectedIds + ids
        rangeAnchorId = null
        saveActiveSessionState()
        announce = doneMsg(ids.size)
    }

    fun rangeHold(id: String, startMsg: String, doneMsg: (Int) -> String) {
        val anchor = rangeAnchorId
        if (anchor == null || anchor == id) {
            startRange(id, startMsg)
            return
        }
        finishRange(id, doneMsg)
    }

    fun selectToStart(id: String, msg: String) {
        val ids = TextSplitter.idsToStart(chunks, id)
        selectedIds = selectedIds + ids
        rangeAnchorId = null
        saveActiveSessionState()
        announce = msg
    }

    fun selectToEnd(id: String, msg: String) {
        val ids = TextSplitter.idsToEnd(chunks, id)
        selectedIds = selectedIds + ids
        rangeAnchorId = null
        saveActiveSessionState()
        announce = msg
    }

    fun selectAll(msg: String) {
        selectedIds = chunks.map { it.id }.toSet()
        rangeAnchorId = null
        saveActiveSessionState()
        announce = msg
    }

    fun clearSelection(msg: String) {
        selectedIds = emptySet()
        rangeAnchorId = null
        saveActiveSessionState()
        announce = msg
    }

    fun saveEdit(id: String, text: String, savedMsg: String, context: Context? = null) {
        push()
        chunks = chunks.map { if (it.id == id) it.copy(text = text) else it }
        sourceText = TextSplitter.join(chunks, splitMode)
        editingId = null
        if (windowId == id) windowId = null
        sheetId = null
        editingSourceItemId?.let { sourceId ->
            if (context != null) {
                ClipboardRepository.getInstance(context).updateItemText(sourceId, sourceText)
            }
        }
        saveActiveSessionState()
        announce = savedMsg
    }

    fun deleteChunk(id: String, deletedMsg: String, context: Context? = null) {
        push()
        chunks = chunks.filterNot { it.id == id }
        if (focusedChunkIndex >= chunks.size) {
            focusedChunkIndex = (chunks.size - 1).coerceAtLeast(0)
        }
        sourceText = TextSplitter.join(chunks, splitMode)
        selectedIds = selectedIds - id
        sheetId = null
        if (editingId == id) editingId = null
        if (windowId == id) windowId = null
        editingSourceItemId?.let { sourceId ->
            if (context != null) {
                ClipboardRepository.getInstance(context).updateItemText(sourceId, sourceText)
            }
        }
        saveActiveSessionState()
        announce = deletedMsg
    }

    fun deleteSelected(deletedMsg: String) {
        if (selectedIds.isEmpty()) return
        push()
        val count = selectedIds.size
        chunks = chunks.filterNot { it.id in selectedIds }
        if (focusedChunkIndex >= chunks.size) {
            focusedChunkIndex = (chunks.size - 1).coerceAtLeast(0)
        }
        sourceText = TextSplitter.join(chunks, splitMode)
        selectedIds = emptySet()
        rangeAnchorId = null
        selectionSheetId = null
        saveActiveSessionState()
        announce = "$deletedMsg. $count"
    }

    fun formatSelectedChunks(
        fontSize: Float?,
        isBold: Boolean,
        isItalic: Boolean,
        isUnderline: Boolean,
        textColorHex: String? = null,
        alignment: String? = null,
        caseType: String? = "keep",
        listType: String? = "keep",
        formattedMsg: String,
    ) {
        if (selectedIds.isEmpty()) return
        push()
        var numberSeq = 1
        var textChanged = false
        chunks = chunks.map { chunk ->
            if (chunk.id in selectedIds) {
                var newText = chunk.text
                if (!caseType.isNullOrBlank() && caseType != "keep") {
                    newText = applyCaseChange(newText, caseType)
                    textChanged = true
                }
                if (!listType.isNullOrBlank() && listType != "keep") {
                    newText = applyListStyle(newText, listType, numberSeq++)
                    textChanged = true
                }
                chunk.copy(
                    text = newText,
                    fontSizeSp = fontSize,
                    isBold = isBold,
                    isItalic = isItalic,
                    isUnderline = isUnderline,
                    textColorHex = textColorHex,
                    alignment = alignment,
                )
            } else {
                chunk
            }
        }
        if (textChanged) {
            sourceText = TextSplitter.join(chunks, splitMode)
        }
        showFormatDialog = false
        selectionSheetId = null
        saveActiveSessionState()
        announce = formattedMsg
    }

    companion object {
        fun applyCaseChange(text: String, caseType: String): String {
            return when (caseType) {
                "upper" -> text.uppercase(Locale.getDefault())
                "lower" -> text.lowercase(Locale.getDefault())
                "title" -> {
                    text.split(Regex("(?<=\\s)|(?=\\s)")).joinToString("") { word ->
                        if (word.isBlank()) word
                        else word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    }
                }
                "sentence" -> {
                    val regex = Regex("(^|[.!?\\n]\\s*)([\\p{Ll}a-z])")
                    regex.replace(text) { matchResult ->
                        val prefix = matchResult.groupValues[1]
                        val char = matchResult.groupValues[2]
                        prefix + char.uppercase(Locale.getDefault())
                    }
                }
                else -> text
            }
        }

        fun stripListPrefix(text: String): String {
            return text.replace(Regex("^(\\s*)([•*\\-–—]\\s*|\\d+([.)]|\\s*-\\s*)\\s*)"), "$1")
        }

        fun applyListStyle(text: String, listType: String, index: Int): String {
            val stripped = stripListPrefix(text).trimStart()
            return when (listType) {
                "bullet" -> "• $stripped"
                "number" -> "$index. $stripped"
                "none" -> stripped
                else -> text
            }
        }
    }

    fun showFormatInfo(chunk: Chunk, index: Int = -1) {
        formatInfoChunk = chunk
        formatInfoChunkIndex = if (index >= 0) index else chunks.indexOfFirst { it.id == chunk.id }
        showFormatInfoDialog = true
    }

    fun dismissFormatInfo() {
        showFormatInfoDialog = false
        formatInfoChunk = null
    }

    fun cutCurrentOrSelected(context: Context, cutMsg: String): String {
        val textToCut = if (tab == AppTab.Selection && selectedIds.isNotEmpty()) {
            copyText()
        } else if (chunks.isNotEmpty() && focusedChunkIndex in chunks.indices) {
            chunks[focusedChunkIndex].text
        } else {
            currentTargetText()
        }
        if (textToCut.isBlank()) return ""

        push()
        if (tab == AppTab.Selection && selectedIds.isNotEmpty()) {
            deleteSelected(cutMsg)
        } else if (chunks.isNotEmpty() && focusedChunkIndex in chunks.indices) {
            deleteChunk(chunks[focusedChunkIndex].id, cutMsg, context)
        } else {
            sourceText = ""
            chunks = emptyList()
            selectedIds = emptySet()
            focusedChunkIndex = 0
            saveActiveSessionState()
            announce = cutMsg
        }
        return textToCut
    }

    fun deleteCurrentOrSelected(context: Context, deletedMsg: String) {
        if (tab == AppTab.Selection && selectedIds.isNotEmpty()) {
            deleteSelected(deletedMsg)
        } else if (chunks.isNotEmpty() && focusedChunkIndex in chunks.indices) {
            deleteChunk(chunks[focusedChunkIndex].id, deletedMsg, context)
        } else if (currentTargetText().isNotBlank()) {
            push()
            sourceText = ""
            chunks = emptyList()
            selectedIds = emptySet()
            focusedChunkIndex = 0
            saveActiveSessionState()
            announce = deletedMsg
        }
    }

    fun nextChunk(template: (Int, Int, String) -> String) {
        if (chunks.isEmpty()) {
            if (sourceText.isNotBlank()) {
                split(splitMode ?: SplitMode.Paragraph, "", "")
            } else {
                return
            }
        }
        if (focusedChunkIndex < chunks.size - 1) {
            focusedChunkIndex++
        }
        saveActiveSessionState()
        announceCurrentChunk(template)
    }

    fun prevChunk(template: (Int, Int, String) -> String) {
        if (chunks.isEmpty()) {
            if (sourceText.isNotBlank()) {
                split(splitMode ?: SplitMode.Paragraph, "", "")
            } else {
                return
            }
        }
        if (focusedChunkIndex > 0) {
            focusedChunkIndex--
        }
        saveActiveSessionState()
        announceCurrentChunk(template)
    }

    fun focusChunk(index: Int, template: (Int, Int, String) -> String) {
        if (index in chunks.indices) {
            focusedChunkIndex = index
            saveActiveSessionState()
            announceCurrentChunk(template)
        }
    }

    private fun announceCurrentChunk(template: (Int, Int, String) -> String) {
        if (focusedChunkIndex in chunks.indices) {
            val chunk = chunks[focusedChunkIndex]
            announce = if (announceChunkNumbers) {
                template(focusedChunkIndex + 1, chunks.size, chunk.text)
            } else {
                chunk.text
            }
        }
    }

    fun copyText(): String {
        if (tab == AppTab.Selection && selectedIds.isNotEmpty()) {
            return TextSplitter.join(chunks.filter { it.id in selectedIds }, splitMode ?: SplitMode.Paragraph)
        }
        return currentText()
    }

    fun textToPlay(): String = copyText()

    fun countOccurrences(find: String): Int {
        if (find.isEmpty()) return 0
        val text = currentText()
        var count = 0
        var index = 0
        while (true) {
            val found = text.indexOf(find, index, ignoreCase = true)
            if (found < 0) break
            count++
            index = found + find.length
        }
        return count
    }

    fun replaceNext(find: String, replacement: String, successMsg: String, notFoundMsg: String): Boolean {
        if (find.isEmpty()) return false
        val current = currentText()
        val index = current.indexOf(find, ignoreCase = true)
        if (index < 0) {
            announce = notFoundMsg
            return false
        }
        push()
        val newText = current.substring(0, index) + replacement + current.substring(index + find.length)
        if (chunks.isNotEmpty() && splitMode != null) {
            chunks = TextSplitter.split(newText, splitMode!!)
            sourceText = newText
        } else {
            sourceText = newText
            chunks = emptyList()
        }
        saveActiveSessionState()
        announce = successMsg
        return true
    }

    fun replaceAll(find: String, replacement: String, successMsg: (Int) -> String, notFoundMsg: String): Int {
        if (find.isEmpty()) return 0
        val count = countOccurrences(find)
        if (count == 0) {
            announce = notFoundMsg
            return 0
        }
        push()
        val current = currentText()
        val regex = Regex(Regex.escape(find), RegexOption.IGNORE_CASE)
        val newText = current.replace(regex, replacement)
        if (chunks.isNotEmpty() && splitMode != null) {
            chunks = TextSplitter.split(newText, splitMode!!)
            sourceText = newText
        } else {
            sourceText = newText
            chunks = emptyList()
        }
        saveActiveSessionState()
        announce = successMsg(count)
        return count
    }

    fun undo(nothing: String, done: String) {
        val prev = history.removeLastOrNull()
        if (prev == null) {
            announce = nothing
            return
        }
        sourceText = prev.sourceText
        chunks = prev.chunks
        splitMode = prev.splitMode
        selectedIds = prev.selectedIds
        saveActiveSessionState()
        announce = done
    }

    private fun push() {
        if (history.size > 19) history.removeFirst()
        history.addLast(Snapshot(sourceText, chunks, splitMode, selectedIds))
    }

    fun buildBackupPayload(context: Context): BackupPayload {
        saveActiveSessionState()
        return BackupManager.createBackupPayload(
            context = context,
            sessions = sessions,
            activeSessionIndex = activeSessionIndex,
            autoClipboard = autoClipboard,
            highContrast = highContrast,
            fontSize = fontSize,
            darkTheme = darkTheme,
        )
    }

    fun backupToDownloads(context: Context, successFormatMsg: String, errorMsg: String) {
        saveActiveSessionState()
        val sessionsSnapshot = sessions.toList()
        val activeIdxSnapshot = activeSessionIndex
        val autoClip = autoClipboard
        val hiContrast = highContrast
        val fSize = fontSize
        val dTheme = darkTheme

        viewModelScope.launch(Dispatchers.IO) {
            val payload = BackupManager.createBackupPayload(
                context = context,
                sessions = sessionsSnapshot,
                activeSessionIndex = activeIdxSnapshot,
                autoClipboard = autoClip,
                highContrast = hiContrast,
                fontSize = fSize,
                darkTheme = dTheme,
            )
            val result = BackupManager.saveBackupToDownloads(context, payload)
            withContext(Dispatchers.Main) {
                result.onSuccess { fileName ->
                    lastBackupTimestamp = payload.createdAt
                    hasLocalBackup = true
                    val msg = String.format(Locale.getDefault(), successFormatMsg, fileName)
                    announce = msg
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }.onFailure {
                    announce = errorMsg
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun backupToUri(context: Context, uri: Uri, successMsg: String, errorMsg: String) {
        saveActiveSessionState()
        val sessionsSnapshot = sessions.toList()
        val activeIdxSnapshot = activeSessionIndex
        val autoClip = autoClipboard
        val hiContrast = highContrast
        val fSize = fontSize
        val dTheme = darkTheme

        viewModelScope.launch(Dispatchers.IO) {
            val payload = BackupManager.createBackupPayload(
                context = context,
                sessions = sessionsSnapshot,
                activeSessionIndex = activeIdxSnapshot,
                autoClipboard = autoClip,
                highContrast = hiContrast,
                fontSize = fSize,
                darkTheme = dTheme,
            )
            val result = BackupManager.writeBackupToUri(context, uri, payload)
            withContext(Dispatchers.Main) {
                result.onSuccess {
                    lastBackupTimestamp = payload.createdAt
                    hasLocalBackup = true
                    announce = successMsg
                    Toast.makeText(context, successMsg, Toast.LENGTH_LONG).show()
                }.onFailure {
                    announce = errorMsg
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun shareBackup(context: Context, chooserTitle: String, errorMsg: String) {
        saveActiveSessionState()
        val sessionsSnapshot = sessions.toList()
        val activeIdxSnapshot = activeSessionIndex
        val autoClip = autoClipboard
        val hiContrast = highContrast
        val fSize = fontSize
        val dTheme = darkTheme

        viewModelScope.launch(Dispatchers.IO) {
            val payload = BackupManager.createBackupPayload(
                context = context,
                sessions = sessionsSnapshot,
                activeSessionIndex = activeIdxSnapshot,
                autoClipboard = autoClip,
                highContrast = hiContrast,
                fontSize = fSize,
                darkTheme = dTheme,
            )
            val result = BackupManager.createShareBackupIntent(context, payload, chooserTitle)
            withContext(Dispatchers.Main) {
                result.onSuccess { intent ->
                    lastBackupTimestamp = payload.createdAt
                    hasLocalBackup = true
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        announce = errorMsg
                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }.onFailure {
                    announce = errorMsg
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun loadBackupFromUriForRestore(context: Context, uri: Uri, invalidFileMsg: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = BackupManager.readBackupFromUri(context, uri)
            withContext(Dispatchers.Main) {
                result.onSuccess { payload ->
                    pendingRestorePayload = payload
                }.onFailure {
                    announce = invalidFileMsg
                    Toast.makeText(context, invalidFileMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun loadLatestLocalBackupForRestore(context: Context, invalidFileMsg: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = BackupManager.readLocalLatestBackup(context)
            withContext(Dispatchers.Main) {
                result.onSuccess { payload ->
                    pendingRestorePayload = payload
                }.onFailure {
                    announce = invalidFileMsg
                    Toast.makeText(context, invalidFileMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun applyRestore(
        context: Context,
        payload: BackupPayload,
        replaceExisting: Boolean,
        successMsg: String,
        errorMsg: String,
    ) {
        saveActiveSessionState()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val repo = ClipboardRepository.getInstance(context)
                repo.restoreFromBackup(
                    folders = payload.folders,
                    items = payload.items,
                    folderNamesByItemIndex = payload.folderNamesByItemIndex,
                    replaceExisting = replaceExisting,
                )

                val s = payload.settings
                KtsPreferences.setAutoReadBackgroundEnabled(context, s.autoReadInBackground)
                KtsPreferences.setSpeechRate(context, s.speechRate)
                KtsPreferences.setTtsEngine(context, s.ttsEngine)
                KtsPreferences.setTtsLanguage(context, s.ttsLanguage)
                KtsPreferences.setTtsVoice(context, s.ttsVoice)
                KtsPreferences.setTtsPitch(context, s.ttsPitch)
                KtsPreferences.setTtsVolume(context, s.ttsVolume)
                KtsPreferences.setAnnounceChunkNumbersEnabled(context, s.announceChunkNumbers)
                KtsPreferences.setSpeechFilterMode(context, s.speechFilterMode)
                KtsPreferences.setClipboardHistoryEnabled(context, s.clipboardHistoryEnabled)
                KtsPreferences.setHapticFeedbackEnabled(context, s.hapticFeedbackEnabled)
                KtsPreferences.setAutoClipboardEnabled(context, s.autoClipboard)
                KtsPreferences.setHighContrastEnabled(context, s.highContrast)
                KtsPreferences.setFontSize(context, s.fontSize)
                KtsPreferences.setDarkThemeEnabled(context, s.darkTheme)

                withContext(Dispatchers.Main) {
                    autoReadInBackground = s.autoReadInBackground
                    speechRate = s.speechRate
                    pitch = s.ttsPitch
                    volume = s.ttsVolume
                    selectedEngine = s.ttsEngine
                    selectedTtsLanguage = s.ttsLanguage
                    selectedVoice = s.ttsVoice
                    announceChunkNumbers = s.announceChunkNumbers
                    speechFilterMode = s.speechFilterMode
                    clipboardHistoryEnabled = s.clipboardHistoryEnabled
                    hapticFeedbackEnabled = s.hapticFeedbackEnabled
                    autoClipboard = s.autoClipboard
                    highContrast = s.highContrast
                    fontSize = s.fontSize
                    darkTheme = s.darkTheme

                    BackgroundTtsManager.updateSpeechRate(context, s.speechRate)
                    BackgroundTtsManager.updatePitch(context, s.ttsPitch)
                    BackgroundTtsManager.updateVolume(context, s.ttsVolume)
                    BackgroundTtsManager.updateFilterMode(context, s.speechFilterMode)
                    BackgroundTtsManager.updateLanguage(context, s.ttsLanguage)
                    if (s.ttsVoice.isNotBlank()) {
                        BackgroundTtsManager.updateVoice(context, s.ttsVoice)
                    }

                    if (replaceExisting) {
                        if (payload.sessions.isNotEmpty()) {
                            sessions = payload.sessions
                            loadSessionState(payload.activeSessionIndex)
                        } else {
                            sessions = emptyList()
                            activeSessionIndex = 0
                            sourceText = ""
                            chunks = emptyList()
                            splitMode = null
                            selectedIds = emptySet()
                            rangeAnchorId = null
                            focusedChunkIndex = 0
                            editingSourceItemId = null
                            returnTabAfterOpen = null
                            history.clear()
                            persistSessions()
                        }
                    } else if (payload.sessions.isNotEmpty()) {
                        val existingTexts = sessions.map { it.sourceText.trim() }.toMutableSet()
                        val merged = sessions.toMutableList()
                        for (restoredSession in payload.sessions) {
                            val trimmedText = restoredSession.sourceText.trim()
                            if (trimmedText.isNotEmpty() && trimmedText !in existingTexts) {
                                merged.add(restoredSession)
                                existingTexts.add(trimmedText)
                            }
                        }
                        val wasBlank = sourceText.isBlank()
                        sessions = merged
                        if (wasBlank && merged.isNotEmpty()) {
                            loadSessionState(0)
                        } else {
                            persistSessions()
                        }
                    }

                    pendingRestorePayload = null
                    announce = successMsg
                    Toast.makeText(context, successMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    pendingRestorePayload = null
                    announce = errorMsg
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

