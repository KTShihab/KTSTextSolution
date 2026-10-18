package com.kts.textsolution.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.ShortText
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.automirrored.outlined.Segment
import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.DriveFileMove
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.FindReplace
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatPaint
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import com.kts.textsolution.data.FavoriteFolder
import com.kts.textsolution.data.ClipboardRepository
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.kts.textsolution.util.KtsHaptics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.ViewCompat
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.FormatColorText
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.ui.zIndex
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import com.kts.textsolution.AppTab
import com.kts.textsolution.KtsViewModel
import com.kts.textsolution.R
import com.kts.textsolution.data.BackupManager
import com.kts.textsolution.data.Chunk
import com.kts.textsolution.data.ClipboardItem
import com.kts.textsolution.data.SpeechFilterMode
import com.kts.textsolution.data.SplitMode
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material.icons.outlined.SystemUpdate
import com.kts.textsolution.update.AppUpdateManager
import com.kts.textsolution.data.TextSplitter
import com.kts.textsolution.service.BackgroundTtsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KtsApp(
    vm: KtsViewModel,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onPlay: () -> Unit,
    onSpeakChunk: (String) -> Unit,
    onToggleBackgroundReading: (Boolean) -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onRequestIgnoreBatteryOptimization: () -> Unit,
) {
    val view = LocalView.current
    LaunchedEffect(vm.announce, vm.announceTrigger) {
        val textToSpeak = vm.announce
        if (textToSpeak.isNotBlank()) {
            ViewCompat.setAccessibilityPaneTitle(view, textToSpeak)
            delay(150)
            view.announceForAccessibility(textToSpeak)
        }
    }
    LaunchedEffect(vm.tab, vm.sourceText) {
        if (vm.tab == AppTab.Reading && vm.chunks.isEmpty() && vm.sourceText.isNotBlank()) {
            vm.split(vm.splitMode ?: SplitMode.Paragraph, "", "")
        }
    }
    val isSecondaryScreen = vm.tab == AppTab.Settings || vm.tab == AppTab.Help || vm.tab == AppTab.About
    val canReturnToOpenedSource = vm.returnTabAfterOpen != null &&
        (vm.tab == AppTab.Reading || vm.tab == AppTab.Editing || vm.tab == AppTab.Selection)

    BackHandler(enabled = isSecondaryScreen) {
        when (vm.tab) {
            AppTab.Settings -> vm.closeSettings()
            AppTab.Help -> vm.closeHelp()
            AppTab.About -> vm.closeAbout()
            else -> {}
        }
    }
    BackHandler(enabled = canReturnToOpenedSource) {
        vm.navigateBackFromOpenedSource()
    }
    BackHandler(
        enabled = !canReturnToOpenedSource &&
            !isSecondaryScreen &&
            vm.tab != AppTab.Reading &&
            !(vm.tab == AppTab.Favorite && vm.currentFolder != null),
    ) {
        vm.tab = AppTab.Reading
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    navigationIcon = {
                        if (isSecondaryScreen) {
                            IconButton(onClick = {
                                when (vm.tab) {
                                    AppTab.Settings -> vm.closeSettings()
                                    AppTab.Help -> vm.closeHelp()
                                    AppTab.About -> vm.closeAbout()
                                    else -> {}
                                }
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = stringResource(R.string.cancel),
                                )
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (canReturnToOpenedSource) {
                                    IconButton(onClick = {
                                        KtsHaptics.vibrateSelect(view.context)
                                        vm.navigateBackFromOpenedSource()
                                    }) {
                                        Icon(
                                            Icons.AutoMirrored.Outlined.ArrowBack,
                                            contentDescription = stringResource(
                                                if (vm.returnTabAfterOpen == AppTab.Favorite) R.string.action_back_to_favorites
                                                else R.string.tab_history,
                                            ),
                                        )
                                    }
                                }
                                MainNewTextButton(vm = vm)
                            }
                        }
                    },
                    title = {
                        Column {
                            Text("KTS", style = MaterialTheme.typography.labelSmall)
                            Text(
                                when (vm.tab) {
                                    AppTab.Settings -> stringResource(R.string.tab_settings)
                                    AppTab.Help -> stringResource(R.string.menu_help)
                                    AppTab.About -> stringResource(R.string.menu_about)
                                    else -> stringResource(R.string.app_name)
                                },
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    },
                    actions = {
                        when (vm.tab) {
                            AppTab.Reading -> {
                                MainFavoriteButton(vm = vm)
                                MainMoreOptionsMenu(vm = vm)
                            }
                            AppTab.Editing -> {
                                MainFavoriteButton(vm = vm)
                                IconButton(onClick = { vm.showFindReplaceDialog = true }) {
                                    Icon(
                                        Icons.Outlined.FindReplace,
                                        contentDescription = stringResource(R.string.action_find_replace),
                                    )
                                }
                                val undoDesc = stringResource(R.string.action_undo)
                                IconButton(onClick = {
                                    vm.undo("Nothing to undo", undoDesc)
                                }) {
                                    Icon(Icons.AutoMirrored.Outlined.Undo, contentDescription = undoDesc)
                                }
                                MainMoreOptionsMenu(vm = vm)
                            }
                            AppTab.Selection -> {
                                MainFavoriteButton(vm = vm)
                                MainMoreOptionsMenu(vm = vm)
                            }
                            AppTab.History -> {
                                MainMoreOptionsMenu(vm = vm)
                            }
                            AppTab.Favorite -> {
                                MainMoreOptionsMenu(vm = vm)
                            }
                            AppTab.Settings, AppTab.Help, AppTab.About -> {
                                // Secondary screens have no extra top bar actions
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
                if (!isSecondaryScreen) {
                    MainTabs(vm)
                }
            }
        },
        bottomBar = {
            if (!isSecondaryScreen && vm.tab != AppTab.History && vm.tab != AppTab.Favorite) {
                Column(Modifier.navigationBarsPadding()) {
                    if (vm.chunks.isNotEmpty()) {
                        ChunkNavigationBar(vm = vm)
                    }
                    if (vm.tab == AppTab.Reading) {
                        ReadingBottomActionBar(vm = vm, onPlay = onPlay)
                    } else {
                        TextBottomActionBar(vm = vm, onCopy = onCopy, onPaste = onPaste)
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    )
                    SplitBar(vm = vm)
                }
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            if (vm.isLoadingWebPage) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.loading_web_page),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
            when (vm.tab) {
                AppTab.Settings -> SettingsPane(
                    vm = vm,
                    onToggleBackgroundReading = onToggleBackgroundReading,
                    onOpenAccessibilitySettings = onOpenAccessibilitySettings,
                    onRequestIgnoreBatteryOptimization = onRequestIgnoreBatteryOptimization,
                )
                AppTab.Help -> HelpPane()
                AppTab.About -> AboutPane()
                AppTab.History -> HistoryPane(vm = vm)
                AppTab.Favorite -> FavoritePane(vm = vm)
                else -> {
                    if (vm.sessions.size > 1) {
                        OpenTextsBar(vm = vm)
                    }
                    val sessionSwitchTemplate: (Int, Int, String) -> String = { curr, total, title ->
                        view.context.getString(R.string.session_switched_announced, curr, total, title)
                    }
                    val accessibilityManager = remember {
                        view.context.getSystemService(android.content.Context.ACCESSIBILITY_SERVICE) as? android.view.accessibility.AccessibilityManager
                    }
                    val isTouchExploration = accessibilityManager?.isTouchExplorationEnabled == true
                    val nextTextLabel = stringResource(R.string.action_next_text)
                    val prevTextLabel = stringResource(R.string.action_prev_text)

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .twoFingerSwipe(
                                enabled = vm.sessions.size > 1,
                                isTouchExploration = isTouchExploration,
                                onSwipeLeft = {
                                    KtsHaptics.vibrateSelect(view.context)
                                    vm.switchToNextSession(sessionSwitchTemplate)
                                },
                                onSwipeRight = {
                                    KtsHaptics.vibrateSelect(view.context)
                                    vm.switchToPrevSession(sessionSwitchTemplate)
                                },
                            )
                            .semantics {
                                if (vm.sessions.size > 1) {
                                    customActions = listOf(
                                        CustomAccessibilityAction(nextTextLabel) {
                                            KtsHaptics.vibrateSelect(view.context)
                                            vm.switchToNextSession(sessionSwitchTemplate)
                                            true
                                        },
                                        CustomAccessibilityAction(prevTextLabel) {
                                            KtsHaptics.vibrateSelect(view.context)
                                            vm.switchToPrevSession(sessionSwitchTemplate)
                                            true
                                        },
                                    )
                                }
                            },
                    ) {
                        TextWorkspace(vm, onCopy)
                    }
                }
            }
        }
    }

    vm.sheetId?.let { id ->
        ActionSheet(vm, id)
    }
    vm.selectionSheetId?.let { id ->
        SelectionActionSheet(vm, id)
    }
    if (vm.showFindReplaceDialog) {
        FindReplaceDialog(vm = vm, onDismiss = { vm.showFindReplaceDialog = false })
    }
    if (vm.showFormatDialog) {
        FormatTextDialog(vm = vm, onDismiss = { vm.showFormatDialog = false })
    }
    if (vm.showFormatInfoDialog && vm.formatInfoChunk != null) {
        TextFormatInfoDialog(
            vm = vm,
            chunk = vm.formatInfoChunk!!,
            index = vm.formatInfoChunkIndex,
            onDismiss = { vm.dismissFormatInfo() },
        )
    }
    if (vm.showAccessibilityDialog) {
        AlertDialog(
            onDismissRequest = { vm.showAccessibilityDialog = false },
            title = { Text(stringResource(R.string.grant_accessibility_dialog_title)) },
            text = { Text(stringResource(R.string.grant_accessibility_dialog_msg)) },
            confirmButton = {
                Button(onClick = {
                    vm.showAccessibilityDialog = false
                    onOpenAccessibilitySettings()
                }) {
                    Text(stringResource(R.string.open_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.showAccessibilityDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
    vm.editingId?.let { id ->
        val chunk = vm.chunks.find { it.id == id } ?: return@let
        EditDialog(
            title = stringResource(R.string.edit),
            initial = chunk.text,
            fontSize = vm.fontSize,
            onSave = { vm.saveEdit(id, it, view.context.getString(R.string.save), view.context) },
            onDismiss = { vm.editingId = null },
            onOpenInWindow = { interimText ->
                vm.saveEdit(id, interimText, "", view.context)
                vm.editingId = null
                vm.windowId = id
            },
        )
    }
    vm.windowId?.let { id ->
        val chunk = vm.chunks.find { it.id == id } ?: return@let
        FullEditWindow(
            vm = vm,
            title = stringResource(R.string.open_window),
            initial = chunk.text,
            fontSize = vm.fontSize,
            announceChunkNumbers = vm.announceChunkNumbers,
            onSave = { vm.saveEdit(id, it, view.context.getString(R.string.save), view.context) },
            onDismiss = { vm.windowId = null },
        )
    }
    vm.customWindowData?.let { (text, onSaveCallback) ->
        FullEditWindow(
            vm = vm,
            title = stringResource(R.string.open_window),
            initial = text,
            fontSize = vm.fontSize,
            announceChunkNumbers = vm.announceChunkNumbers,
            onSave = {
                onSaveCallback(it)
                vm.customWindowData = null
            },
            onDismiss = { vm.customWindowData = null },
        )
    }
    vm.editingClipboardItem?.let { item ->
        val savedMsg = stringResource(R.string.saved_changes)
        EditDialog(
            title = stringResource(R.string.edit_item_title),
            initial = item.text,
            fontSize = vm.fontSize,
            onSave = { updatedText ->
                KtsHaptics.vibrateAction(view.context)
                vm.saveDirectClipboardEdit(view.context, item.id, updatedText, savedMsg)
                Toast.makeText(view.context, savedMsg, Toast.LENGTH_SHORT).show()
            },
            onDismiss = { vm.editingClipboardItem = null },
            onOpenInWindow = { currentTypedText ->
                val current = vm.editingClipboardItem
                vm.editingClipboardItem = null
                if (current != null) {
                    vm.customWindowData = Pair(currentTypedText) { updated ->
                        vm.updateClipboardItemText(view.context, current.id, updated)
                    }
                }
            },
        )
    }
    vm.activeClipboardSheetItem?.let { item ->
        HistoryActionSheet(vm = vm, item = item)
    }
    if (vm.showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { vm.showClearHistoryDialog = false },
            title = { Text(stringResource(R.string.clear_history_confirm_title)) },
            text = { Text(stringResource(R.string.clear_history_confirm_msg)) },
            confirmButton = {
                Button(
                    onClick = { vm.clearHistory(view.context) },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.showClearHistoryDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (vm.showCreateFolderDialog) {
        val successMsg = stringResource(R.string.folder_created_msg)
        val existsMsg = stringResource(R.string.folder_already_exists_msg)
        CreateFolderDialog(
            onConfirm = { folderName ->
                KtsHaptics.vibrateAction(view.context)
                vm.createFolder(view.context, folderName, successMsg, existsMsg)
            },
            onDismiss = { vm.showCreateFolderDialog = false },
        )
    }

    vm.folderToManage?.let { folder ->
        if (vm.showRenameFolderDialog) {
            val successMsg = stringResource(R.string.folder_renamed_msg)
            val existsMsg = stringResource(R.string.folder_already_exists_msg)
            RenameFolderDialog(
                folder = folder,
                onConfirm = { newName ->
                    KtsHaptics.vibrateAction(view.context)
                    vm.renameFolder(view.context, folder.id, newName, successMsg, existsMsg)
                },
                onDismiss = {
                    vm.showRenameFolderDialog = false
                    vm.folderToManage = null
                },
            )
        } else if (vm.showDeleteFolderDialog) {
            val successMsg = stringResource(R.string.folder_deleted_msg)
            DeleteFolderDialog(
                folder = folder,
                onConfirm = {
                    KtsHaptics.vibrateAction(view.context)
                    vm.deleteFolder(view.context, folder.id, successMsg)
                },
                onDismiss = {
                    vm.showDeleteFolderDialog = false
                    vm.folderToManage = null
                },
            )
        } else {
            FolderManageSheet(
                folder = folder,
                onRename = {
                    vm.showRenameFolderDialog = true
                },
                onDelete = {
                    vm.showDeleteFolderDialog = true
                },
                onDismiss = { vm.folderToManage = null },
            )
        }
    }

    vm.itemToMove?.let { item ->
        val movedMsg = stringResource(R.string.moved_to_folder_msg)
        FolderSelectionDialog(
            title = stringResource(R.string.action_move_to_folder),
            folders = vm.favoriteFolders,
            onSelectFolder = { targetFolderId, folderName ->
                KtsHaptics.vibrateAction(view.context)
                vm.moveItemToFolder(view.context, item, targetFolderId, folderName, movedMsg)
            },
            onCreateNewFolder = {
                vm.showCreateFolderDialog = true
            },
            onDismiss = { vm.itemToMove = null },
        )
    }

    vm.itemToCopy?.let { item ->
        val copiedMsg = stringResource(R.string.copied_to_folder_msg)
        FolderSelectionDialog(
            title = stringResource(R.string.action_copy_to_folder),
            folders = vm.favoriteFolders,
            onSelectFolder = { targetFolderId, folderName ->
                KtsHaptics.vibrateAction(view.context)
                vm.copyItemToFolder(view.context, item, targetFolderId, folderName, copiedMsg)
            },
            onCreateNewFolder = {
                vm.showCreateFolderDialog = true
            },
            onDismiss = { vm.itemToCopy = null },
        )
    }

    if (vm.showMainFavoriteFolderDialog) {
        FolderSelectionDialog(
            title = stringResource(R.string.dialog_select_folder_title),
            folders = vm.favoriteFolders,
            onSelectFolder = { targetFolderId, folderName ->
                KtsHaptics.vibrateAction(view.context)
                val msg = view.context.getString(R.string.moved_to_folder_msg, folderName)
                vm.saveCurrentTextToFolder(view.context, targetFolderId, folderName, msg)
            },
            onCreateNewFolder = {
                vm.showCreateFolderDialog = true
            },
            onDismiss = { vm.showMainFavoriteFolderDialog = false },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainFavoriteButton(vm: KtsViewModel) {
    val context = LocalView.current.context
    val isFav = vm.isCurrentTextFavorite
    val hasText = vm.currentTargetText().isNotBlank()
    val favDesc = stringResource(if (isFav) R.string.action_remove_favorite else R.string.action_add_favorite)
    val selectFolderLabel = stringResource(R.string.dialog_select_folder_title)
    val addedFavMsg = stringResource(R.string.added_to_favorites)
    val removedFavMsg = stringResource(R.string.removed_from_favorites)

    Box(
        modifier = Modifier
            .size(48.dp)
            .semantics(mergeDescendants = true) {
                if (vm.favoriteFolders.isNotEmpty()) {
                    customActions = listOf(
                        CustomAccessibilityAction(label = selectFolderLabel) {
                            KtsHaptics.vibrateLongPress(context)
                            vm.showMainFavoriteFolderDialog = true
                            true
                        }
                    )
                }
            }
            .combinedClickable(
                enabled = hasText,
                onClick = {
                    KtsHaptics.vibrateAction(context)
                    val willBeFav = !isFav
                    vm.toggleCurrentTextFavorite(context, addedFavMsg, removedFavMsg)
                    Toast.makeText(context, if (willBeFav) addedFavMsg else removedFavMsg, Toast.LENGTH_SHORT).show()
                },
                onLongClick = if (vm.favoriteFolders.isNotEmpty()) {
                    {
                        KtsHaptics.vibrateLongPress(context)
                        vm.showMainFavoriteFolderDialog = true
                    }
                } else null,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isFav) Icons.Outlined.Star else Icons.Outlined.StarBorder,
            contentDescription = favDesc,
            tint = if (hasText) {
                if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            },
        )
    }
}

@Composable
private fun MainNewTextButton(vm: KtsViewModel) {
    val context = LocalView.current.context
    val newCreatedMsg = stringResource(R.string.new_text_created)
    IconButton(
        onClick = {
            KtsHaptics.vibrateAction(context)
            vm.createNewText(newCreatedMsg)
        },
    ) {
        Icon(
            Icons.AutoMirrored.Outlined.NoteAdd,
            contentDescription = stringResource(R.string.action_new_text),
        )
    }
}

@Composable
private fun MainTabs(vm: KtsViewModel) {
    val context = LocalView.current.context
    val tabs = listOf(
        AppTab.Reading,
        AppTab.Editing,
        AppTab.Selection,
        AppTab.History,
        AppTab.Favorite,
    )
    val selectedIndex = tabs.indexOf(vm.tab).coerceAtLeast(0)
    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = 8.dp,
    ) {
        Tab(
            selected = vm.tab == AppTab.Reading,
            onClick = {
                KtsHaptics.vibrateSelect(context)
                vm.tab = AppTab.Reading
            },
            icon = { Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null) },
            text = { Text(stringResource(R.string.tab_reading)) },
        )
        Tab(
            selected = vm.tab == AppTab.Editing,
            onClick = {
                KtsHaptics.vibrateSelect(context)
                vm.tab = AppTab.Editing
            },
            icon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
            text = { Text(stringResource(R.string.tab_editing)) },
        )
        Tab(
            selected = vm.tab == AppTab.Selection,
            onClick = {
                KtsHaptics.vibrateSelect(context)
                vm.tab = AppTab.Selection
            },
            icon = { Icon(Icons.Outlined.CheckBox, contentDescription = null) },
            text = { Text(stringResource(R.string.tab_selection)) },
        )
        Tab(
            selected = vm.tab == AppTab.History,
            onClick = {
                KtsHaptics.vibrateSelect(context)
                vm.returnTabAfterOpen = null
                vm.tab = AppTab.History
            },
            icon = { Icon(Icons.Outlined.History, contentDescription = null) },
            text = { Text(stringResource(R.string.tab_history)) },
        )
        Tab(
            selected = vm.tab == AppTab.Favorite,
            onClick = {
                KtsHaptics.vibrateSelect(context)
                vm.returnTabAfterOpen = null
                vm.tab = AppTab.Favorite
            },
            icon = { Icon(Icons.Outlined.Star, contentDescription = null) },
            text = { Text(stringResource(R.string.tab_favorite)) },
        )
    }
}

@Composable
private fun ReadingBottomActionBar(
    vm: KtsViewModel,
    onPlay: () -> Unit,
) {
    val context = LocalView.current.context
    val template = stringResource(R.string.current_chunk_announced)
    val prevLabel = stringResource(R.string.action_previous_chunk)
    val nextLabel = stringResource(R.string.action_next_chunk)
    val playLabel = stringResource(R.string.action_play)
    val stopLabel = stringResource(R.string.action_stop)

    val hasText = vm.currentTargetText().isNotBlank() || vm.chunks.isNotEmpty()
    val hasChunks = vm.chunks.isNotEmpty()
    val canGoPrev = if (hasChunks) vm.focusedChunkIndex > 0 else vm.currentTargetText().isNotBlank()
    val canGoNext = if (hasChunks) vm.focusedChunkIndex < vm.chunks.size - 1 else vm.currentTargetText().isNotBlank()

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ActionBottomButton(
            label = prevLabel,
            icon = Icons.AutoMirrored.Outlined.ArrowBack,
            enabled = canGoPrev,
            onClick = {
                KtsHaptics.vibrateSelect(context)
                vm.prevChunk { index, total, text ->
                    String.format(java.util.Locale.getDefault(), template, index, total, text)
                }
            },
        )

        ActionBottomButton(
            label = if (vm.isSpeaking) stopLabel else playLabel,
            icon = if (vm.isSpeaking) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
            tint = if (vm.isSpeaking) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            enabled = hasText,
            onClick = {
                KtsHaptics.vibrateAction(context)
                onPlay()
            },
        )

        ActionBottomButton(
            label = nextLabel,
            icon = Icons.AutoMirrored.Outlined.ArrowForward,
            enabled = canGoNext,
            onClick = {
                KtsHaptics.vibrateSelect(context)
                vm.nextChunk { index, total, text ->
                    String.format(java.util.Locale.getDefault(), template, index, total, text)
                }
            },
        )
    }
}

@Composable
private fun TextBottomActionBar(
    vm: KtsViewModel,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
) {
    val context = LocalView.current.context
    val cutMsg = stringResource(R.string.cut)
    val deleteMsg = stringResource(R.string.delete)
    val emptyMsg = stringResource(R.string.empty_title)
    val savedChangesMsg = stringResource(R.string.saved_changes)

    val hasText = vm.currentTargetText().isNotBlank() || vm.chunks.isNotEmpty()
    val canCutOrDelete = if (vm.tab == AppTab.Selection) {
        vm.selectedIds.isNotEmpty()
    } else {
        hasText
    }
    val canCopy = if (vm.tab == AppTab.Selection) {
        vm.selectedIds.isNotEmpty()
    } else {
        vm.copyText().isNotBlank()
    }
    val canSave = vm.currentTargetText().isNotBlank()

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        ActionBottomButton(
            label = stringResource(R.string.action_cut),
            icon = Icons.Outlined.ContentCut,
            enabled = canCutOrDelete,
            onClick = {
                KtsHaptics.vibrateAction(context)
                val cutText = vm.cutCurrentOrSelected(context, cutMsg)
                if (cutText.isNotBlank()) {
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("KTS", cutText))
                    ClipboardRepository.getInstance(context).addOrUpdateClip(context, cutText)
                    Toast.makeText(context, cutMsg, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, emptyMsg, Toast.LENGTH_SHORT).show()
                }
            },
        )
        ActionBottomButton(
            label = stringResource(R.string.action_copy),
            icon = Icons.Outlined.ContentCopy,
            enabled = canCopy,
            onClick = {
                KtsHaptics.vibrateAction(context)
                onCopy()
            },
        )
        if (vm.tab == AppTab.Selection) {
            ActionBottomButton(
                label = stringResource(R.string.action_format),
                icon = Icons.Outlined.FormatPaint,
                enabled = vm.selectedIds.isNotEmpty(),
                tint = if (vm.selectedIds.isNotEmpty()) MaterialTheme.colorScheme.primary else null,
                onClick = {
                    KtsHaptics.vibrateSelect(context)
                    vm.showFormatDialog = true
                },
            )
        } else {
            ActionBottomButton(
                label = stringResource(R.string.action_paste),
                icon = Icons.Outlined.ContentPaste,
                enabled = true,
                onClick = {
                    KtsHaptics.vibrateAction(context)
                    onPaste()
                },
            )
        }
        ActionBottomButton(
            label = stringResource(R.string.delete),
            icon = Icons.Outlined.Delete,
            enabled = canCutOrDelete,
            tint = if (canCutOrDelete) MaterialTheme.colorScheme.error else null,
            onClick = {
                KtsHaptics.vibrateAction(context)
                vm.deleteCurrentOrSelected(context, deleteMsg)
                Toast.makeText(context, deleteMsg, Toast.LENGTH_SHORT).show()
            },
        )
        ActionBottomButton(
            label = stringResource(R.string.save),
            icon = Icons.Outlined.Save,
            enabled = canSave,
            tint = if (canSave) MaterialTheme.colorScheme.primary else null,
            onClick = {
                KtsHaptics.vibrateAction(context)
                vm.saveCurrentEditingText(context, savedChangesMsg)
                Toast.makeText(context, savedChangesMsg, Toast.LENGTH_SHORT).show()
            },
        )
    }
}

@Composable
private fun ActionBottomButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean = true,
    tint: androidx.compose.ui.graphics.Color? = null,
    onClick: () -> Unit,
) {
    val iconTint = if (!enabled) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    } else {
        tint ?: MaterialTheme.colorScheme.onSurface
    }
    val textColor = if (!enabled) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    } else {
        tint ?: MaterialTheme.colorScheme.onSurface
    }

    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.clearAndSetSemantics {
            contentDescription = label
            role = Role.Button
        },
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconTint,
            )
            Text(
                label,
                fontSize = 11.sp,
                color = textColor,
            )
        }
    }
}

@Composable
private fun SplitBar(vm: KtsViewModel) {
    val paragraphLabel = stringResource(R.string.action_paragraph)
    val sentenceLabel = stringResource(R.string.action_sentence)
    val lineLabel = stringResource(R.string.action_line)
    val wordLabel = stringResource(R.string.action_word)
    val characterLabel = stringResource(R.string.action_character)
    val emptyMsg = stringResource(R.string.empty_title)

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        SplitButton(paragraphLabel, Icons.AutoMirrored.Outlined.Notes, vm.splitMode == SplitMode.Paragraph) {
            vm.split(SplitMode.Paragraph, emptyMsg, paragraphLabel)
        }
        SplitButton(sentenceLabel, Icons.AutoMirrored.Outlined.ShortText, vm.splitMode == SplitMode.Sentence) {
            vm.split(SplitMode.Sentence, emptyMsg, sentenceLabel)
        }
        SplitButton(lineLabel, Icons.AutoMirrored.Outlined.Segment, vm.splitMode == SplitMode.Line) {
            vm.split(SplitMode.Line, emptyMsg, lineLabel)
        }
        SplitButton(wordLabel, Icons.Outlined.FormatSize, vm.splitMode == SplitMode.Word) {
            vm.split(SplitMode.Word, emptyMsg, wordLabel)
        }
        SplitButton(characterLabel, Icons.Outlined.FormatSize, vm.splitMode == SplitMode.Character) {
            vm.split(SplitMode.Character, emptyMsg, characterLabel)
        }
    }
}

@Composable
private fun SplitButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    isSelectable: Boolean = true,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val stateDesc = if (selected) stringResource(R.string.state_selected) else stringResource(R.string.state_not_selected)
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.clearAndSetSemantics {
            contentDescription = if (isSelectable) "$label, $stateDesc" else label
            role = Role.Button
        },
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 4.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (!enabled) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                } else if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            Text(
                label,
                fontSize = 11.sp,
                color = if (!enabled) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                } else if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
    }
}

@Composable
private fun ChunkNavigationBar(vm: KtsViewModel) {
    val template = stringResource(R.string.current_chunk_announced)
    val counterText = stringResource(R.string.chunk_counter, vm.focusedChunkIndex + 1, vm.chunks.size)
    val prevDesc = stringResource(R.string.action_previous_chunk)
    val nextDesc = stringResource(R.string.action_next_chunk)

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (vm.tab == AppTab.Reading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = counterText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        vm.prevChunk { index, total, text ->
                            String.format(java.util.Locale.getDefault(), template, index, total, text)
                        }
                    },
                    enabled = vm.focusedChunkIndex > 0,
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = prevDesc,
                        tint = if (vm.focusedChunkIndex > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    )
                }

                Text(
                    text = counterText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                IconButton(
                    onClick = {
                        vm.nextChunk { index, total, text ->
                            String.format(java.util.Locale.getDefault(), template, index, total, text)
                        }
                    },
                    enabled = vm.focusedChunkIndex < vm.chunks.size - 1,
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = nextDesc,
                        tint = if (vm.focusedChunkIndex < vm.chunks.size - 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    )
                }
            }
        }
    }
}

@Composable
private fun OpenTextsBar(vm: KtsViewModel) {
    val context = LocalView.current.context
    val sessionSwitchTemplate: (Int, Int, String) -> String = { curr, total, title ->
        context.getString(R.string.session_switched_announced, curr, total, title)
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        shape = RoundedCornerShape(10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = {
                    KtsHaptics.vibrateSelect(context)
                    vm.switchToPrevSession(sessionSwitchTemplate)
                },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.action_prev_text),
                    modifier = Modifier.size(18.dp),
                )
            }

            LazyRow(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                itemsIndexed(vm.sessions, key = { _, s -> s.id }) { index, session ->
                    val isSelected = index == vm.activeSessionIndex
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            KtsHaptics.vibrateSelect(context)
                            vm.switchToSession(index, sessionSwitchTemplate)
                        },
                        label = {
                            Text(
                                text = "${index + 1}. ${session.title}",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    KtsHaptics.vibrateAction(context)
                                    vm.closeSession(index, sessionSwitchTemplate)
                                },
                                modifier = Modifier.size(18.dp),
                            ) {
                                Icon(
                                    Icons.Outlined.Close,
                                    contentDescription = "${stringResource(R.string.action_close_text)}: ${index + 1}. ${session.title}",
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        },
                    )
                }
            }

            IconButton(
                onClick = {
                    KtsHaptics.vibrateSelect(context)
                    vm.switchToNextSession(sessionSwitchTemplate)
                },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = stringResource(R.string.action_next_text),
                    modifier = Modifier.size(18.dp),
                )
            }

            if (vm.sessions.size > 1) {
                IconButton(
                    onClick = {
                        KtsHaptics.vibrateAction(context)
                        vm.closeAllSessions("Close All")
                    },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = "Close All",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

private fun Modifier.twoFingerSwipe(
    enabled: Boolean = true,
    isTouchExploration: Boolean = false,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
): Modifier = if (!enabled) this else pointerInput(isTouchExploration) {
    awaitEachGesture {
        var totalX = 0f
        var totalY = 0f
        var swipeDir = 0

        do {
            val event = awaitPointerEvent()
            val pointers = event.changes.filter { it.pressed }

            val isMultiTouch = pointers.size >= 2 || (isTouchExploration && pointers.size == 1)

            if (isMultiTouch) {
                val dx = if (pointers.size >= 2) {
                    (pointers[0].position.x - pointers[0].previousPosition.x +
                        pointers[1].position.x - pointers[1].previousPosition.x) / 2f
                } else {
                    pointers[0].position.x - pointers[0].previousPosition.x
                }

                val dy = if (pointers.size >= 2) {
                    (pointers[0].position.y - pointers[0].previousPosition.y +
                        pointers[1].position.y - pointers[1].previousPosition.y) / 2f
                } else {
                    pointers[0].position.y - pointers[0].previousPosition.y
                }

                totalX += dx
                totalY += dy

                val thresholdPx = 90f

                if (swipeDir == 0 && abs(totalX) > thresholdPx && abs(totalX) > abs(totalY) * 1.3f) {
                    swipeDir = if (totalX < -thresholdPx) -1 else 1
                }
                if (swipeDir != 0) {
                    event.changes.forEach { it.consume() }
                }
            }
        } while (event.changes.any { it.pressed })

        if (swipeDir < 0) {
            onSwipeLeft()
        } else if (swipeDir > 0) {
            onSwipeRight()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ColumnScope.TextWorkspace(vm: KtsViewModel, onCopy: () -> Unit) {
    val context = LocalView.current.context
    val text = vm.currentText()
    val newCreatedMsg = stringResource(R.string.new_text_created)

    if (vm.tab == AppTab.Editing && vm.chunks.isEmpty()) {
        OutlinedTextField(
            value = vm.sourceText,
            onValueChange = {
                vm.sourceText = it
                vm.saveActiveSessionState()
            },
            placeholder = { Text(stringResource(R.string.type_or_paste_here)) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = vm.fontSize.sp),
        )
        return
    }

    if (text.isBlank()) {
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(R.string.empty_title), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.empty_body), style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    KtsHaptics.vibrateAction(context)
                    vm.createNewText(newCreatedMsg)
                },
            ) {
                Icon(Icons.Outlined.NoteAdd, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.action_new_text))
            }
        }
        return
    }
    if (vm.chunks.isEmpty()) {
        Text(
            text,
            fontSize = vm.fontSize.sp,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        )
        return
    }
    if (vm.tab == AppTab.Selection && vm.chunks.isNotEmpty()) {
        val allMsg = stringResource(R.string.all_selected)
        val clearMsg = stringResource(R.string.selection_cleared)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.selected_count, vm.selectedIds.size, vm.chunks.size),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = {
                        KtsHaptics.vibrateSelect(context)
                        vm.showFormatDialog = true
                    },
                    enabled = vm.selectedIds.isNotEmpty(),
                ) {
                    Icon(
                        Icons.Outlined.FormatPaint,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.action_format), fontSize = 12.sp)
                }
                TextButton(onClick = { vm.selectAll(allMsg) }) {
                    Text(stringResource(R.string.select_all), fontSize = 12.sp)
                }
                TextButton(
                    onClick = { vm.clearSelection(clearMsg) },
                    enabled = vm.selectedIds.isNotEmpty() || vm.rangeAnchorId != null,
                ) {
                    Text(stringResource(R.string.deselect_all), fontSize = 12.sp)
                }
            }
        }
        if (vm.rangeAnchorId != null) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.range_active),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                TextButton(onClick = { vm.rangeAnchorId = null }) {
                    Text(stringResource(R.string.cancel), fontSize = 11.sp)
                }
            }
        }
    }

    val columnCount = when (vm.splitMode) {
        SplitMode.Paragraph, SplitMode.Sentence, SplitMode.Line -> 1
        SplitMode.Word -> 2
        SplitMode.Character -> 6
        null -> 1
    }

    val gridSpacing = when (vm.splitMode) {
        SplitMode.Paragraph -> 8.dp
        SplitMode.Sentence, SplitMode.Line -> 6.dp
        SplitMode.Word -> 6.dp
        SplitMode.Character -> 4.dp
        null -> 8.dp
    }

    val cellPadding = when (vm.splitMode) {
        SplitMode.Paragraph -> 12.dp
        SplitMode.Sentence, SplitMode.Line -> 10.dp
        SplitMode.Word -> 8.dp
        SplitMode.Character -> 6.dp
        null -> 12.dp
    }

    val textAlign = when (vm.splitMode) {
        SplitMode.Word, SplitMode.Character -> TextAlign.Center
        else -> TextAlign.Start
    }

    val cellAlignment = when (vm.splitMode) {
        SplitMode.Word, SplitMode.Character -> Alignment.Center
        else -> Alignment.TopStart
    }

    val gridState = rememberLazyGridState()
    val clipboardManager = LocalClipboardManager.current
    val template = stringResource(R.string.current_chunk_announced)

    LaunchedEffect(vm.focusedChunkIndex) {
        if (vm.focusedChunkIndex in vm.chunks.indices) {
            gridState.animateScrollToItem(vm.focusedChunkIndex)
        }
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(columnCount),
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        contentPadding = PaddingValues(top = 4.dp, bottom = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(gridSpacing),
        verticalArrangement = Arrangement.spacedBy(gridSpacing),
    ) {
        gridItemsIndexed(vm.chunks, key = { _, c -> c.id }) { index, chunk ->
            val selected = chunk.id in vm.selectedIds
            val isFocused = vm.focusedChunkIndex == index
            val desc = if (vm.announceChunkNumbers) {
                "${index + 1} / ${vm.chunks.size}. ${chunk.text}"
            } else {
                chunk.text
            }
            when (vm.tab) {
                AppTab.Reading -> {
                    val containerColor = if (isFocused) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    }
                    val borderColor = if (isFocused) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    }
                    val listenLabel = stringResource(R.string.action_listen)
                    val copyLabel = stringResource(R.string.action_copy)
                    val switchToEditingLabel = stringResource(R.string.action_switch_to_editing)
                    val openWindowLabel = stringResource(R.string.open_window)
                    val formatInfoLabel = stringResource(R.string.action_format_info)
                    val copiedMsg = stringResource(R.string.copied)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics(mergeDescendants = true) {
                                contentDescription = desc
                                customActions = listOf(
                                    CustomAccessibilityAction(listenLabel) {
                                        KtsHaptics.vibrateAction(context)
                                        vm.focusChunk(index) { i, total, t ->
                                            String.format(java.util.Locale.getDefault(), template, i, total, t)
                                        }
                                        BackgroundTtsManager.speak(chunk.text, "ChunkReadingTts")
                                        true
                                    },
                                    CustomAccessibilityAction(copyLabel) {
                                        KtsHaptics.vibrateSelect(context)
                                        clipboardManager.setText(AnnotatedString(chunk.text))
                                        Toast.makeText(context, copiedMsg, Toast.LENGTH_SHORT).show()
                                        vm.announce = copiedMsg
                                        true
                                    },
                                    CustomAccessibilityAction(openWindowLabel) {
                                        KtsHaptics.vibrateAction(context)
                                        vm.focusedChunkIndex = index
                                        vm.windowId = chunk.id
                                        true
                                    },
                                    CustomAccessibilityAction(formatInfoLabel) {
                                        KtsHaptics.vibrateAction(context)
                                        vm.showFormatInfo(chunk, index)
                                        true
                                    },
                                    CustomAccessibilityAction(switchToEditingLabel) {
                                        KtsHaptics.vibrateAction(context)
                                        vm.focusedChunkIndex = index
                                        vm.tab = AppTab.Editing
                                        true
                                    },
                                )
                            }
                            .combinedClickable(
                                onClick = {
                                    KtsHaptics.vibrateSelect(context)
                                    vm.focusChunk(index) { i, total, t ->
                                        String.format(java.util.Locale.getDefault(), template, i, total, t)
                                    }
                                },
                                onLongClick = {
                                    KtsHaptics.vibrateLongPress(context)
                                    vm.showFormatInfo(chunk, index)
                                },
                                onLongClickLabel = formatInfoLabel,
                            ),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = containerColor),
                        border = BorderStroke(if (isFocused) 2.dp else 1.dp, borderColor),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(cellPadding),
                            contentAlignment = cellAlignment,
                        ) {
                            Text(
                                text = chunk.text,
                                fontSize = (chunk.fontSizeSp ?: vm.fontSize).sp,
                                textAlign = textAlign,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (chunk.isBold) FontWeight.Bold else FontWeight.Normal,
                                fontStyle = if (chunk.isItalic) FontStyle.Italic else FontStyle.Normal,
                                textDecoration = if (chunk.isUnderline) TextDecoration.Underline else TextDecoration.None,
                                modifier = Modifier.clearAndSetSemantics { },
                            )
                        }
                    }
                }
                AppTab.Editing -> {
                    val containerColor = if (isFocused) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    }
                    val borderColor = if (isFocused) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    }
                    val moreActionsLabel = stringResource(R.string.more_actions)
                    val openWindowLabel = stringResource(R.string.open_window)
                    val formatInfoLabel = stringResource(R.string.action_format_info)
                    val deleteLabel = stringResource(R.string.delete)
                    val copyLabel = stringResource(R.string.action_copy)
                    val deletedMsg = stringResource(R.string.delete)
                    val copiedMsg = stringResource(R.string.copied)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics(mergeDescendants = true) {
                                contentDescription = desc
                                role = Role.Button
                                customActions = listOf(
                                    CustomAccessibilityAction(moreActionsLabel) {
                                        KtsHaptics.vibrateLongPress(context)
                                        vm.focusedChunkIndex = index
                                        vm.sheetId = chunk.id
                                        true
                                    },
                                    CustomAccessibilityAction(openWindowLabel) {
                                        KtsHaptics.vibrateAction(context)
                                        vm.focusedChunkIndex = index
                                        vm.windowId = chunk.id
                                        true
                                    },
                                    CustomAccessibilityAction(formatInfoLabel) {
                                        KtsHaptics.vibrateAction(context)
                                        vm.showFormatInfo(chunk, index)
                                        true
                                    },
                                    CustomAccessibilityAction(deleteLabel) {
                                        KtsHaptics.vibrateAction(context)
                                        vm.deleteChunk(chunk.id, deletedMsg, context)
                                        true
                                    },
                                    CustomAccessibilityAction(copyLabel) {
                                        KtsHaptics.vibrateSelect(context)
                                        clipboardManager.setText(AnnotatedString(chunk.text))
                                        Toast.makeText(context, copiedMsg, Toast.LENGTH_SHORT).show()
                                        vm.announce = copiedMsg
                                        true
                                    },
                                )
                            }
                            .combinedClickable(
                                onClick = {
                                    KtsHaptics.vibrateSelect(context)
                                    vm.focusedChunkIndex = index
                                    vm.editingId = chunk.id
                                },
                                onLongClick = {
                                    KtsHaptics.vibrateLongPress(context)
                                    vm.focusedChunkIndex = index
                                    vm.sheetId = chunk.id
                                },
                                onLongClickLabel = moreActionsLabel,
                            ),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = containerColor),
                        border = BorderStroke(if (isFocused) 2.dp else 1.dp, borderColor),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(cellPadding),
                            contentAlignment = cellAlignment,
                        ) {
                            Text(
                                text = chunk.text,
                                fontSize = (chunk.fontSizeSp ?: vm.fontSize).sp,
                                textAlign = textAlign,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (chunk.isBold) FontWeight.Bold else FontWeight.Normal,
                                fontStyle = if (chunk.isItalic) FontStyle.Italic else FontStyle.Normal,
                                textDecoration = if (chunk.isUnderline) TextDecoration.Underline else TextDecoration.None,
                                modifier = Modifier.clearAndSetSemantics { },
                            )
                        }
                    }
                }
                AppTab.Selection -> {
                    val stateSelected = stringResource(R.string.state_selected)
                    val stateNotSelected = stringResource(R.string.state_not_selected)
                    val selectLabel = stringResource(R.string.selection_options)
                    val isAnchor = vm.rangeAnchorId == chunk.id
                    val hasAnchor = vm.rangeAnchorId != null

                    val toStartMsg = stringResource(R.string.select_to_start)
                    val toEndMsg = stringResource(R.string.select_to_end)
                    val rangeStartMsg = stringResource(R.string.range_start)
                    val rangeFinishTemplate = stringResource(R.string.range_finish_announced)
                    val allMsg = stringResource(R.string.all_selected)
                    val clearMsg = stringResource(R.string.selection_cleared)
                    val deleteSelectedMsg = stringResource(R.string.delete)

                    val selectionActions = mutableListOf<CustomAccessibilityAction>()
                    selectionActions.add(
                        CustomAccessibilityAction(selectLabel) {
                            KtsHaptics.vibrateLongPress(context)
                            vm.focusedChunkIndex = index
                            vm.selectionSheetId = chunk.id
                            true
                        }
                    )
                    selectionActions.add(
                        CustomAccessibilityAction(toStartMsg) {
                            KtsHaptics.vibrateAction(context)
                            vm.selectToStart(chunk.id, toStartMsg)
                            true
                        }
                    )
                    selectionActions.add(
                        CustomAccessibilityAction(toEndMsg) {
                            KtsHaptics.vibrateAction(context)
                            vm.selectToEnd(chunk.id, toEndMsg)
                            true
                        }
                    )
                    if (!hasAnchor || isAnchor) {
                        selectionActions.add(
                            CustomAccessibilityAction(stringResource(R.string.select_range_start)) {
                                KtsHaptics.vibrateAction(context)
                                vm.startRange(chunk.id, rangeStartMsg)
                                true
                            }
                        )
                    } else {
                        selectionActions.add(
                            CustomAccessibilityAction(stringResource(R.string.select_range_finish)) {
                                KtsHaptics.vibrateAction(context)
                                vm.finishRange(chunk.id) { count ->
                                    String.format(java.util.Locale.getDefault(), rangeFinishTemplate, count)
                                }
                                true
                            }
                        )
                    }
                    selectionActions.add(
                        CustomAccessibilityAction(allMsg) {
                            KtsHaptics.vibrateAction(context)
                            vm.selectAll(allMsg)
                            true
                        }
                    )
                    selectionActions.add(
                        CustomAccessibilityAction(clearMsg) {
                            KtsHaptics.vibrateAction(context)
                            vm.clearSelection(clearMsg)
                            true
                        }
                    )
                    if (vm.selectedIds.isNotEmpty()) {
                        selectionActions.add(
                            CustomAccessibilityAction(stringResource(R.string.action_format)) {
                                KtsHaptics.vibrateAction(context)
                                vm.showFormatDialog = true
                                true
                            }
                        )
                        selectionActions.add(
                            CustomAccessibilityAction(stringResource(R.string.delete_selected)) {
                                KtsHaptics.vibrateAction(context)
                                vm.deleteSelected(deleteSelectedMsg)
                                true
                            }
                        )
                    }
                    selectionActions.add(
                        CustomAccessibilityAction(stringResource(R.string.action_format_info)) {
                            KtsHaptics.vibrateAction(context)
                            vm.showFormatInfo(chunk, index)
                            true
                        }
                    )

                    val containerColor = when {
                        isAnchor -> MaterialTheme.colorScheme.tertiaryContainer
                        selected -> MaterialTheme.colorScheme.primaryContainer
                        isFocused -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    }
                    val contentColor = when {
                        isAnchor -> MaterialTheme.colorScheme.onTertiaryContainer
                        selected -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                    val borderColor = when {
                        isAnchor -> MaterialTheme.colorScheme.tertiary
                        selected -> MaterialTheme.colorScheme.primary
                        isFocused -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    }
                    val borderWidth = if (selected || isAnchor || isFocused) 2.dp else 1.dp

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics(mergeDescendants = true) {
                                contentDescription = desc
                                stateDescription = if (selected) stateSelected else stateNotSelected
                                role = Role.Checkbox
                                customActions = selectionActions
                            }
                            .combinedClickable(
                                onClick = {
                                    KtsHaptics.vibrateSelect(context)
                                    vm.focusedChunkIndex = index
                                    vm.toggleSelect(chunk.id, stateSelected, stateNotSelected)
                                },
                                onLongClick = {
                                    KtsHaptics.vibrateLongPress(context)
                                    vm.focusedChunkIndex = index
                                    vm.selectionSheetId = chunk.id
                                },
                                onLongClickLabel = selectLabel,
                            ),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = containerColor),
                        border = BorderStroke(borderWidth, borderColor),
                    ) {
                        if (columnCount <= 2) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(cellPadding),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = selected,
                                    onCheckedChange = null,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clearAndSetSemantics { },
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = chunk.text,
                                    fontSize = (chunk.fontSizeSp ?: vm.fontSize).sp,
                                    color = contentColor,
                                    fontWeight = when {
                                        chunk.isBold -> FontWeight.Bold
                                        isAnchor || selected -> FontWeight.SemiBold
                                        else -> FontWeight.Normal
                                    },
                                    fontStyle = if (chunk.isItalic) FontStyle.Italic else FontStyle.Normal,
                                    textDecoration = if (chunk.isUnderline) TextDecoration.Underline else TextDecoration.None,
                                    modifier = Modifier.clearAndSetSemantics { },
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(cellPadding),
                                contentAlignment = cellAlignment,
                            ) {
                                Text(
                                    text = chunk.text,
                                    fontSize = (chunk.fontSizeSp ?: vm.fontSize).sp,
                                    textAlign = textAlign,
                                    color = contentColor,
                                    fontWeight = when {
                                        chunk.isBold -> FontWeight.Bold
                                        isAnchor || selected -> FontWeight.Bold
                                        else -> FontWeight.Normal
                                    },
                                    fontStyle = if (chunk.isItalic) FontStyle.Italic else FontStyle.Normal,
                                    textDecoration = if (chunk.isUnderline) TextDecoration.Underline else TextDecoration.None,
                                    modifier = Modifier.clearAndSetSemantics { },
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionSheet(
    vm: KtsViewModel,
    id: String,
) {
    val context = LocalView.current.context
    val deletedMsg = stringResource(R.string.delete)
    ModalBottomSheet(onDismissRequest = { vm.sheetId = null }) {
        TextButton(
            onClick = {
                KtsHaptics.vibrateAction(context)
                vm.windowId = id
                vm.sheetId = null
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Outlined.Edit, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.open_window))
        }
        TextButton(
            onClick = {
                KtsHaptics.vibrateAction(context)
                val chunk = vm.chunks.firstOrNull { it.id == id }
                if (chunk != null) {
                    vm.showFormatInfo(chunk)
                }
                vm.sheetId = null
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.format_info_title), color = MaterialTheme.colorScheme.primary)
        }
        TextButton(
            onClick = {
                KtsHaptics.vibrateAction(context)
                vm.deleteChunk(id, deletedMsg, context)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionActionSheet(vm: KtsViewModel, id: String) {
    val context = LocalView.current.context
    val toStartMsg = stringResource(R.string.select_to_start)
    val toEndMsg = stringResource(R.string.select_to_end)
    val rangeStartMsg = stringResource(R.string.range_start)
    val rangeFinishTemplate = stringResource(R.string.range_finish_announced)
    val allMsg = stringResource(R.string.all_selected)
    val clearMsg = stringResource(R.string.selection_cleared)
    val isAnchor = vm.rangeAnchorId == id
    val hasAnchor = vm.rangeAnchorId != null

    ModalBottomSheet(onDismissRequest = { vm.selectionSheetId = null }) {
        Text(
            stringResource(R.string.selection_options),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontWeight = FontWeight.Bold,
        )

        TextButton(
            onClick = {
                KtsHaptics.vibrateAction(context)
                vm.selectToStart(id, toStartMsg)
                vm.selectionSheetId = null
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.select_to_start))
        }

        TextButton(
            onClick = {
                KtsHaptics.vibrateAction(context)
                vm.selectToEnd(id, toEndMsg)
                vm.selectionSheetId = null
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.select_to_end))
        }

        if (!hasAnchor || isAnchor) {
            TextButton(
                onClick = {
                    KtsHaptics.vibrateAction(context)
                    vm.startRange(id, rangeStartMsg)
                    vm.selectionSheetId = null
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.select_range_start))
            }
        } else {
            TextButton(
                onClick = {
                    KtsHaptics.vibrateAction(context)
                    vm.finishRange(id) { count ->
                        String.format(java.util.Locale.getDefault(), rangeFinishTemplate, count)
                    }
                    vm.selectionSheetId = null
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.select_range_finish))
            }
        }

        TextButton(
            onClick = {
                KtsHaptics.vibrateAction(context)
                vm.selectAll(allMsg)
                vm.selectionSheetId = null
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.select_all))
        }

        TextButton(
            onClick = {
                KtsHaptics.vibrateAction(context)
                val chunk = vm.chunks.firstOrNull { it.id == id }
                if (chunk != null) {
                    vm.showFormatInfo(chunk)
                }
                vm.selectionSheetId = null
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.format_info_title), color = MaterialTheme.colorScheme.primary)
        }

        TextButton(
            onClick = {
                KtsHaptics.vibrateAction(context)
                vm.clearSelection(clearMsg)
                vm.selectionSheetId = null
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.deselect_all), color = MaterialTheme.colorScheme.error)
        }

        if (vm.selectedIds.isNotEmpty()) {
            TextButton(
                onClick = {
                    KtsHaptics.vibrateSelect(context)
                    vm.showFormatDialog = true
                    vm.selectionSheetId = null
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Outlined.FormatPaint, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.action_format), color = MaterialTheme.colorScheme.primary)
            }
            val deletedMsg = stringResource(R.string.delete)
            TextButton(
                onClick = {
                    KtsHaptics.vibrateAction(context)
                    vm.deleteSelected(deletedMsg)
                    vm.selectionSheetId = null
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.delete_selected), color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun FormatTextDialog(
    vm: KtsViewModel,
    onDismiss: () -> Unit,
) {
    val context = LocalView.current.context
    val appliedMsg = stringResource(R.string.format_applied)
    val firstSelectedChunk = remember {
        vm.chunks.firstOrNull { it.id in vm.selectedIds }
    }

    var fontSize by remember {
        mutableFloatStateOf(firstSelectedChunk?.fontSizeSp ?: vm.fontSize)
    }
    var useDefaultFontSize by remember {
        mutableStateOf(firstSelectedChunk?.fontSizeSp == null)
    }
    var isBold by remember {
        mutableStateOf(firstSelectedChunk?.isBold ?: false)
    }
    var isItalic by remember {
        mutableStateOf(firstSelectedChunk?.isItalic ?: false)
    }
    var isUnderline by remember {
        mutableStateOf(firstSelectedChunk?.isUnderline ?: false)
    }

    val previewSampleText = remember {
        val sample = firstSelectedChunk?.text?.take(80) ?: "Sample Text"
        if (sample.length == 80) "$sample…" else sample
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.FormatPaint,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        stringResource(R.string.format_dialog_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        stringResource(R.string.selected_count, vm.selectedIds.size, vm.chunks.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                // Live Preview Card
                Text(
                    stringResource(R.string.format_preview),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    ),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                    ) {
                        Text(
                            text = previewSampleText,
                            fontSize = if (useDefaultFontSize) vm.fontSize.sp else fontSize.sp,
                            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                            fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                            textDecoration = if (isUnderline) TextDecoration.Underline else TextDecoration.None,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = if (useDefaultFontSize) (vm.fontSize * 1.3f).sp else (fontSize * 1.3f).sp,
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(12.dp))

                // Font Size Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "${stringResource(R.string.format_font_size)}: ${if (useDefaultFontSize) "${vm.fontSize.toInt()} sp (Default)" else "${fontSize.toInt()} sp"}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                useDefaultFontSize = false
                                fontSize = (fontSize - 2f).coerceAtLeast(12f)
                            },
                            enabled = !useDefaultFontSize || fontSize > 12f,
                        ) {
                            Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(
                            onClick = {
                                useDefaultFontSize = false
                                fontSize = (fontSize + 2f).coerceAtMost(48f)
                            },
                            enabled = !useDefaultFontSize || fontSize < 48f,
                        ) {
                            Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Slider(
                    value = fontSize,
                    onValueChange = {
                        useDefaultFontSize = false
                        fontSize = it
                    },
                    valueRange = 12f..48f,
                    steps = 17,
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { useDefaultFontSize = !useDefaultFontSize }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = useDefaultFontSize,
                        onCheckedChange = { useDefaultFontSize = it },
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.format_reset_default),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(12.dp))

                // Bold Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isBold = !isBold }
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.FormatBold,
                            contentDescription = null,
                            tint = if (isBold) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            stringResource(R.string.format_bold),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                    Switch(
                        checked = isBold,
                        onCheckedChange = { isBold = it },
                    )
                }

                // Italic Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isItalic = !isItalic }
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.FormatItalic,
                            contentDescription = null,
                            tint = if (isItalic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            stringResource(R.string.format_italic),
                            style = MaterialTheme.typography.bodyLarge,
                            fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                        )
                    }
                    Switch(
                        checked = isItalic,
                        onCheckedChange = { isItalic = it },
                    )
                }

                // Underline Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isUnderline = !isUnderline }
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.FormatUnderlined,
                            contentDescription = null,
                            tint = if (isUnderline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            stringResource(R.string.format_underline),
                            style = MaterialTheme.typography.bodyLarge,
                            textDecoration = if (isUnderline) TextDecoration.Underline else TextDecoration.None,
                        )
                    }
                    Switch(
                        checked = isUnderline,
                        onCheckedChange = { isUnderline = it },
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    KtsHaptics.vibrateAction(context)
                    val finalFontSize = if (useDefaultFontSize) null else fontSize
                    vm.formatSelectedChunks(
                        fontSize = finalFontSize,
                        isBold = isBold,
                        isItalic = isItalic,
                        isUnderline = isUnderline,
                        formattedMsg = appliedMsg,
                    )
                },
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun TextFormatInfoDialog(
    vm: KtsViewModel,
    chunk: Chunk,
    index: Int,
    onDismiss: () -> Unit,
) {
    val context = LocalView.current.context
    val fontSizeSp = chunk.fontSizeSp ?: vm.fontSize
    val isCustomSize = chunk.fontSizeSp != null
    val isSelected = vm.selectedIds.contains(chunk.id)
    val text = chunk.text
    val charCount = text.length
    val wordCount = text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
    val lineCount = text.lines().size
    val totalChunks = vm.chunks.size
    val pos1Based = if (index >= 0) index + 1 else (vm.chunks.indexOfFirst { it.id == chunk.id } + 1).coerceAtLeast(1)

    val previewSampleText = remember(text) {
        if (text.length > 200) text.take(200) + "…" else text
    }

    val modeName = when (vm.splitMode) {
        SplitMode.Paragraph -> stringResource(R.string.action_paragraph)
        SplitMode.Sentence -> stringResource(R.string.action_sentence)
        SplitMode.Line -> stringResource(R.string.action_line)
        SplitMode.Word -> stringResource(R.string.action_word)
        SplitMode.Character -> stringResource(R.string.action_character)
        null -> "None"
    }

    val titleStr = stringResource(R.string.format_info_title)
    val sizeLabel = stringResource(R.string.format_info_font_size)
    val boldLabel = stringResource(R.string.format_info_bold)
    val italicLabel = stringResource(R.string.format_info_italic)
    val underlineLabel = stringResource(R.string.format_info_underline)
    val yesStr = stringResource(R.string.format_info_yes)
    val noStr = stringResource(R.string.format_info_no)
    val colorLabel = stringResource(R.string.format_info_color)
    val colorDesc = stringResource(R.string.format_info_color_desc)
    val charsLabel = stringResource(R.string.format_info_chars)
    val wordsLabel = stringResource(R.string.format_info_words)
    val linesLabel = stringResource(R.string.format_info_lines)
    val posLabel = stringResource(R.string.format_info_position)
    val selectedStr = stringResource(R.string.format_info_selected)
    val notSelectedStr = stringResource(R.string.format_info_not_selected)

    val announceText = remember(chunk) {
        buildString {
            append(titleStr).append(". ")
            append("$sizeLabel: ${fontSizeSp.toInt()} sp. ")
            append(if (chunk.isBold) "$boldLabel: $yesStr. " else "$boldLabel: $noStr. ")
            append(if (chunk.isItalic) "$italicLabel: $yesStr. " else "$italicLabel: $noStr. ")
            append(if (chunk.isUnderline) "$underlineLabel: $yesStr. " else "$underlineLabel: $noStr. ")
            append("$colorLabel: $colorDesc. ")
            append("$charsLabel: $charCount. ")
            append("$wordsLabel: $wordCount. ")
            append("$linesLabel: $lineCount. ")
            if (totalChunks > 0) {
                append("$posLabel: $pos1Based of $totalChunks. ")
            }
            if (vm.tab == AppTab.Selection) {
                append(if (isSelected) "$selectedStr. " else "$notSelectedStr. ")
            }
        }
    }

    LaunchedEffect(chunk.id) {
        vm.announce = announceText
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.format_info_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = {
                        KtsHaptics.vibrateAction(context)
                        onDismiss()
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.close),
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Preview
                    Text(
                        text = stringResource(R.string.format_preview),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        ),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Box(Modifier.padding(14.dp)) {
                            Text(
                                text = previewSampleText,
                                fontSize = fontSizeSp.sp,
                                fontWeight = if (chunk.isBold) FontWeight.Bold else FontWeight.Normal,
                                fontStyle = if (chunk.isItalic) FontStyle.Italic else FontStyle.Normal,
                                textDecoration = if (chunk.isUnderline) TextDecoration.Underline else TextDecoration.None,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = (fontSizeSp * 1.3f).sp,
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Font Size
                    FormatInfoRow(
                        icon = Icons.Outlined.FormatSize,
                        label = stringResource(R.string.format_info_font_size),
                        value = if (isCustomSize) {
                            stringResource(R.string.format_info_size_custom, fontSizeSp.toInt())
                        } else {
                            stringResource(R.string.format_info_size_default, fontSizeSp.toInt())
                        },
                        highlight = isCustomSize,
                    )

                    // Text Color
                    FormatInfoRow(
                        icon = Icons.Outlined.FormatColorText,
                        label = stringResource(R.string.format_info_color),
                        value = stringResource(R.string.format_info_color_desc),
                    )

                    // Bold
                    FormatInfoRow(
                        icon = Icons.Outlined.FormatBold,
                        label = stringResource(R.string.format_info_bold),
                        value = if (chunk.isBold) stringResource(R.string.format_info_yes) else stringResource(R.string.format_info_no),
                        highlight = chunk.isBold,
                    )

                    // Italic
                    FormatInfoRow(
                        icon = Icons.Outlined.FormatItalic,
                        label = stringResource(R.string.format_info_italic),
                        value = if (chunk.isItalic) stringResource(R.string.format_info_yes) else stringResource(R.string.format_info_no),
                        highlight = chunk.isItalic,
                    )

                    // Underline
                    FormatInfoRow(
                        icon = Icons.Outlined.FormatUnderlined,
                        label = stringResource(R.string.format_info_underline),
                        value = if (chunk.isUnderline) stringResource(R.string.format_info_yes) else stringResource(R.string.format_info_no),
                        highlight = chunk.isUnderline,
                    )

                    // Navigation Mode
                    FormatInfoRow(
                        icon = Icons.Outlined.Category,
                        label = stringResource(R.string.format_info_mode),
                        value = modeName,
                    )

                    // Position
                    if (totalChunks > 0) {
                        FormatInfoRow(
                            icon = Icons.Outlined.Numbers,
                            label = stringResource(R.string.format_info_position),
                            value = stringResource(R.string.format_info_position_val, pos1Based, totalChunks),
                        )
                    }

                    // Selection Status (if Selection mode)
                    if (vm.tab == AppTab.Selection) {
                        FormatInfoRow(
                            icon = Icons.Outlined.Checklist,
                            label = stringResource(R.string.format_info_selection_status),
                            value = if (isSelected) stringResource(R.string.format_info_selected) else stringResource(R.string.format_info_not_selected),
                            highlight = isSelected,
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // Text Statistics Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        ),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(
                                text = stringResource(R.string.format_info_stats),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                            ) {
                                FormatStatColumn(label = stringResource(R.string.format_info_chars), value = "$charCount")
                                FormatStatColumn(label = stringResource(R.string.format_info_words), value = "$wordCount")
                                FormatStatColumn(label = stringResource(R.string.format_info_lines), value = "$lineCount")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        KtsHaptics.vibrateAction(context)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    }
}

@Composable
private fun FormatInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    highlight: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.SemiBold,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun FormatStatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FindReplaceDialog(
    vm: KtsViewModel,
    onDismiss: () -> Unit,
) {
    var findText by remember { mutableStateOf(vm.findQuery) }
    var replaceText by remember { mutableStateOf(vm.replaceQuery) }
    val matchCount = remember(findText, vm.currentText()) {
        vm.countOccurrences(findText)
    }

    val replacedOneMsg = stringResource(R.string.replaced_one)
    val noMatchesMsg = stringResource(R.string.no_matches)
    val replacedAllTemplate = stringResource(R.string.replaced_all_count)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.action_find_replace), fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = findText,
                    onValueChange = {
                        findText = it
                        vm.findQuery = it
                    },
                    label = { Text(stringResource(R.string.find_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = replaceText,
                    onValueChange = {
                        replaceText = it
                        vm.replaceQuery = it
                    },
                    label = { Text(stringResource(R.string.replace_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                if (findText.isNotEmpty()) {
                    val countText = if (matchCount > 0) {
                        stringResource(R.string.matches_found, matchCount)
                    } else {
                        stringResource(R.string.no_matches)
                    }
                    Text(
                        countText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (matchCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = {
                        vm.replaceNext(findText, replaceText, replacedOneMsg, noMatchesMsg)
                    },
                    enabled = findText.isNotEmpty() && matchCount > 0,
                ) {
                    Text(stringResource(R.string.action_replace))
                }
                Button(
                    onClick = {
                        vm.replaceAll(findText, replaceText, { count ->
                            String.format(java.util.Locale.getDefault(), replacedAllTemplate, count)
                        }, noMatchesMsg)
                    },
                    enabled = findText.isNotEmpty() && matchCount > 0,
                ) {
                    Text(stringResource(R.string.action_replace_all))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun EditDialog(
    title: String,
    initial: String,
    fontSize: Float,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
    onOpenInWindow: ((String) -> Unit)? = null,
) {
    var value by remember(initial) { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title)
                if (onOpenInWindow != null) {
                    IconButton(onClick = { onOpenInWindow(value) }) {
                        Icon(
                            Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = stringResource(R.string.open_window),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp),
            )
        },
        confirmButton = {
            Button(onClick = { onSave(value) }) {
                Icon(Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun FullEditWindow(
    vm: KtsViewModel,
    title: String,
    initial: String,
    fontSize: Float,
    announceChunkNumbers: Boolean,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var value by remember(initial) { mutableStateOf(initial) }
    var subSplitMode by remember(initial) { mutableStateOf<SplitMode?>(null) }
    var subChunks by remember(initial) {
        mutableStateOf<List<Chunk>>(emptyList())
    }
    var subEditingId by remember { mutableStateOf<String?>(null) }
    var subSheetId by remember { mutableStateOf<String?>(null) }
    var isSpeaking by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalView.current.context

    val paragraphLabel = stringResource(R.string.action_paragraph)
    val sentenceLabel = stringResource(R.string.action_sentence)
    val lineLabel = stringResource(R.string.action_line)
    val wordLabel = stringResource(R.string.action_word)
    val characterLabel = stringResource(R.string.action_character)
    val fullTextLabel = stringResource(R.string.action_full_text)
    val copiedMsg = stringResource(R.string.copied)
    val saveLabel = stringResource(R.string.save)
    val cancelLabel = stringResource(R.string.cancel)

    fun currentFullText(): String =
        if (subChunks.isNotEmpty()) TextSplitter.join(subChunks, subSplitMode) else value

    fun doSplit(mode: SplitMode) {
        val base = currentFullText()
        if (base.isNotBlank()) {
            value = base
            subChunks = TextSplitter.split(base, mode)
            subSplitMode = mode
        }
    }

    fun showFullText() {
        if (subChunks.isNotEmpty()) {
            value = TextSplitter.join(subChunks, subSplitMode)
            subChunks = emptyList()
            subSplitMode = null
        }
    }

    BackHandler {
        if (isSpeaking) BackgroundTtsManager.stop()
        onDismiss()
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(100f),
        color = MaterialTheme.colorScheme.background,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    modifier = Modifier.statusBarsPadding(),
                    title = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (isSpeaking) BackgroundTtsManager.stop()
                            onDismiss()
                        }) {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = cancelLabel,
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            if (isSpeaking) {
                                BackgroundTtsManager.stop()
                                isSpeaking = false
                            } else {
                                val textToRead = currentFullText()
                                if (textToRead.isNotBlank()) {
                                    BackgroundTtsManager.speak(textToRead, "WindowTts")
                                    isSpeaking = true
                                }
                            }
                        }) {
                            Icon(
                                if (isSpeaking) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
                                contentDescription = stringResource(if (isSpeaking) R.string.action_stop else R.string.action_play),
                            )
                        }
                        IconButton(onClick = {
                            val textToCopy = currentFullText()
                            if (textToCopy.isNotBlank()) {
                                clipboardManager.setText(AnnotatedString(textToCopy))
                                Toast.makeText(context, copiedMsg, Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = stringResource(R.string.action_copy))
                        }
                        Button(
                            onClick = {
                                if (isSpeaking) BackgroundTtsManager.stop()
                                onSave(currentFullText())
                            },
                            modifier = Modifier.padding(end = 8.dp),
                        ) {
                            Text(saveLabel)
                        }
                    },
                )
            },
            bottomBar = {
                Column(Modifier.navigationBarsPadding().imePadding()) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        SplitButton(
                            label = paragraphLabel,
                            icon = Icons.AutoMirrored.Outlined.Notes,
                            selected = subSplitMode == SplitMode.Paragraph,
                        ) {
                            KtsHaptics.vibrateSelect(context)
                            doSplit(SplitMode.Paragraph)
                        }
                        SplitButton(
                            label = sentenceLabel,
                            icon = Icons.AutoMirrored.Outlined.ShortText,
                            selected = subSplitMode == SplitMode.Sentence,
                        ) {
                            KtsHaptics.vibrateSelect(context)
                            doSplit(SplitMode.Sentence)
                        }
                        SplitButton(
                            label = lineLabel,
                            icon = Icons.AutoMirrored.Outlined.Segment,
                            selected = subSplitMode == SplitMode.Line,
                        ) {
                            KtsHaptics.vibrateSelect(context)
                            doSplit(SplitMode.Line)
                        }
                        SplitButton(
                            label = wordLabel,
                            icon = Icons.Outlined.FormatSize,
                            selected = subSplitMode == SplitMode.Word,
                        ) {
                            KtsHaptics.vibrateSelect(context)
                            doSplit(SplitMode.Word)
                        }
                        SplitButton(
                            label = characterLabel,
                            icon = Icons.Outlined.FormatSize,
                            selected = subSplitMode == SplitMode.Character,
                        ) {
                            KtsHaptics.vibrateSelect(context)
                            doSplit(SplitMode.Character)
                        }
                        SplitButton(
                            label = fullTextLabel,
                            icon = Icons.Outlined.Edit,
                            selected = subSplitMode == null,
                        ) {
                            KtsHaptics.vibrateSelect(context)
                            showFullText()
                        }
                    }
                }
            },
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                if (subChunks.isEmpty()) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp),
                    )
                } else {
                    val columnCount = when (subSplitMode) {
                        SplitMode.Paragraph, SplitMode.Sentence, SplitMode.Line -> 1
                        SplitMode.Word -> 2
                        SplitMode.Character -> 6
                        null -> 1
                    }
                    val gridSpacing = when (subSplitMode) {
                        SplitMode.Paragraph -> 8.dp
                        SplitMode.Sentence, SplitMode.Line -> 6.dp
                        SplitMode.Word -> 6.dp
                        SplitMode.Character -> 4.dp
                        null -> 8.dp
                    }
                    val cellPadding = when (subSplitMode) {
                        SplitMode.Paragraph -> 12.dp
                        SplitMode.Sentence, SplitMode.Line -> 10.dp
                        SplitMode.Word -> 8.dp
                        SplitMode.Character -> 6.dp
                        null -> 12.dp
                    }
                    val textAlign = when (subSplitMode) {
                        SplitMode.Word, SplitMode.Character -> TextAlign.Center
                        else -> TextAlign.Start
                    }
                    val cellAlignment = when (subSplitMode) {
                        SplitMode.Word, SplitMode.Character -> Alignment.Center
                        else -> Alignment.TopStart
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columnCount),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(gridSpacing),
                        verticalArrangement = Arrangement.spacedBy(gridSpacing),
                    ) {
                        gridItemsIndexed(subChunks, key = { _, c -> c.id }) { index, chunk ->
                            val desc = if (announceChunkNumbers) {
                                "${index + 1} / ${subChunks.size}. ${chunk.text}"
                            } else {
                                chunk.text
                            }
                            val moreActionsLabel = stringResource(R.string.more_actions)
                            val listenPartLabel = stringResource(R.string.listen_part)
                            val formatInfoLabel = stringResource(R.string.action_format_info)
                            val copyLabel = stringResource(R.string.action_copy)
                            val deleteLabel = stringResource(R.string.delete)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics(mergeDescendants = true) {
                                        contentDescription = desc
                                        role = Role.Button
                                        customActions = listOf(
                                            CustomAccessibilityAction(moreActionsLabel) {
                                                KtsHaptics.vibrateLongPress(context)
                                                subSheetId = chunk.id
                                                true
                                            },
                                            CustomAccessibilityAction(listenPartLabel) {
                                                KtsHaptics.vibrateAction(context)
                                                BackgroundTtsManager.speak(chunk.text, "SubChunkTts")
                                                true
                                            },
                                            CustomAccessibilityAction(formatInfoLabel) {
                                                KtsHaptics.vibrateAction(context)
                                                vm.showFormatInfo(chunk, index)
                                                true
                                            },
                                            CustomAccessibilityAction(copyLabel) {
                                                KtsHaptics.vibrateSelect(context)
                                                clipboardManager.setText(AnnotatedString(chunk.text))
                                                Toast.makeText(context, copiedMsg, Toast.LENGTH_SHORT).show()
                                                true
                                            },
                                            CustomAccessibilityAction(deleteLabel) {
                                                KtsHaptics.vibrateAction(context)
                                                subChunks = subChunks.filterNot { it.id == chunk.id }
                                                value = TextSplitter.join(subChunks, subSplitMode)
                                                true
                                            },
                                        )
                                    }
                                    .combinedClickable(
                                        onClick = {
                                            KtsHaptics.vibrateSelect(context)
                                            subEditingId = chunk.id
                                        },
                                        onLongClick = {
                                            KtsHaptics.vibrateLongPress(context)
                                            subSheetId = chunk.id
                                        },
                                        onLongClickLabel = moreActionsLabel,
                                    ),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(cellPadding),
                                    contentAlignment = cellAlignment,
                                ) {
                                    Text(
                                        text = chunk.text,
                                        fontSize = (chunk.fontSizeSp ?: fontSize).sp,
                                        textAlign = textAlign,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (chunk.isBold) FontWeight.Bold else FontWeight.Normal,
                                        fontStyle = if (chunk.isItalic) FontStyle.Italic else FontStyle.Normal,
                                        textDecoration = if (chunk.isUnderline) TextDecoration.Underline else TextDecoration.None,
                                        modifier = Modifier.clearAndSetSemantics { },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        subEditingId?.let { id ->
            val chunk = subChunks.find { it.id == id } ?: return@let
            EditDialog(
                title = stringResource(R.string.edit),
                initial = chunk.text,
                fontSize = fontSize,
                onSave = { updated ->
                    subChunks = subChunks.map { if (it.id == id) it.copy(text = updated) else it }
                    value = TextSplitter.join(subChunks, subSplitMode)
                    subEditingId = null
                },
                onDismiss = { subEditingId = null },
            )
        }

        subSheetId?.let { id ->
            val chunk = subChunks.find { it.id == id } ?: return@let
            ModalBottomSheet(onDismissRequest = { subSheetId = null }) {
                TextButton(
                    onClick = {
                        KtsHaptics.vibrateAction(context)
                        BackgroundTtsManager.speak(chunk.text, "SubChunkTts")
                        subSheetId = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.listen_part))
                }
                TextButton(
                    onClick = {
                        KtsHaptics.vibrateAction(context)
                        clipboardManager.setText(AnnotatedString(chunk.text))
                        Toast.makeText(context, copiedMsg, Toast.LENGTH_SHORT).show()
                        subSheetId = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.action_copy))
                }
                TextButton(
                    onClick = {
                        KtsHaptics.vibrateAction(context)
                        vm.showFormatInfo(chunk, subChunks.indexOf(chunk))
                        subSheetId = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.format_info_title), color = MaterialTheme.colorScheme.primary)
                }
                TextButton(
                    onClick = {
                        KtsHaptics.vibrateAction(context)
                        subChunks = subChunks.filterNot { it.id == id }
                        value = TextSplitter.join(subChunks, subSplitMode)
                        subSheetId = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SettingsPane(
    vm: KtsViewModel,
    onToggleBackgroundReading: (Boolean) -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onRequestIgnoreBatteryOptimization: () -> Unit,
) {
    val context = LocalView.current.context
    var showFilterModeDialog by remember { mutableStateOf(false) }
    Column(
        Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Background Reading Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = stringResource(R.string.pref_bg_reading_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.pref_bg_reading_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = vm.autoReadInBackground,
                        onCheckedChange = { onToggleBackgroundReading(it) },
                    )
                }

                if (vm.autoReadInBackground) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val statusText = if (vm.isAccessibilityEnabled) {
                            stringResource(R.string.pref_bg_reading_status_active)
                        } else {
                            stringResource(R.string.pref_bg_reading_perm_required)
                        }
                        val statusColor = if (vm.isAccessibilityEnabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                        Text(
                            text = "Status: $statusText",
                            style = MaterialTheme.typography.labelLarge,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    if (!vm.isAccessibilityEnabled) {
                        FilledTonalButton(
                            onClick = onOpenAccessibilitySettings,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                Icons.Outlined.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.grant_accessibility_btn))
                        }
                    }

                    if (!vm.isBatteryOptIgnored) {
                        OutlinedButton(
                            onClick = onRequestIgnoreBatteryOptimization,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.battery_optimization_btn))
                        }
                    }
                }

                // Clipboard reading filter mode row
                val currentFilterModeLabel = when (vm.speechFilterMode) {
                    SpeechFilterMode.None -> stringResource(R.string.speech_filter_mode_none_label)
                    SpeechFilterMode.Some -> stringResource(R.string.speech_filter_mode_some_label)
                    SpeechFilterMode.Most -> stringResource(R.string.speech_filter_mode_most_label)
                    SpeechFilterMode.All -> stringResource(R.string.speech_filter_mode_all_label)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showFilterModeDialog = true }
                        .padding(vertical = 4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.pref_speech_filter_mode_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = currentFilterModeLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.pref_speech_filter_mode_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        var showEngineDialog by remember { mutableStateOf(false) }
        var showLanguageDialog by remember { mutableStateOf(false) }
        var showVoiceDialog by remember { mutableStateOf(false) }

        // Advanced TTS Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = stringResource(R.string.tts_settings_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                // 1. TTS Engine Selector
                val currentEngineName = vm.ttsEngines.find { it.packageName == vm.selectedEngine }?.label
                    ?: if (vm.selectedEngine.isBlank()) stringResource(R.string.tts_system_default) else vm.selectedEngine

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showEngineDialog = true }
                        .padding(vertical = 4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.tts_engine_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = currentEngineName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }

                // 2. Language Selector
                val currentLangLabel = when {
                    vm.selectedTtsLanguage.isBlank() || vm.selectedTtsLanguage == "auto" -> stringResource(R.string.tts_auto_detect)
                    vm.selectedTtsLanguage == "en" -> stringResource(R.string.tts_lang_english)
                    else -> java.util.Locale(vm.selectedTtsLanguage).getDisplayLanguage(java.util.Locale.ENGLISH)
                        .ifBlank { vm.selectedTtsLanguage }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLanguageDialog = true }
                        .padding(vertical = 4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.tts_language_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = currentLangLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }

                // 3. Voice Selector
                val currentVoiceLabel = vm.availableVoices.find { it.name == vm.selectedVoice }?.displayName
                    ?: if (vm.selectedVoice.isBlank()) stringResource(R.string.tts_default_voice) else vm.selectedVoice

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showVoiceDialog = true }
                        .padding(vertical = 4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.tts_voice_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = currentVoiceLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }

                // 4. Speech Rate Slider
                val speedText = String.format(java.util.Locale.US, "%.1fx", vm.speechRate)
                Column {
                    Text("${stringResource(R.string.speech_rate)}: $speedText", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = vm.speechRate,
                        onValueChange = { vm.updateSpeechRate(context, it) },
                        valueRange = 0.5f..2.5f,
                        steps = 7,
                    )
                }

                // 5. Pitch Slider
                val pitchText = String.format(java.util.Locale.US, "%.1fx", vm.pitch)
                Column {
                    Text("${stringResource(R.string.tts_pitch_label)}: $pitchText", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = vm.pitch,
                        onValueChange = { vm.updatePitch(context, it) },
                        valueRange = 0.5f..2.0f,
                        steps = 5,
                    )
                }

                // 6. Volume Slider
                val volumeText = "${(vm.volume * 100).toInt()}%"
                Column {
                    Text("${stringResource(R.string.tts_volume_label)}: $volumeText", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = vm.volume,
                        onValueChange = { vm.updateVolume(context, it) },
                        valueRange = 0.1f..1.0f,
                        steps = 8,
                    )
                }

                // 7. Test Voice Button
                val sampleEn = stringResource(R.string.tts_test_sample_en)
                FilledTonalButton(
                    onClick = { vm.toggleTestVoice(context, sampleEn) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        if (vm.isSpeaking) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (vm.isSpeaking) stringResource(R.string.tts_stop_test_btn) else stringResource(R.string.tts_test_voice_btn),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.tts_play_pause_gesture_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (showEngineDialog) {
            AlertDialog(
                onDismissRequest = { showEngineDialog = false },
                title = { Text(stringResource(R.string.tts_engine_label)) },
                text = {
                    LazyColumn {
                        item {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        vm.selectEngine(context, "")
                                        showEngineDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = vm.selectedEngine.isBlank(),
                                    onClick = {
                                        vm.selectEngine(context, "")
                                        showEngineDialog = false
                                    },
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.tts_system_default))
                            }
                        }
                        items(vm.ttsEngines.size) { index ->
                            val engine = vm.ttsEngines[index]
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        vm.selectEngine(context, engine.packageName)
                                        showEngineDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = vm.selectedEngine == engine.packageName,
                                    onClick = {
                                        vm.selectEngine(context, engine.packageName)
                                        showEngineDialog = false
                                    },
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(engine.label)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showEngineDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        if (showLanguageDialog) {
            val autoLabel = stringResource(R.string.tts_auto_detect)
            val enLabel = stringResource(R.string.tts_lang_english)
            val dynamicLangs = vm.availableTtsLanguages
                .map { it.language.lowercase(java.util.Locale.ROOT) }
                .distinct()
                .filter { it.isNotBlank() && it != "en" }
                .map { code ->
                    code to java.util.Locale(code).getDisplayLanguage(java.util.Locale.ENGLISH).ifBlank { code }
                }
                .sortedBy { it.second }
            val langOptions = listOf("auto" to autoLabel, "en" to enLabel) + dynamicLangs

            AlertDialog(
                onDismissRequest = { showLanguageDialog = false },
                title = { Text(stringResource(R.string.tts_language_label)) },
                text = {
                    LazyColumn {
                        items(langOptions.size) { index ->
                            val (code, label) = langOptions[index]
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        vm.selectTtsLanguage(context, code)
                                        showLanguageDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = vm.selectedTtsLanguage == code,
                                    onClick = {
                                        vm.selectTtsLanguage(context, code)
                                        showLanguageDialog = false
                                    },
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(label)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showLanguageDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        if (showVoiceDialog) {
            AlertDialog(
                onDismissRequest = { showVoiceDialog = false },
                title = { Text(stringResource(R.string.tts_voice_label)) },
                text = {
                    LazyColumn {
                        item {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        vm.selectVoice(context, "")
                                        showVoiceDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = vm.selectedVoice.isBlank(),
                                    onClick = {
                                        vm.selectVoice(context, "")
                                        showVoiceDialog = false
                                    },
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.tts_default_voice))
                            }
                        }
                        items(vm.availableVoices.size) { index ->
                            val voice = vm.availableVoices[index]
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        vm.selectVoice(context, voice.name)
                                        showVoiceDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = vm.selectedVoice == voice.name,
                                    onClick = {
                                        vm.selectVoice(context, voice.name)
                                        showVoiceDialog = false
                                    },
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(voice.displayName)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showVoiceDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        if (showFilterModeDialog) {
            AlertDialog(
                onDismissRequest = { showFilterModeDialog = false },
                title = { Text(stringResource(R.string.pref_speech_filter_mode_title)) },
                text = {
                    Column {
                        val filterOptions = listOf(
                            SpeechFilterMode.None to stringResource(R.string.speech_filter_mode_none),
                            SpeechFilterMode.Some to stringResource(R.string.speech_filter_mode_some),
                            SpeechFilterMode.Most to stringResource(R.string.speech_filter_mode_most),
                            SpeechFilterMode.All to stringResource(R.string.speech_filter_mode_all),
                        )
                        filterOptions.forEach { (mode, label) ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        vm.setSpeechFilterMode(context, mode)
                                        showFilterModeDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = vm.speechFilterMode == mode,
                                    onClick = {
                                        vm.setSpeechFilterMode(context, mode)
                                        showFilterModeDialog = false
                                    },
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showFilterModeDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        // Clipboard History Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = stringResource(R.string.pref_clipboard_history_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.pref_clipboard_history_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = vm.clipboardHistoryEnabled,
                        onCheckedChange = { vm.setClipboardHistoryEnabled(context, it) },
                    )
                }

                if (vm.clipboardHistoryItems.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { vm.showClearHistoryDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            Icons.Outlined.DeleteSweep,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.clear_history),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }

        BackupRestoreCard(vm = vm)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.auto_clipboard))
            Switch(checked = vm.autoClipboard, onCheckedChange = { vm.updateAutoClipboard(context, it) })
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = stringResource(R.string.pref_announce_chunk_numbers_title),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = stringResource(R.string.pref_announce_chunk_numbers_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = vm.announceChunkNumbers,
                onCheckedChange = { vm.setAnnounceChunkNumbers(context, it) },
            )
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = stringResource(R.string.pref_haptic_feedback_title),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = stringResource(R.string.pref_haptic_feedback_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = vm.hapticFeedbackEnabled,
                onCheckedChange = {
                    if (it) KtsHaptics.vibrateAction(context)
                    vm.setHapticFeedbackEnabled(context, it)
                },
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.high_contrast))
            Switch(checked = vm.highContrast, onCheckedChange = { vm.updateHighContrast(context, it) })
        }
        Text(stringResource(R.string.font_size))
        Slider(value = vm.fontSize, onValueChange = { vm.updateFontSize(context, it) }, valueRange = 16f..28f)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(onClick = { vm.updateDarkTheme(context, true) }) { Text("Dark") }
            FilledTonalButton(onClick = { vm.updateDarkTheme(context, false) }) { Text("Light") }
        }
    }
}

@Composable
private fun BackupRestoreCard(vm: KtsViewModel) {
    val context = LocalView.current.context
    val savedToDownloadsMsg = stringResource(R.string.backup_saved_to_downloads)
    val backupSavedSuccessMsg = stringResource(R.string.backup_saved_success)
    val backupErrorMsg = stringResource(R.string.backup_error)
    val restoreInvalidMsg = stringResource(R.string.restore_invalid_file)
    val restoreSuccessMsg = stringResource(R.string.restore_success)
    val restoreErrorMsg = stringResource(R.string.restore_error)
    val shareTitle = stringResource(R.string.action_share_backup)

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            vm.backupToUri(
                context = context,
                uri = uri,
                successMsg = backupSavedSuccessMsg,
                errorMsg = backupErrorMsg,
            )
        }
    }

    val openRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            vm.loadBackupFromUriForRestore(
                context = context,
                uri = uri,
                invalidFileMsg = restoreInvalidMsg,
            )
        }
    }

    val openTextCount = remember(vm.sessions, vm.sourceText) {
        val count = vm.sessions.count { it.sourceText.isNotBlank() }
        if (count == 0 && vm.sourceText.isNotBlank()) 1 else count
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.backup_restore_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.backup_restore_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(
                    R.string.backup_current_summary,
                    vm.favoriteItems.size,
                    vm.favoriteFolders.size,
                    vm.clipboardHistoryItems.size,
                    openTextCount,
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            )
            if (vm.lastBackupTimestamp > 0L) {
                Text(
                    text = stringResource(
                        R.string.backup_last_time,
                        BackupManager.formatTimestamp(vm.lastBackupTimestamp),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(2.dp))

            Button(
                onClick = {
                    KtsHaptics.vibrateAction(context)
                    vm.backupToDownloads(
                        context = context,
                        successFormatMsg = savedToDownloadsMsg,
                        errorMsg = backupErrorMsg,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    Icons.Outlined.Save,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.action_backup_to_downloads))
            }

            FilledTonalButton(
                onClick = {
                    KtsHaptics.vibrateSelect(context)
                    createBackupLauncher.launch(BackupManager.defaultBackupFileName())
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    Icons.Outlined.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.action_backup_custom_file))
            }

            FilledTonalButton(
                onClick = {
                    KtsHaptics.vibrateSelect(context)
                    vm.shareBackup(
                        context = context,
                        chooserTitle = shareTitle,
                        errorMsg = backupErrorMsg,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    Icons.Outlined.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.action_share_backup))
            }

            OutlinedButton(
                onClick = {
                    KtsHaptics.vibrateSelect(context)
                    openRestoreLauncher.launch(
                        arrayOf("application/json", "application/octet-stream", "text/plain", "*/*"),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.action_restore_from_file))
            }

            if (vm.hasLocalBackup) {
                OutlinedButton(
                    onClick = {
                        KtsHaptics.vibrateSelect(context)
                        vm.loadLatestLocalBackupForRestore(
                            context = context,
                            invalidFileMsg = restoreInvalidMsg,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        Icons.Outlined.History,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.action_restore_latest_local))
                }
            }
        }
    }

    vm.pendingRestorePayload?.let { payload ->
        AlertDialog(
            onDismissRequest = { vm.pendingRestorePayload = null },
            title = {
                Text(stringResource(R.string.restore_confirm_title))
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = stringResource(
                            R.string.restore_confirm_details,
                            payload.favoriteCount,
                            payload.folderCount,
                            payload.historyCount,
                            payload.sessionCount,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Button(
                        onClick = {
                            KtsHaptics.vibrateAction(context)
                            vm.applyRestore(
                                context = context,
                                payload = payload,
                                replaceExisting = false,
                                successMsg = restoreSuccessMsg,
                                errorMsg = restoreErrorMsg,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.restore_option_merge))
                    }
                    FilledTonalButton(
                        onClick = {
                            KtsHaptics.vibrateAction(context)
                            vm.applyRestore(
                                context = context,
                                payload = payload,
                                replaceExisting = true,
                                successMsg = restoreSuccessMsg,
                                errorMsg = restoreErrorMsg,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.restore_option_replace))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { vm.pendingRestorePayload = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun HistoryPane(vm: KtsViewModel) {
    val context = LocalView.current.context
    val items = vm.clipboardHistoryItems
    Box(modifier = Modifier.fillMaxSize()) {
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        Icons.Outlined.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.history_empty_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.history_empty_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items.size, key = { index -> items[index].id }) { index ->
                    ClipboardItemCard(item = items[index], vm = vm)
                }
            }

            ExtendedFloatingActionButton(
                onClick = {
                    KtsHaptics.vibrateAction(context)
                    vm.showClearHistoryDialog = true
                },
                icon = {
                    Icon(
                        Icons.Outlined.DeleteSweep,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
                text = {
                    Text(
                        stringResource(R.string.action_clear),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.95f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
            )
        }
    }
}

@Composable
private fun FavoritePane(vm: KtsViewModel) {
    val context = LocalView.current.context
    val currentFolder = vm.currentFolder

    if (currentFolder != null) {
        // INSIDE FOLDER VIEW
        BackHandler {
            vm.closeFolder()
        }

        val items = vm.currentFolderItems

        Column(modifier = Modifier.fillMaxSize()) {
            // Folder Navigation Toolbar
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = {
                            KtsHaptics.vibrateSelect(context)
                            vm.closeFolder()
                        },
                    ) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.action_back_to_favorites),
                        )
                    }
                    Text(
                        text = currentFolder.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    IconButton(
                        onClick = {
                            KtsHaptics.vibrateAction(context)
                            vm.folderToManage = currentFolder
                            vm.showRenameFolderDialog = true
                        },
                    ) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = stringResource(R.string.action_rename),
                        )
                    }
                    IconButton(
                        onClick = {
                            KtsHaptics.vibrateAction(context)
                            vm.folderToManage = currentFolder
                            vm.showDeleteFolderDialog = true
                        },
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.dialog_delete_folder_title),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            Icons.Outlined.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.folder_empty_msg),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.folder_empty_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(items.size, key = { index -> items[index].id }) { index ->
                        ClipboardItemCard(item = items[index], vm = vm)
                    }
                }
            }
        }
    } else {
        // ROOT FAVORITE VIEW (Folders + Root Items)
        val folders = vm.favoriteFolders
        val rootItems = vm.rootFavoriteItems
        val hasFolders = folders.isNotEmpty()
        val hasRootItems = rootItems.isNotEmpty()

        Column(modifier = Modifier.fillMaxSize()) {
            // Root Top Action Bar: Header + "+ New Folder" button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.tab_favorite),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp),
                )
                FilledTonalButton(
                    onClick = {
                        KtsHaptics.vibrateAction(context)
                        vm.showCreateFolderDialog = true
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Icon(
                        Icons.Outlined.CreateNewFolder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.action_new_folder),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }

            if (!hasFolders && !hasRootItems) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            Icons.Outlined.Star,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.favorite_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.favorite_empty_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Folders Section
                    if (hasFolders) {
                        item(key = "header_folders") {
                            Text(
                                text = stringResource(R.string.section_folders),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
                            )
                        }
                        items(folders.size, key = { index -> "folder_${folders[index].id}" }) { index ->
                            FavoriteFolderCard(folder = folders[index], vm = vm)
                        }
                    }

                    // Root Favorites Section
                    if (hasRootItems) {
                        if (hasFolders) {
                            item(key = "header_other_favorites") {
                                Text(
                                    text = stringResource(R.string.section_other_favorites),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
                                )
                            }
                        }
                        items(rootItems.size, key = { index -> "item_${rootItems[index].id}" }) { index ->
                            ClipboardItemCard(item = rootItems[index], vm = vm)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ClipboardItemCard(
    item: ClipboardItem,
    vm: KtsViewModel,
) {
    val context = LocalView.current.context
    val clipboardManager = LocalClipboardManager.current
    val copiedMsg = stringResource(R.string.copied)
    val moreActionsLabel = stringResource(R.string.more_actions)
    val isHistoryTab = vm.tab == AppTab.History
    val removeActionLabel = stringResource(R.string.delete)
    val favActionLabel = if (item.isFavorite) {
        stringResource(R.string.action_remove_favorite)
    } else {
        stringResource(R.string.action_add_favorite)
    }
    val openReadingLabel = stringResource(R.string.action_open_reading)
    val openEditingLabel = stringResource(R.string.action_open_editing)
    val openSelectionLabel = stringResource(R.string.action_open_selection)
    val openWindowLabel = stringResource(R.string.open_window)
    val copyActionLabel = stringResource(R.string.action_copy)
    val listenActionLabel = stringResource(R.string.action_listen)
    val deletedMsg = stringResource(R.string.item_deleted)
    val addedFavMsg = stringResource(R.string.added_to_favorites)
    val removedFavMsg = stringResource(R.string.removed_from_favorites)
    val openedReadingMsg = stringResource(R.string.opened_in_reading)
    val openedEditingMsg = stringResource(R.string.opened_in_editing)
    val openedSelectionMsg = stringResource(R.string.opened_in_selection)
    val editActionLabel = stringResource(R.string.edit)
    val moveToFolderLabel = stringResource(R.string.action_move_to_folder)
    val copyToFolderLabel = stringResource(R.string.action_copy_to_folder)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                customActions = buildList {
                    add(CustomAccessibilityAction(removeActionLabel) {
                        KtsHaptics.vibrateAction(context)
                        if (isHistoryTab) {
                            vm.removeFromHistory(context, item, deletedMsg)
                        } else {
                            vm.deleteCompletely(context, item, deletedMsg)
                        }
                        Toast.makeText(context, deletedMsg, Toast.LENGTH_SHORT).show()
                        true
                    })
                    add(CustomAccessibilityAction(moreActionsLabel) {
                        KtsHaptics.vibrateLongPress(context)
                        vm.activeClipboardSheetItem = item
                        true
                    })
                    add(CustomAccessibilityAction(favActionLabel) {
                        KtsHaptics.vibrateAction(context)
                        vm.toggleHistoryFavorite(context, item, addedFavMsg, removedFavMsg)
                        true
                    })
                    add(CustomAccessibilityAction(copyActionLabel) {
                        KtsHaptics.vibrateSelect(context)
                        clipboardManager.setText(AnnotatedString(item.text))
                        Toast.makeText(context, copiedMsg, Toast.LENGTH_SHORT).show()
                        vm.announce = copiedMsg
                        true
                    })
                    add(CustomAccessibilityAction(editActionLabel) {
                        KtsHaptics.vibrateAction(context)
                        vm.editingClipboardItem = item
                        true
                    })
                    add(CustomAccessibilityAction(openReadingLabel) {
                        KtsHaptics.vibrateAction(context)
                        vm.openInReadingMode(item, openedReadingMsg)
                        true
                    })
                    add(CustomAccessibilityAction(listenActionLabel) {
                        KtsHaptics.vibrateAction(context)
                        BackgroundTtsManager.speak(item.text, "ClipboardHistoryItemTts")
                        true
                    })
                    if (vm.favoriteFolders.isNotEmpty() && (vm.tab == AppTab.Favorite || item.isFavorite)) {
                        add(CustomAccessibilityAction(moveToFolderLabel) {
                            KtsHaptics.vibrateAction(context)
                            vm.itemToMove = item
                            true
                        })
                        add(CustomAccessibilityAction(copyToFolderLabel) {
                            KtsHaptics.vibrateAction(context)
                            vm.itemToCopy = item
                            true
                        })
                    }
                    add(CustomAccessibilityAction(openEditingLabel) {
                        KtsHaptics.vibrateAction(context)
                        vm.openInEditingMode(item, openedEditingMsg)
                        true
                    })
                    add(CustomAccessibilityAction(openSelectionLabel) {
                        KtsHaptics.vibrateAction(context)
                        vm.openInSelectionMode(item, openedSelectionMsg)
                        true
                    })
                    add(CustomAccessibilityAction(openWindowLabel) {
                        KtsHaptics.vibrateAction(context)
                        vm.customWindowData = Pair(item.text) { updated ->
                            vm.updateClipboardItemText(context, item.id, updated)
                        }
                        true
                    })
                }
            }
            .combinedClickable(
                onClick = {
                    KtsHaptics.vibrateSelect(context)
                    vm.openInReadingMode(item, openedReadingMsg)
                },
                onLongClick = {
                    KtsHaptics.vibrateLongPress(context)
                    vm.activeClipboardSheetItem = item
                },
                onLongClickLabel = moreActionsLabel,
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
        border = BorderStroke(
            1.dp,
            if (item.isFavorite) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = formatClipboardTimestamp(item.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    val folderName = if (item.folderId != null) {
                        vm.favoriteFolders.find { it.id == item.folderId }?.name
                    } else null

                    if (folderName != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        ) {
                            Text(
                                text = folderName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }

                    if (item.isFavorite) {
                        Icon(
                            imageVector = Icons.Outlined.Star,
                            contentDescription = stringResource(R.string.tab_favorite),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Text(
                        text = stringResource(R.string.clipboard_char_count, item.text.length),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = item.text,
                fontSize = vm.fontSize.sp,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = (vm.fontSize * 1.35f).sp,
            )

        }
    }
}

private fun formatClipboardTimestamp(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryActionSheet(
    vm: KtsViewModel,
    item: ClipboardItem,
) {
    val context = LocalView.current.context
    val clipboardManager = LocalClipboardManager.current
    val copiedMsg = stringResource(R.string.copied)
    val deletedMsg = stringResource(R.string.item_deleted)
    val addedFavMsg = stringResource(R.string.added_to_favorites)
    val removedFavMsg = stringResource(R.string.removed_from_favorites)
    val openedReadingMsg = stringResource(R.string.opened_in_reading)
    val openedEditingMsg = stringResource(R.string.opened_in_editing)
    val openedSelectionMsg = stringResource(R.string.opened_in_selection)

    ModalBottomSheet(onDismissRequest = { vm.activeClipboardSheetItem = null }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.more_actions),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                TextButton(
                    onClick = {
                        KtsHaptics.vibrateAction(context)
                        vm.activeClipboardSheetItem = null
                        if (vm.tab == AppTab.History) {
                            vm.removeFromHistory(context, item, deletedMsg)
                        } else {
                            vm.deleteCompletely(context, item, deletedMsg)
                        }
                        Toast.makeText(context, deletedMsg, Toast.LENGTH_SHORT).show()
                    },
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.delete),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // 1. Add/Remove Favorite
            TextButton(
                onClick = {
                    KtsHaptics.vibrateAction(context)
                    vm.toggleHistoryFavorite(context, item, addedFavMsg, removedFavMsg)
                    vm.activeClipboardSheetItem = null
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        if (item.isFavorite) Icons.Outlined.StarBorder else Icons.Outlined.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(if (item.isFavorite) R.string.action_remove_favorite else R.string.action_add_favorite),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            // 1b. Edit Text
            TextButton(
                onClick = {
                    KtsHaptics.vibrateAction(context)
                    vm.activeClipboardSheetItem = null
                    vm.editingClipboardItem = item
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.edit),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            if (vm.favoriteFolders.isNotEmpty() && (vm.tab == AppTab.Favorite || item.isFavorite)) {
                // 1c. Move to Folder
                TextButton(
                    onClick = {
                        KtsHaptics.vibrateAction(context)
                        vm.activeClipboardSheetItem = null
                        vm.itemToMove = item
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Outlined.DriveFileMove,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            stringResource(R.string.action_move_to_folder),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }

                // 1d. Copy to Folder
                TextButton(
                    onClick = {
                        KtsHaptics.vibrateAction(context)
                        vm.activeClipboardSheetItem = null
                        vm.itemToCopy = item
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Outlined.FileCopy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            stringResource(R.string.action_copy_to_folder),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }

            // 2. Open in Reading Mode
            TextButton(
                onClick = {
                    KtsHaptics.vibrateAction(context)
                    vm.openInReadingMode(item, openedReadingMsg)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.MenuBook,
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.action_open_reading),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            // 3. Open in Editing Mode
            TextButton(
                onClick = {
                    KtsHaptics.vibrateAction(context)
                    vm.openInEditingMode(item, openedEditingMsg)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.action_open_editing),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            // 4. Open in Selection Mode
            TextButton(
                onClick = {
                    KtsHaptics.vibrateAction(context)
                    vm.openInSelectionMode(item, openedSelectionMsg)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.CheckBox,
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.action_open_selection),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            // 4b. Open in Window
            TextButton(
                onClick = {
                    KtsHaptics.vibrateAction(context)
                    vm.customWindowData = Pair(item.text) { updated ->
                        vm.updateClipboardItemText(context, item.id, updated)
                    }
                    vm.activeClipboardSheetItem = null
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.OpenInNew,
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.open_window),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            // 5. Copy Text
            TextButton(
                onClick = {
                    KtsHaptics.vibrateSelect(context)
                    clipboardManager.setText(AnnotatedString(item.text))
                    Toast.makeText(context, copiedMsg, Toast.LENGTH_SHORT).show()
                    vm.announce = copiedMsg
                    vm.activeClipboardSheetItem = null
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.ContentCopy,
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.action_copy),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            // 6. Listen (TTS)
            TextButton(
                onClick = {
                    KtsHaptics.vibrateAction(context)
                    BackgroundTtsManager.speak(item.text, "HistoryItemTTS")
                    vm.activeClipboardSheetItem = null
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.PlayArrow,
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.action_listen),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            // 7. Delete (History / Favorite)
            TextButton(
                onClick = {
                    KtsHaptics.vibrateAction(context)
                    vm.activeClipboardSheetItem = null
                    if (vm.tab == AppTab.History) {
                        vm.removeFromHistory(context, item, deletedMsg)
                    } else {
                        vm.deleteCompletely(context, item, deletedMsg)
                    }
                    Toast.makeText(context, deletedMsg, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.delete),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FavoriteFolderCard(
    folder: FavoriteFolder,
    vm: KtsViewModel,
) {
    val context = LocalView.current.context
    val openedMsg = stringResource(R.string.folder_opened_msg)
    val renameLabel = stringResource(R.string.action_rename)
    val deleteLabel = stringResource(R.string.dialog_delete_folder_title)
    val moreActionsLabel = stringResource(R.string.more_actions)
    val accessibilityDesc = stringResource(R.string.folder_item_count_desc, folder.name, folder.itemCount)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .combinedClickable(
                onClick = {
                    KtsHaptics.vibrateSelect(context)
                    vm.openFolder(context, folder, openedMsg)
                },
                onLongClick = {
                    KtsHaptics.vibrateLongPress(context)
                    vm.folderToManage = folder
                },
                onLongClickLabel = moreActionsLabel,
            )
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = accessibilityDesc
                customActions = listOf(
                    CustomAccessibilityAction(label = renameLabel) {
                        KtsHaptics.vibrateAction(context)
                        vm.folderToManage = folder
                        vm.showRenameFolderDialog = true
                        true
                    },
                    CustomAccessibilityAction(label = deleteLabel) {
                        KtsHaptics.vibrateAction(context)
                        vm.folderToManage = folder
                        vm.showDeleteFolderDialog = true
                        true
                    },
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.items_count_format, folder.itemCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CreateFolderDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_create_folder_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.folder_name_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank(),
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun RenameFolderDialog(
    folder: FavoriteFolder,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember(folder) { mutableStateOf(folder.name) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_rename_folder_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.folder_name_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank() && name.trim() != folder.name,
            ) {
                Text(stringResource(R.string.action_rename))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun DeleteFolderDialog(
    folder: FavoriteFolder,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_delete_folder_title)) },
        text = {
            Text(stringResource(R.string.dialog_delete_folder_msg))
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FolderManageSheet(
    folder: FavoriteFolder,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = folder.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(12.dp))
            TextButton(
                onClick = onRename,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(R.string.action_rename))
                }
            }
            TextButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(R.string.dialog_delete_folder_title), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun FolderSelectionDialog(
    title: String,
    folders: List<FavoriteFolder>,
    onSelectFolder: (targetFolderId: Long?, folderName: String) -> Unit,
    onCreateNewFolder: () -> Unit,
    onDismiss: () -> Unit,
) {
    val rootLabel = stringResource(R.string.move_to_root_option)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title)
                IconButton(onClick = onCreateNewFolder) {
                    Icon(
                        Icons.Outlined.CreateNewFolder,
                        contentDescription = stringResource(R.string.action_new_folder),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                // Root (Remove from folder) option
                TextButton(
                    onClick = { onSelectFolder(null, rootLabel) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Outlined.FolderOpen, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Text(rootLabel, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                // Custom Folders
                folders.forEach { folder ->
                    TextButton(
                        onClick = { onSelectFolder(folder.id, folder.name) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Outlined.Folder, contentDescription = null)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "${folder.name} (${folder.itemCount})",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@Composable
private fun MainMoreOptionsMenu(vm: KtsViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalView.current.context

    Box {
        IconButton(onClick = {
            KtsHaptics.vibrateSelect(context)
            expanded = true
        }) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = stringResource(R.string.menu_more_options),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.tab_settings)) },
                onClick = {
                    expanded = false
                    KtsHaptics.vibrateSelect(context)
                    vm.openSettings()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = null,
                    )
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_help)) },
                onClick = {
                    expanded = false
                    KtsHaptics.vibrateSelect(context)
                    vm.openHelp()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.HelpOutline,
                        contentDescription = null,
                    )
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_check_updates)) },
                onClick = {
                    expanded = false
                    KtsHaptics.vibrateSelect(context)
                    val activity = context.findActivity()
                    if (activity != null) {
                        AppUpdateManager.getInstance().checkForUpdates(activity, isManualCheck = true)
                    }
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.SystemUpdate,
                        contentDescription = null,
                    )
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_about)) },
                onClick = {
                    expanded = false
                    KtsHaptics.vibrateSelect(context)
                    vm.openAbout()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

@Composable
private fun HelpPane() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
    ) {
        Text(
            text = "User Guide & Feature Documentation",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Text(
            text = "Complete guide to reading, editing, organizing, and navigating texts in KTS Text Solution.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        // Category 1: Overview
        HelpCategoryCard(
            title = "1. Overview & Core Concept",
            icon = Icons.AutoMirrored.Outlined.MenuBook,
        ) {
            HelpItem(
                headline = "Accessible Text Solution",
                explanation = "KTS Text Solution is designed specifically for comfortable, accessible text navigation, editing, and Text-to-Speech (TTS) reading. It provides first-class support for TalkBack and screen reader users with semantic headings, custom actions, and haptic feedback.",
            )
            HelpItem(
                headline = "Chunk-Based Navigation",
                explanation = "Instead of overwhelming you with long walls of text, documents are split into manageable chunks (paragraphs, sentences, lines, words, or character blocks) that can be read, edited, or spoken one at a time.",
            )
            HelpItem(
                headline = "Top App Bar & More Options",
                explanation = "The top bar gives you quick access to Favorite, New Document (+), Play/Stop speech, and the 'More options' (three dots) menu containing Settings, Help, and About.",
            )
        }

        // Category 2: Starting a New Document
        HelpCategoryCard(
            title = "2. Starting a New Document & Adding Text",
            icon = Icons.AutoMirrored.Outlined.NoteAdd,
        ) {
            HelpItem(
                headline = "New Document Button (+)",
                explanation = "Tap the '+' (New Text) button in the top bar or inside the History screen to open a clean workspace ready for writing or pasting content.",
            )
            HelpItem(
                headline = "Pasting Text",
                explanation = "Use the 'Paste' button in the bottom action bar or long-press inside the editor to paste copied text from other apps or websites.",
            )
            HelpItem(
                headline = "Automatic Clipboard Logging",
                explanation = "When enabled in Settings, any text you copy anywhere on your Android phone is automatically saved to History so you never lose important snippets.",
            )
            HelpItem(
                headline = "Quick-Access Floating Window",
                explanation = "Enable the floating window in Settings to automatically show a quick pop-up window whenever you copy text, allowing immediate chunk reading or editing from any app.",
            )
        }

        // Category 3: Text Splitting Modes
        HelpCategoryCard(
            title = "3. Text Splitting Modes & Navigation",
            icon = Icons.AutoMirrored.Outlined.Segment,
        ) {
            HelpItem(
                headline = "Paragraph Mode",
                explanation = "Splits text by paragraph breaks (empty lines). Ideal for reading long articles, book chapters, and essays.",
            )
            HelpItem(
                headline = "Sentence Mode",
                explanation = "Splits text sentence by sentence using punctuation boundaries (period, question mark, exclamation mark). Perfect for language study and careful proofreading.",
            )
            HelpItem(
                headline = "Line Mode",
                explanation = "Splits text line by line. Great for lyrics, poetry, code snippets, or structured lists.",
            )
            HelpItem(
                headline = "Words Mode",
                explanation = "Splits text into fixed word counts. You can customize the word size (from 5 to 50 words per chunk) in Settings.",
            )
            HelpItem(
                headline = "Character Mode",
                explanation = "Splits text into exact character limits (from 20 to 300 characters per chunk) configured in Settings.",
            )
            HelpItem(
                headline = "Chunk Navigation Bar",
                explanation = "Navigate between chunks with the Previous and Next buttons. The counter displays your exact position (e.g. 'Chunk 3 of 15') and character count.",
            )
        }

        // Category 4: Reading Mode & TTS
        HelpCategoryCard(
            title = "4. Reading Mode & Text-to-Speech (TTS)",
            icon = Icons.Outlined.PlayArrow,
        ) {
            HelpItem(
                headline = "Listen with Text-to-Speech",
                explanation = "Tap the Play button in the top bar to start reading aloud from the current chunk. Tap the Stop button anytime to pause speech.",
            )
            HelpItem(
                headline = "Double-Tap Chunk for Focused Reading",
                explanation = "Double-tapping any chunk opens the focused Reading Modal with larger text and dedicated action options.",
            )
            HelpItem(
                headline = "Background Reading",
                explanation = "Enable 'Background Reading' in Settings to continue listening even when your phone screen is off or while using other apps.",
            )
            HelpItem(
                headline = "Speech Filters",
                explanation = "Select Clean, Moderate, or Strict speech filter in Settings to automatically skip emojis, symbols, numbers, punctuation, or URLs during speech narration.",
            )
        }

        // Category 5: Editing Mode, Undo & Search
        HelpCategoryCard(
            title = "5. Editing Mode, Undo & Find/Replace",
            icon = Icons.Outlined.Edit,
        ) {
            HelpItem(
                headline = "Real-Time Chunk Editing",
                explanation = "In Editing mode, modify the text of the active chunk directly. Any changes you make are instantly synchronized with the entire document.",
            )
            HelpItem(
                headline = "Undo Changes",
                explanation = "Tap the Undo button in the top bar to easily revert the last edit.",
            )
            HelpItem(
                headline = "Find & Replace",
                explanation = "Tap the Find & Replace icon in the top bar to search for specific words or phrases and replace single matches or all occurrences at once.",
            )
            HelpItem(
                headline = "Auto-Saving",
                explanation = "Text edits are automatically preserved in the current session so you don't lose your work.",
            )
        }

        // Category 6: Selection Mode & Bulk Actions
        HelpCategoryCard(
            title = "6. Selection Mode & Bulk Actions",
            icon = Icons.Outlined.CheckBox,
        ) {
            HelpItem(
                headline = "Multi-Chunk Selection",
                explanation = "In Selection mode, each chunk has a checkbox. Select multiple chunks to perform bulk operations.",
            )
            HelpItem(
                headline = "Select All & Invert",
                explanation = "Use 'Select All' to quickly select the entire document, or 'Invert' to switch selected and unselected chunks.",
            )
            HelpItem(
                headline = "Bulk Cut, Copy & Delete",
                explanation = "Cut, Copy, or Delete all selected chunks together using the bottom action bar buttons.",
            )
            HelpItem(
                headline = "Format Selected Text (Font Size, Bold, Italic, Underline)",
                explanation = "Tap 'Format' in the selection header, bottom action bar, or options menu to customize font size, bold, italic, and underline styling for all selected chunks with live preview.",
            )
        }

        // Category 7: Bottom Action Bar
        HelpCategoryCard(
            title = "7. Bottom Action Bar (Cut, Copy, Delete, Save, Paste)",
            icon = Icons.Outlined.ContentCut,
        ) {
            HelpItem(
                headline = "Cut",
                explanation = "Cuts the current chunk (or all selected chunks in Selection mode) to your clipboard and removes it from the document.",
            )
            HelpItem(
                headline = "Copy",
                explanation = "Copies the current chunk (or selected chunks) to the system clipboard.",
            )
            HelpItem(
                headline = "Delete",
                explanation = "Safely deletes the current chunk (or selected chunks) with confirmation.",
            )
            HelpItem(
                headline = "Save",
                explanation = "Saves the current document to storage and updates your active session.",
            )
            HelpItem(
                headline = "Paste",
                explanation = "Pastes text from your clipboard directly into the workspace.",
            )
        }

        // Category 8: History System
        HelpCategoryCard(
            title = "8. History System & Quick Open",
            icon = Icons.Outlined.History,
        ) {
            HelpItem(
                headline = "Double-Tap to Read",
                explanation = "Double-tap any item in the History list to immediately open it in Reading mode.",
            )
            HelpItem(
                headline = "Double-Tap & Hold / Custom Actions",
                explanation = "Double-tap and hold any item (or use the TalkBack Custom Actions menu) to Delete, Edit, Star as Favorite, Copy, or Share.",
            )
            HelpItem(
                headline = "Clear History Button",
                explanation = "Tap the 'Clear' button at the bottom-right corner of the History screen to remove all history entries with a confirmation safety prompt.",
            )
            HelpItem(
                headline = "New Document from History",
                explanation = "Tap the '+' (New Text) button in the top bar while in History to start a new document anytime.",
            )
        }

        // Category 9: Favorites & Folder Organization
        HelpCategoryCard(
            title = "9. Favorites & Folder Management",
            icon = Icons.Outlined.Star,
        ) {
            HelpItem(
                headline = "Organizing with Folders",
                explanation = "Create custom folders (e.g. Articles, Work, Study, Notes) to organize your favorite texts cleanly.",
            )
            HelpItem(
                headline = "Folder Navigation",
                explanation = "Tap any folder to view its contents without extra swipe clutter. Use the back button to return to all folders.",
            )
            HelpItem(
                headline = "Double-Tap to Read",
                explanation = "Double-tap any favorite text to immediately launch it in Reading mode.",
            )
            HelpItem(
                headline = "Move Between Folders",
                explanation = "Open an item's action menu to move it into another folder or back to the root list.",
            )
            HelpItem(
                headline = "Create & Rename Folders",
                explanation = "Use the folder management icons in the top bar to create new folders or rename existing ones.",
            )
        }

        // Category 10: Multi-Text Sessions
        HelpCategoryCard(
            title = "10. Multi-Text Sessions & Fast Switching",
            icon = Icons.Outlined.FileCopy,
        ) {
            HelpItem(
                headline = "Open Multiple Documents",
                explanation = "Open multiple texts simultaneously from History or Favorites. Each open text maintains its own reading position and split mode.",
            )
            HelpItem(
                headline = "Open Texts Tabs Bar",
                explanation = "A horizontal bar shows all currently opened documents. Tap a tab to switch to it, or tap the 'X' button to close it.",
            )
            HelpItem(
                headline = "Two-Finger Swipe to Switch",
                explanation = "Swipe left or right with two fingers anywhere on the workspace to quickly flip to the next or previous open document.",
            )
            HelpItem(
                headline = "TalkBack Accessibility Actions",
                explanation = "TalkBack users can easily switch between open texts using the TalkBack Custom Actions menu ('Next Text' and 'Previous Text').",
            )
        }

        // Category 11: Settings, Backup & Accessibility
        HelpCategoryCard(
            title = "11. Settings, Backup & Accessibility",
            icon = Icons.Outlined.Settings,
        ) {
            HelpItem(
                headline = "TTS Voice & Speed Customization",
                explanation = "Customize speech pitch, speech rate, and language to match your listening preferences.",
            )
            HelpItem(
                headline = "Backup & Restore",
                explanation = "Export all your history items, favorites, and folders to a secure JSON file on your phone. You can restore your data anytime.",
            )
            HelpItem(
                headline = "Accessibility First Design",
                explanation = "Includes high contrast modes, large readable fonts, comprehensive TalkBack announcements, and customized haptic feedback for every gesture.",
            )
        }
    }
}

@Composable
private fun HelpCategoryCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 10.dp),
            )
            content()
        }
    }
}

@Composable
private fun HelpItem(
    headline: String,
    explanation: String,
) {
    Column(modifier = Modifier.padding(vertical = 5.dp)) {
        Text(
            text = headline,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = explanation,
            style = MaterialTheme.typography.bodySmall,
            lineHeight = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AboutPane() {
    val context = LocalView.current.context
    val email = stringResource(R.string.about_developer_email)
    val emailCopiedMsg = stringResource(R.string.email_copied)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(16.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.MenuBook,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Version 1.0.0",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            ) {
                Text(
                    text = stringResource(R.string.about_developer_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.about_developer_name),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:$email")
                                putExtra(Intent.EXTRA_SUBJECT, "KTS Text Solution Feedback")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Outlined.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.action_send_email))
                    }
                    OutlinedButton(
                        onClick = {
                            KtsHaptics.vibrateAction(context)
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Developer Email", email))
                            Toast.makeText(context, emailCopiedMsg, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.action_copy_email))
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            ) {
                Text(
                    text = "About KTS Text Solution",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "KTS Text Solution is a specialized, accessibility-first text reading, editing, and management tool. Built with high TalkBack compatibility, powerful text chunking (paragraph, sentence, line, words, characters), seamless TTS narration, clipboard history, organized favorite folders, and rapid multi-text switching to empower users to read and work comfortably.",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
