package com.maxigimenez.nodes.features.nodes.entries

import android.net.Uri
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.maxigimenez.nodes.R
import com.maxigimenez.nodes.core.ui.theme.entranceAnimation
import com.maxigimenez.nodes.core.ui.theme.staggeredDelay
import com.maxigimenez.nodes.core.ui.components.CategoryFilterRow
import com.maxigimenez.nodes.core.ui.components.CreateChecklistDialog
import com.maxigimenez.nodes.core.ui.components.NodesBackground
import com.maxigimenez.nodes.data.model.ChecklistItem
import com.maxigimenez.nodes.data.model.EntryType
import com.maxigimenez.nodes.data.model.NodeEntry
import com.maxigimenez.nodes.features.auth.AuthViewModel
import com.maxigimenez.nodes.features.nodes.components.ChecklistBubble
import com.maxigimenez.nodes.features.nodes.components.CreateMessageDialog
import com.maxigimenez.nodes.features.nodes.components.CreateReminderDialog
import com.maxigimenez.nodes.features.nodes.components.ImageBubble
import com.maxigimenez.nodes.features.nodes.components.FullScreenImageViewer
import com.maxigimenez.nodes.features.nodes.components.MessageBubble
import com.maxigimenez.nodes.features.nodes.components.NodeEntriesControls
import com.maxigimenez.nodes.features.nodes.components.ReminderBubble
import com.maxigimenez.nodes.core.util.ToastHelper
import com.maxigimenez.nodes.features.nodes.list.NodesViewModel
import com.maxigimenez.nodes.core.util.ImageUtils
import com.maxigimenez.nodes.features.nodes.entries.AlarmScheduler
import kotlinx.coroutines.launch

@Composable
fun NodeChatScreen(
    nodeId: String?,
    modifier: Modifier = Modifier,
    outerPadding: PaddingValues,
    nodesViewModel: NodesViewModel,
    authViewModel: AuthViewModel,
) {
    val userId by authViewModel.userId.observeAsState()

    val entries by nodesViewModel.nodeEntries.collectAsStateWithLifecycle()
    val nodeMembers by nodesViewModel.nodeMembers.collectAsState()
    val selectedNode by nodesViewModel.selectedNode.collectAsState()
    val currentUserProfile by authViewModel.userProfile.collectAsState()
    val blockedUsers = currentUserProfile?.blockedUsers ?: emptyList()
    val userInfo by nodesViewModel.userInfo.collectAsStateWithLifecycle()
    val isSendingImage by nodesViewModel.isSendingImage.collectAsStateWithLifecycle()

    val amIAdmin = selectedNode?.admins?.contains(userId) == true || selectedNode?.ownerId == userId

    val categories by nodesViewModel.availableCategories.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val currentFilter: String? = selectedCategory

    val filteredEntries = remember(entries, currentFilter) {
        if (currentFilter == null) {
            entries
        } else {
            entries.filter { it.category == currentFilter }
        }
    }

    val context = LocalContext.current
    val listState = rememberLazyListState()

    var isFabMenuExpanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(EntryType.NONE) }
    var messageEntry by remember { mutableStateOf("") }

    var showEntryOptionsDialog by remember { mutableStateOf(false) }
    var selectedEntry by remember { mutableStateOf<NodeEntry?>(null) }
    var showCategoryDialog by remember { mutableStateOf(value = false) }
    var showReportConfirmDialog by remember { mutableStateOf(false) }
    var fullscreenImageUrl by remember { mutableStateOf<String?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val anonUser = stringResource(R.string.chat_anonymous_user)
    val imgError = stringResource(R.string.workspace_detail_image_process_error)
    val photoError = stringResource(R.string.workspace_detail_photo_process_error)
    val cameraDenied = stringResource(R.string.workspace_detail_camera_permission_denied)

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(), onResult = { uri ->
            uri?.let {
                if (nodeId != null) {
                    nodesViewModel.addNodeImageEntry(
                        nodeId = nodeId,
                        imageUri = uri,
                        name = authViewModel.userProfile.value?.firstName ?: anonUser,
                        profilePicture = authViewModel.userProfile.value?.profilePicture ?: ""
                    )
                } else {
                    ToastHelper.showToast(context, imgError)
                }
            }
            showDialog = EntryType.NONE
        })

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(), onResult = { success ->
            if (success) {
                tempCameraUri?.let { uri ->
                    if (nodeId != null) {
                        nodesViewModel.addNodeImageEntry(
                            nodeId = nodeId,
                            imageUri = uri,
                            name = authViewModel.userProfile.value?.firstName ?: anonUser,
                            profilePicture = authViewModel.userProfile.value?.profilePicture ?: ""
                        )
                    } else {
                        ToastHelper.showToast(context, photoError)
                    }
                }
            }
            showDialog = EntryType.NONE
        })

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(), onResult = { isGranted ->
            if (isGranted) {
                val uri = ImageUtils.createTempImageUri(context)
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            } else {
                showDialog = EntryType.NONE
                ToastHelper.showToast(context, cameraDenied)
            }
        })

    LaunchedEffect(filteredEntries.size) {
        if (filteredEntries.isNotEmpty()) {
            listState.animateScrollToItem(filteredEntries.size - 1)
        }
    }

    LaunchedEffect(entries) {
        val reminders = entries.filter { it.type == EntryType.REMINDER }
        for (entry in reminders) {
            val itemsList = entry.items
            val reminderItem = itemsList?.firstOrNull()
            val isChecked = reminderItem?.isChecked ?: false

            if (isChecked) AlarmScheduler.schedule(context, entry, nodeId)
            else AlarmScheduler.cancel(context, entry.id)
        }
    }

    LaunchedEffect(userInfo) {
        userInfo?.let { message ->
            ToastHelper.showToast(context, message)
            nodesViewModel.clearUserInfo()
        }
    }

    LaunchedEffect(nodeId) {
        nodeId?.let { nodesViewModel.loadNodeEntries(it) }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Capa 1: Fondo Inmersivo
        NodesBackground(speedMultiplier = 0.2f, nodeCount = 15)

        // Capa 2: UI (Desplazada sobre la BottomBar)
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .padding(outerPadding), // <--- Eleva el Scaffold sobre la barra global
            containerColor = Color.Transparent, 
            floatingActionButton = {
                NodeEntriesControls(
                    isFabMenuExpanded = isFabMenuExpanded,
                    onFabClicked = { isFabMenuExpanded = !isFabMenuExpanded },
                    // El imePadding se aplica al contenido del FAB para subir con el teclado
                    modifier = Modifier.imePadding(),
                    onMessage = {
                        isFabMenuExpanded = false
                        showDialog = EntryType.MESSAGE
                    },
                    onCheckList = {
                        isFabMenuExpanded = false
                        showDialog = EntryType.CHECK_LIST
                    },
                    onImage = {
                        isFabMenuExpanded = false
                        showDialog = EntryType.IMAGE
                    },
                    onReminder = {
                        isFabMenuExpanded = false
                        showDialog = EntryType.REMINDER
                    }
                )
            }) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
            ) {

                CategoryFilterRow(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { category ->
                        selectedCategory = if (selectedCategory == category) null else category
                    })

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    // Padding extra para que el último mensaje no quede detrás del FAB
                    contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
                    reverseLayout = false
                ) {
                    itemsIndexed(filteredEntries, key = { _, entry -> entry.id }) { index, entry ->
                        val isBlocked = blockedUsers.contains(entry.userId)
                        val user = nodeMembers.find { it.id == entry.userId }
                        val profilePicture = user?.profilePicture ?: ""
                        val isFromCurrentUser = entry.userId == userId

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .entranceAnimation(delay = staggeredDelay(index, 30))
                                .pointerInput(entry.id) {
                                    detectTapGestures(
                                        onLongPress = {
                                            selectedEntry = entry
                                            showEntryOptionsDialog = true
                                        })
                                }) {
                            if (isBlocked) {
                                BlockedMessageItem(isFromCurrentUser)
                            } else {
                                when (entry.type) {
                                    EntryType.MESSAGE -> {
                                        MessageBubble(
                                            entry = entry,
                                            userName = entry.name,
                                            isFromCurrentUser = isFromCurrentUser,
                                            profilePicture = profilePicture
                                        )
                                    }

                                    EntryType.IMAGE -> {
                                        ImageBubble(
                                            entry = entry,
                                            userName = entry.name,
                                            isFromCurrentUser = isFromCurrentUser,
                                            profilePicture = profilePicture,
                                            onImageClick = { fullscreenImageUrl = it }
                                        )
                                    }

                                    EntryType.CHECK_LIST -> {
                                        ChecklistBubble(
                                            entry = entry,
                                            userName = entry.name,
                                            isFromCurrentUser = isFromCurrentUser,
                                            profilePicture = profilePicture,
                                            onItemCheckedChange = { itemIndex, newCheckedState ->
                                                nodeId?.let {
                                                    nodesViewModel.updateChecklistItemState(
                                                        nodeId = it,
                                                        entryId = entry.id,
                                                        itemIndex = itemIndex,
                                                        newCheckedState = newCheckedState
                                                    )
                                                }
                                            })
                                    }

                                    EntryType.REMINDER -> {
                                        ReminderBubble(
                                            entry = entry,
                                            userName = entry.name,
                                            isFromCurrentUser = isFromCurrentUser,
                                            profilePicture = profilePicture,
                                            onItemStateChange = { index, isChecked ->
                                                nodeId?.let {
                                                    nodesViewModel.updateChecklistItemState(
                                                        nodeId = it,
                                                        entryId = entry.id,
                                                        itemIndex = index,
                                                        newCheckedState = isChecked
                                                    )
                                                }
                                            })
                                    }

                                    else -> {}
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    when (showDialog) {
        EntryType.MESSAGE -> {
            CreateMessageDialog(
                messageValue = messageEntry,
                onMessageChanged = { messageEntry = it },
                onConfirm = {
                    nodeId?.let {
                        nodesViewModel.addNodeEntry(
                            nodeId = it,
                            value = messageEntry,
                            type = EntryType.MESSAGE,
                            name = authViewModel.userProfile.value?.firstName ?: anonUser,
                            profilePicture = authViewModel.userProfile.value?.profilePicture
                                ?: ""
                        )
                    }
                    showDialog = EntryType.NONE
                    messageEntry = ""
                },
                onDismiss = {
                    showDialog = EntryType.NONE
                    messageEntry = ""
                })
        }

        EntryType.CHECK_LIST -> {
            CreateChecklistDialog(
                onDismiss = { showDialog = EntryType.NONE },
                onConfirm = { title, items ->
                    nodeId?.let {
                        nodesViewModel.addNodeEntry(
                            nodeId = it,
                            value = title,
                            type = EntryType.CHECK_LIST,
                            name = authViewModel.userProfile.value?.firstName ?: anonUser,
                            profilePicture = authViewModel.userProfile.value?.profilePicture
                                ?: "",
                            items = items
                        )
                    }
                    showDialog = EntryType.NONE
                })
        }

        EntryType.IMAGE -> {
            AlertDialog(
                onDismissRequest = { showDialog = EntryType.NONE },
                title = { Text(stringResource(R.string.chat_add_image_title)) },
                text = { Text(stringResource(R.string.profile_pic_change_prompt)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }) { Text(stringResource(R.string.common_gallery)) }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            val permission = android.Manifest.permission.CAMERA
                            val permissionCheckResult =
                                ContextCompat.checkSelfPermission(context, permission)
                            if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                                val uri = ImageUtils.createTempImageUri(context)
                                tempCameraUri = uri
                                cameraLauncher.launch(uri)
                            } else {
                                cameraPermissionLauncher.launch(permission)
                            }
                        }) { Text(stringResource(R.string.common_camera)) }
                })
        }

        EntryType.REMINDER -> {
            CreateReminderDialog(onDismiss = {
                showDialog = EntryType.NONE
            }, onConfirm = { title, items ->
                nodeId?.let {
                    nodesViewModel.addNodeEntry(
                        nodeId = it,
                        value = title,
                        type = EntryType.REMINDER,
                        name = authViewModel.userProfile.value?.firstName ?: anonUser,
                        profilePicture = authViewModel.userProfile.value?.profilePicture ?: "",
                        items = items
                    )
                }
                showDialog = EntryType.NONE
            })
        }

        EntryType.NONE -> {}
    }

    if (showEntryOptionsDialog) {
        val entryToManage = selectedEntry
        val isFromCurrentUser = entryToManage?.userId == userId

        AlertDialog(onDismissRequest = {
            showEntryOptionsDialog = false
            selectedEntry = null
        },
            title = { Text(stringResource(R.string.chat_entry_options_title)) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.chat_entry_option_add_category),
                        modifier = Modifier
                            .clickable {
                                showEntryOptionsDialog = false
                                showCategoryDialog = true
                            }
                            .padding(vertical = 12.dp)
                            .fillMaxWidth())

                    if (!isFromCurrentUser) {
                        Text(
                            text = stringResource(R.string.chat_entry_option_report),
                            modifier = Modifier
                                .clickable {
                                    showEntryOptionsDialog = false
                                    showReportConfirmDialog = true
                                }
                                .padding(vertical = 12.dp)
                                .fillMaxWidth())
                    }

                    if (isFromCurrentUser || amIAdmin) {
                        Text(
                            text = stringResource(R.string.chat_entry_option_delete),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .clickable {
                                    if (entryToManage != null && nodeId != null) {
                                        nodesViewModel.deleteNodeEntry(
                                            nodeId = nodeId, entryId = entryToManage.id
                                        )
                                    }
                                    showEntryOptionsDialog = false
                                    selectedEntry = null
                                }
                                .padding(vertical = 12.dp)
                                .fillMaxWidth())
                    }
                }
            }, confirmButton = {
                TextButton(
                    onClick = {
                        showEntryOptionsDialog = false
                        selectedEntry = null
                    }) {
                    Text(stringResource(R.string.common_close))
                }
            })
    }

    if (showCategoryDialog) {
        var categoryName by remember { mutableStateOf(selectedEntry?.category ?: "") }

        AlertDialog(onDismissRequest = {
            showCategoryDialog = false
            selectedEntry = null
        },

            title = { Text(stringResource(R.string.chat_assign_category_title)) },
            text = {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text(stringResource(R.string.chat_category_name_label)) },
                    singleLine = true
                )
            }, confirmButton = {
                TextButton(
                    onClick = {
                        if (nodeId != null && selectedEntry != null) {
                            nodesViewModel.updateEntryCategory(
                                nodeId = nodeId,
                                entryId = selectedEntry!!.id,
                                newCategory = categoryName
                            )
                        }
                        showCategoryDialog = false
                        selectedEntry = null
                    }) {
                    Text(stringResource(R.string.common_save))
                }
            }, dismissButton = {
                TextButton(
                    onClick = {
                        showCategoryDialog = false
                        selectedEntry = null
                    }) {
                    Text(stringResource(R.string.common_cancel))
                }
            })
    }

    if (showReportConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showReportConfirmDialog = false
                selectedEntry = null
            },
            title = { Text(stringResource(R.string.report_dialog_title)) },
            text = { Text(stringResource(R.string.report_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (nodeId != null && selectedEntry != null) {
                            nodesViewModel.reportEntry(nodeId, selectedEntry!!)
                        }
                        showReportConfirmDialog = false
                        selectedEntry = null
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showReportConfirmDialog = false
                        selectedEntry = null
                    }
                ) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    fullscreenImageUrl?.let { url ->
        FullScreenImageViewer(imageUrl = url) {
            fullscreenImageUrl = null
        }
    }

    if (isSendingImage) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.chat_image_uploading_status),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BlockedMessageItem(isFromCurrentUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = stringResource(R.string.chat_blocked_message),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
