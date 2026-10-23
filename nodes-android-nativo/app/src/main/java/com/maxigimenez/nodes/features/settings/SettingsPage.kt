package com.maxigimenez.nodes.features.settings


import android.Manifest
import android.os.Build
import androidx.core.content.ContextCompat
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.maxigimenez.nodes.core.ui.theme.entranceAnimation
import com.maxigimenez.nodes.core.ui.components.CharacterLimitedOutlinedTextField
import com.maxigimenez.nodes.core.ui.components.ShimmerImageLoader
import com.maxigimenez.nodes.core.util.ToastHelper
import com.maxigimenez.nodes.R
import com.maxigimenez.nodes.features.auth.AuthState
import com.maxigimenez.nodes.features.auth.AuthViewModel
import com.maxigimenez.nodes.features.auth.UserViewModel
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(
    modifier: Modifier,
    outerPadding: PaddingValues,
    authViewmodel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    navController: NavController,
    userViewModel: UserViewModel,
    contentController: NavController
) {
    val TAG = "SettingsPage"
    val context = LocalContext.current

    val settings by settingsViewModel.appSettings.collectAsState()
    val authState = authViewmodel.authState.observeAsState()
    val userId by authViewmodel.userId.observeAsState()
    val userProfile by userViewModel.userProfile.collectAsState()
    val mensajeUsuario by userViewModel.userInfo.collectAsState()
    val isUploadingImage by userViewModel.isUploadingImage.collectAsState()

    var FirstName by remember(userProfile?.firstName) {
        mutableStateOf(userProfile?.firstName ?: "")
    }
    var LastName by remember(userProfile?.lastName) {
        mutableStateOf(userProfile?.lastName ?: "")
    }
    val Email = userProfile?.email ?: ""

    val isDirty =
        (FirstName != (userProfile?.firstName ?: "")) || (LastName != (userProfile?.lastName ?: ""))

    var isLanguageMenuExpanded by remember { mutableStateOf(false) }
    var isThemeMenuExpanded by remember { mutableStateOf(false) }
    var showChangeImageDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(userProfile) {
        if (userProfile != null) {
            Log.d(TAG, "Datos del perfil actualizados: $userProfile")
        } else {
            Log.d(TAG, "Datos del perfil son nulos.")
        }
    }

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.UnAuthenticated -> navController.navigate("login")
            else -> {
                userId?.let { userViewModel.loadUserProfile(it) }
            }
        }
    }

    LaunchedEffect(key1 = mensajeUsuario) {
        if (mensajeUsuario != null) {
            ToastHelper.showToast(context, mensajeUsuario!!)
            userViewModel.clearUserInfo()
        }
    }


    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            if (uri != null && userId != null) {
                userViewModel.uploadProfilePictureFromUri(userId!!, uri, context)
            }
            showChangeImageDialog = false
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success: Boolean ->
            if (success && userId != null) {
                tempCameraUri?.let { uri ->
                    userViewModel.uploadProfilePictureFromUri(userId!!, uri, context)
                }
            }
            showChangeImageDialog = false
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(), onResult = { isGranted: Boolean ->
            if (isGranted) {
                val uri = com.maxigimenez.nodes.core.util.ImageUtils.createTempImageUri(context)
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            } else {
                ToastHelper.showToast(
                    context,
                    context.getString(R.string.permission_camera_denied)
                )
                showChangeImageDialog = false
            }
        })


    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(outerPadding),
        topBar = {
            TopAppBar(
                title = {
                Text(
                    stringResource(R.string.settings),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }, navigationIcon = {
                IconButton(onClick = { contentController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
            )
        }) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .entranceAnimation(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            ProfilePicture(
                userProfile?.profilePicture,
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp),
                onImageChange = { showChangeImageDialog = true })

            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(stringResource(R.string.profile_section))
            SectionDescription(stringResource(R.string.profile_description))
            Spacer(modifier = Modifier.height(16.dp))

            CharacterLimitedOutlinedTextField(
                value = FirstName,
                onValueChange = { FirstName = it },
                maxLength = 40,
                label = { Text(stringResource(R.string.name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            CharacterLimitedOutlinedTextField(
                value = LastName,
                onValueChange = { LastName = it },
                maxLength = 40,
                label = { Text(stringResource(R.string.name_lastname)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = Email,
                onValueChange = {},
                label = { Text(stringResource(R.string.name_email)) },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (isDirty) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End)
                ) {
                    OutlinedButton(
                        onClick = {
                            FirstName = userProfile?.firstName ?: ""
                            LastName = userProfile?.lastName ?: ""
                        }) {
                        Text(stringResource(R.string.cancel))
                    }
                    Button(
                        onClick = {
                            userId?.let {
                                userViewModel.updateUserInfo(
                                    userProfile?.id ?: "",
                                    firstName = FirstName,
                                    lastName = LastName,
                                )
                                Log.d(TAG, "Confirmar cambios: $FirstName, $LastName")
                            }
                        }) {
                        Text(stringResource(R.string.confirm))
                    }
                }
            }


            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(stringResource(R.string.application_section))
            SectionDescription(stringResource(R.string.application_description))
            Spacer(modifier = Modifier.height(16.dp))

            val languages = listOf("Español", "Inglés", "Portugués")
            ExposedDropdownMenuBox(
                expanded = isLanguageMenuExpanded,
                onExpandedChange = { isLanguageMenuExpanded = !isLanguageMenuExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = settings.language,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.language)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isLanguageMenuExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = isLanguageMenuExpanded,
                    onDismissRequest = { isLanguageMenuExpanded = false }) {
                    languages.forEach { language ->
                        DropdownMenuItem(text = { Text(language) }, onClick = {
                            isLanguageMenuExpanded = false
                            settingsViewModel.setLanguage(language)
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val themes = listOf(
                stringResource(R.string.theme_system),
                stringResource(R.string.theme_light),
                stringResource(R.string.theme_dark)
            )
            val themeToSave = mapOf(
                stringResource(R.string.theme_system) to "Automático (Sistema)",
                stringResource(R.string.theme_light) to "Claro",
                stringResource(R.string.theme_dark) to "Oscuro"
            )
            val themeToDisplay = mapOf(
                "Automático (Sistema)" to stringResource(R.string.theme_system),
                "Claro" to stringResource(R.string.theme_light),
                "Oscuro" to stringResource(R.string.theme_dark)
            )

            ExposedDropdownMenuBox(
                expanded = isThemeMenuExpanded,
                onExpandedChange = { isThemeMenuExpanded = !isThemeMenuExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = themeToDisplay[settings.theme] ?: settings.theme,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.theme)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isThemeMenuExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = isThemeMenuExpanded,
                    onDismissRequest = { isThemeMenuExpanded = false }) {
                    themes.forEach { themeName ->
                        DropdownMenuItem(text = { Text(themeName) }, onClick = {
                            isThemeMenuExpanded = false
                            settingsViewModel.setTheme(themeToSave[themeName] ?: themeName)
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            FontSizeSlider(
                currentSize = settings.fontSize, onSizeChange = { newSize ->
                    settingsViewModel.setFontSize(newSize)
                })

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(stringResource(R.string.notifications_section))
            SectionDescription(stringResource(R.string.notifications_description))
            Spacer(modifier = Modifier.height(16.dp))

            var isPushEnabled by remember {
                mutableStateOf(com.maxigimenez.nodes.core.util.NotificationHelper.isPushNotificationsEnabled(context))
            }

            var isRemindersEnabled by remember {
                mutableStateOf(com.maxigimenez.nodes.core.util.NotificationHelper.isRemindersNotificationsEnabled(context))
            }

            val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
            androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                        isPushEnabled = com.maxigimenez.nodes.core.util.NotificationHelper.isPushNotificationsEnabled(context)
                        isRemindersEnabled = com.maxigimenez.nodes.core.util.NotificationHelper.isRemindersNotificationsEnabled(context)
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            SettingSwitchItem(
                text = stringResource(R.string.allow_push_notifications),
                checked = isPushEnabled,
                onCheckedChange = { _ ->
                    openNotificationSettings(context)
                })

            Spacer(modifier = Modifier.height(16.dp))

            SettingSwitchItem(
                text = stringResource(R.string.allow_reminders_notifications),
                checked = isRemindersEnabled,
                onCheckedChange = { _ ->
                    openNotificationSettings(context)
                })

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                onClick = {
                    authViewmodel.signOut()
                    navController.navigate("login")
                }) {
                Text(stringResource(R.string.logout))
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                onClick = {
                    showDeleteDialog = true
                }) {
                Text(stringResource(R.string.delete_account))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_account_dialog_title)) },
            text = { Text(stringResource(R.string.delete_account_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        authViewmodel.deleteAccount(
                            onSuccess = {
                                showDeleteDialog = false
                                ToastHelper.showToast(context, context.getString(R.string.delete_account_success), Toast.LENGTH_LONG)
                                navController.navigate("login") {
                                    popUpTo(0)
                                }
                            },
                            onError = { error ->
                                showDeleteDialog = false
                                ToastHelper.showToast(context, error, Toast.LENGTH_LONG)
                            }
                        )
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showChangeImageDialog) {
        AlertDialog(
            onDismissRequest = { showChangeImageDialog = false },
            title = { Text(stringResource(R.string.profile_pic_change_title)) },
            text = { Text(stringResource(R.string.profile_pic_change_prompt)) },
            confirmButton = {
                TextButton(onClick = {
                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Text(stringResource(R.string.gallery))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    val permission = Manifest.permission.CAMERA
                    val permissionCheckResult =
                        ContextCompat.checkSelfPermission(context, permission)

                    if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                        val uri = com.maxigimenez.nodes.core.util.ImageUtils.createTempImageUri(context)
                        tempCameraUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        cameraPermissionLauncher.launch(permission)
                    }
                }) {
                    Text(stringResource(R.string.camera))
                }
            })
    }

    if (authState.value is AuthState.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = Color.White)
                Text(
                    text = stringResource(R.string.common_loading),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }

    if (isUploadingImage) {
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
private fun FontSizeSlider(
    currentSize: Int, onSizeChange: (Int) -> Unit
) {
    val labels = listOf(
        stringResource(R.string.font_small),
        stringResource(R.string.font_medium),
        stringResource(R.string.font_large)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.font_size),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = labels[currentSize],
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = currentSize.toFloat(), onValueChange = { newValue ->
                onSizeChange(newValue.roundToInt())
            }, valueRange = 0f..2f, steps = 1
        )
    }
}


@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SectionDescription(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SettingSwitchItem(
    text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .toggleable(
                value = checked, onValueChange = onCheckedChange, enabled = enabled
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
        Switch(
            checked = checked, onCheckedChange = null, enabled = enabled
        )
    }
}

private fun openNotificationSettings(context: Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    } else {
        Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
    context.startActivity(intent)
}

@Composable
private fun ProfilePicture(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    onImageChange: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onImageChange)
    ) {
        if (!imageUrl.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = stringResource(R.string.profile_pic_desc),
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
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(R.string.profile_pic_empty_desc),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(50.dp)
                )
            }
        }

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
                        contentDescription = stringResource(R.string.profile_pic_change_desc),
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
