package com.maxigimenez.nodes.features.nodes.detail

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.maxigimenez.nodes.core.ui.components.ShimmerImageLoader
import com.maxigimenez.nodes.core.ui.theme.entranceAnimation
import com.maxigimenez.nodes.core.ui.components.CharacterLimitedOutlinedTextField
import com.maxigimenez.nodes.core.util.ImageUtils
import com.maxigimenez.nodes.core.util.ToastHelper
import com.maxigimenez.nodes.R
import com.maxigimenez.nodes.features.auth.AuthViewModel
import com.maxigimenez.nodes.features.nodes.list.NodesViewModel
import com.maxigimenez.nodes.features.nodes.entries.NodeChatScreen
import com.maxigimenez.nodes.data.model.UserProfile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeDetailScreen(
    nodeId: String,
    nodesViewModel: NodesViewModel,
    contentNavController: NavController,
    modifier: Modifier,
    authViewModel: AuthViewModel,
    outerPadding: PaddingValues,
) {
    LaunchedEffect(key1 = nodeId) {
        nodesViewModel.loadNodeDetails(nodeId)
    }

    val node by nodesViewModel.selectedNode.collectAsState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .padding(outerPadding), // <--- IMPORTANTE: Desplaza el detalle sobre la BottomBar
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = node?.name ?: stringResource(R.string.common_loading),
                        maxLines = 1,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { contentNavController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { contentNavController.navigate("info/$nodeId") }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.workspace_detail_info_description)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        }
    ) { innerPadding ->
        NodeChatScreen(
            nodeId = nodeId,
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(innerPadding)
                .entranceAnimation(),
            outerPadding = innerPadding,
            nodesViewModel = nodesViewModel,
            authViewModel = authViewModel,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeDetailsContent(
    nodesViewModel: NodesViewModel,
    authViewModel: AuthViewModel,
    modifier: Modifier,
    contentNavController: NavController,
    outerPadding: PaddingValues,
) {
    val node by nodesViewModel.selectedNode.collectAsState()
    val members by nodesViewModel.nodeMembers.collectAsStateWithLifecycle()
    val currentUserProfile by authViewModel.userProfile.collectAsState()

    var showChangeImageDialog by remember { mutableStateOf(value = false) }
    var showChangeNameDialog by remember { mutableStateOf(value = false) }
    var showBlockDialog by remember { mutableStateOf(value = false) }
    var showUnblockDialog by remember { mutableStateOf(value = false) }
    var userToBlock by remember { mutableStateOf<UserProfile?>(null) }
    var userToUnblock by remember { mutableStateOf<UserProfile?>(null) }
    var showBlockOptions by remember { mutableStateOf(false) }
    var showLeaveConfirmDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val isSendingImage by nodesViewModel.isSendingImage.collectAsStateWithLifecycle()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null && node?.id != null) {
                nodesViewModel.uploadNodePictureFromUri(
                    nodeId = node!!.id,
                    uri = uri,
                    context = context
                )
            }
            showChangeImageDialog = false
        }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success: Boolean ->
            if (success && node?.id != null) {
                tempCameraUri?.let { uri ->
                    nodesViewModel.uploadNodePictureFromUri(
                        nodeId = node!!.id,
                        uri = uri,
                        context = context
                    )
                }
            }
            showChangeImageDialog = false
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                val uri = ImageUtils.createTempImageUri(context)
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            } else {
                showChangeImageDialog = false
                ToastHelper.showToast(
                    context,
                    context.getString(R.string.workspace_detail_camera_permission_denied)
                )
            }
        }
    )

    val messageNode by nodesViewModel.userInfo.collectAsState()
    LaunchedEffect(key1 = messageNode) {
        if (messageNode != null) {
            ToastHelper.showToast(context, messageNode!!)
            nodesViewModel.clearUserInfo()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .padding(outerPadding), // <--- IMPORTANTE: Desplaza el contenido sobre la BottomBar
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.workspace_detail_info_title)) },
                navigationIcon = {
                    IconButton(onClick = { contentNavController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.workspace_detail_back_to_chat_description)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showLeaveConfirmDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = stringResource(R.string.leave_node),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .entranceAnimation(),
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ... items logic ...

            item {
                NodeProfileImage(
                    imageUrl = node?.workspacePicture,
                    modifier = Modifier
                        .size(120.dp)
                        .clickable {
                            showChangeImageDialog = true
                        })
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = node?.name ?: "",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = { showChangeNameDialog = true },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.workspace_detail_change_name_description)
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.workspace_detail_invite_code_label),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SelectionContainer {
                            Text(
                                text = node?.inviteCode
                                    ?: stringResource(R.string.common_not_available),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = formatDate(node?.createdAt, context),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Text(
                    text = stringResource(R.string.workspace_detail_members_label, members.size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            items(members) { userProfile ->
                val isMe = userProfile.id == currentUserProfile?.id
                val isTargetBlocked = currentUserProfile?.blockedUsers?.contains(userProfile.id) == true
                val isTargetAdmin = node?.admins?.contains(userProfile.id) == true || node?.ownerId == userProfile.id
                
                UserProfileItem(
                    user = userProfile,
                    isAdmin = isTargetAdmin,
                    onLongClick = {
                        if (!isMe) {
                            if (isTargetBlocked) {
                                userToUnblock = userProfile
                            } else {
                                userToBlock = userProfile
                            }
                            showBlockOptions = true
                        }
                    }
                )
                HorizontalDivider(Modifier.padding(horizontal = 8.dp))
            }
        }
    }

    if (showBlockOptions) {
        val targetUser = userToBlock ?: userToUnblock
        val isBlocked = targetUser?.id?.let { currentUserProfile?.blockedUsers?.contains(it) } == true
        val amIAdmin = node?.admins?.contains(currentUserProfile?.id) == true || node?.ownerId == currentUserProfile?.id
        val isTargetAdmin = targetUser?.id?.let { uid -> node?.admins?.contains(uid) == true || node?.ownerId == uid } == true

        AlertDialog(
            onDismissRequest = { 
                showBlockOptions = false 
                userToBlock = null
                userToUnblock = null
            },
            title = { Text(stringResource(R.string.chat_entry_options_title)) },
            text = {
                Column(Modifier.fillMaxWidth()) {
                    // Opción de Bloqueo/Desbloqueo (Existente)
                    Text(
                        text = stringResource(if (isBlocked) R.string.unblock_user else R.string.block_user),
                        color = if (isBlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showBlockOptions = false
                                if (isBlocked) showUnblockDialog = true else showBlockDialog = true
                            }
                            .padding(vertical = 12.dp)
                    )

                    // Opción de Rol Admin (Nueva)
                    if (amIAdmin) {
                        val isTargetOwner = targetUser?.id == node?.ownerId
                        
                        if (!isTargetOwner) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Text(
                                text = stringResource(if (isTargetAdmin) R.string.role_remove_admin else R.string.role_make_admin),
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val nodeId = node?.id
                                        if (nodeId != null) {
                                            targetUser?.id?.let { targetUid ->
                                                nodesViewModel.toggleAdminRole(
                                                    nodeId = nodeId,
                                                    targetUid = targetUid,
                                                    makeAdmin = !isTargetAdmin
                                                )
                                            }
                                        }
                                        showBlockOptions = false
                                        userToBlock = null
                                        userToUnblock = null
                                    }
                                    .padding(vertical = 12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    showBlockOptions = false
                    userToBlock = null
                    userToUnblock = null
                }) {
                    Text(stringResource(R.string.common_close))
                }
            }
        )
    }

    if (showUnblockDialog) {
        AlertDialog(
            onDismissRequest = { 
                showUnblockDialog = false 
                userToUnblock = null
            },
            title = { Text(stringResource(R.string.unblock_user_dialog_title)) },
            text = { Text(stringResource(R.string.unblock_user_dialog_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        userToUnblock?.id?.let { targetUid ->
                            authViewModel.unblockUser(
                                targetUid = targetUid,
                                onSuccess = {
                                    ToastHelper.showToast(context, context.getString(R.string.unblock_user_success))
                                },
                                onError = { error ->
                                    ToastHelper.showToast(context, error)
                                }
                            )
                        }
                        showUnblockDialog = false
                        userToUnblock = null
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showUnblockDialog = false 
                    userToUnblock = null
                }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    if (showBlockDialog) {
        AlertDialog(
            onDismissRequest = { 
                showBlockDialog = false 
                userToBlock = null
            },
            title = { Text(stringResource(R.string.block_user_dialog_title)) },
            text = { Text(stringResource(R.string.block_user_dialog_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        userToBlock?.id?.let { targetUid ->
                            authViewModel.blockUser(
                                targetUid = targetUid,
                                onSuccess = {
                                    ToastHelper.showToast(context, context.getString(R.string.block_user_success))
                                },
                                onError = { error ->
                                    ToastHelper.showToast(context, error)
                                }
                            )
                        }
                        showBlockDialog = false
                        userToBlock = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showBlockDialog = false 
                    userToBlock = null
                }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    if (showChangeNameDialog) {
        MyTextDialog(
            startValue = node?.name ?: "",
            onConfirm = { inputName: String ->
                node?.id?.let {
                    nodesViewModel.updateNodeName(it, inputName)
                }
                showChangeNameDialog = false
            },
            onDismiss = {
                showChangeNameDialog = false
            },
            title = stringResource(R.string.dialog_change_name_title),
            label = stringResource(R.string.common_name)
        )
    }

    if (showChangeImageDialog) {
        AlertDialog(
            onDismissRequest = { showChangeImageDialog = false },
            title = { Text(stringResource(R.string.workspace_detail_change_picture_title)) },
            text = { Text(stringResource(R.string.workspace_detail_change_picture_prompt)) },
            confirmButton = {
                TextButton(onClick = {
                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Text(stringResource(R.string.common_gallery))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    val permission = android.Manifest.permission.CAMERA
                    if (ContextCompat.checkSelfPermission(
                            context, permission
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val uri = ImageUtils.createTempImageUri(context)
                        tempCameraUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        cameraPermissionLauncher.launch(permission)
                    }
                }) {
                    Text(stringResource(R.string.common_camera))
                }
            })
    }

    if (showLeaveConfirmDialog) {
        val amIAdmin = node?.admins?.contains(currentUserProfile?.id) == true || node?.ownerId == currentUserProfile?.id
        
        AlertDialog(
            onDismissRequest = { showLeaveConfirmDialog = false },
            title = { Text(stringResource(R.string.leave_node)) },
            text = { 
                Text(
                    stringResource(if (amIAdmin) R.string.leave_node_warning_admin else R.string.leave_node_warning_user)
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        node?.let {
                            nodesViewModel.leaveNode(it) {
                                showLeaveConfirmDialog = false
                                contentNavController.navigate("workspaces") {
                                    popUpTo("home") { inclusive = false }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveConfirmDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
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

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun UserProfileItem(user: UserProfile, isAdmin: Boolean, onLongClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            )
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        ProfileImage(
            imageUrl = user.profilePicture, modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${user.firstName} ${user.lastName}",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // Badge de Rol
        Surface(
            color = if (isAdmin) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.extraSmall,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text(
                text = stringResource(if (isAdmin) R.string.role_admin else R.string.role_user),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                color = if (isAdmin) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ProfileImage(imageUrl: String?, modifier: Modifier = Modifier) {
    if (!imageUrl.isNullOrBlank()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = stringResource(R.string.common_profile_picture_description),
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(CircleShape)
        )
    } else {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = stringResource(R.string.common_user_no_image_description),
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun NodeProfileImage(
    imageUrl: String?, modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.clip(CircleShape)
    ) {
        if (!imageUrl.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = stringResource(R.string.workspace_detail_picture_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    ShimmerImageLoader(modifier = Modifier.fillMaxSize())
                }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Apartment,
                    contentDescription = stringResource(R.string.workspace_detail_no_image_description),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(50.dp)
                )
            }
        }

        // Overlay difuminado con icono de lapiz por encima
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.5f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.workspace_detail_change_picture_title),
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTextDialog(
    startValue: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    title: String,
    label: String,
    maxLength: Int = 30
) {
    var tempText by remember(startValue) { mutableStateOf(startValue) }

        AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = {
            CharacterLimitedOutlinedTextField(
                value = tempText,
                onValueChange = { tempText = it },
                maxLength = maxLength,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(label) },
                singleLine = true,
            )
        }, confirmButton = {
        Button(
            onClick = { onConfirm(tempText) }, enabled = tempText.isNotBlank()
        ) {
            Text(stringResource(R.string.common_save))
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text(stringResource(R.string.common_cancel))
        }
    })
}

private fun formatDate(date: Date?, context: Context): String {
    if (date == null) return context.getString(R.string.common_date_not_available)
    val formatPattern = context.getString(R.string.common_date_format_long)
    val formatter = SimpleDateFormat(formatPattern, Locale.getDefault())
    return context.getString(R.string.common_created_on, formatter.format(date))
}
