package com.maxigimenez.nodes.features.nodes.components

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import android.text.format.DateFormat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.maxigimenez.nodes.core.ui.components.ShimmerImageLoader
import com.maxigimenez.nodes.R
import com.maxigimenez.nodes.data.model.ChecklistItem
import com.maxigimenez.nodes.data.model.NodeEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileAvatar(userName: String, profilePicture: String?) {
    val initials = userName.take(1).uppercase()

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        if (profilePicture.isNullOrBlank()) {
            Text(
                text = initials,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        } else {
            SubcomposeAsyncImage(
                model = profilePicture,
                contentDescription = stringResource(R.string.common_profile_picture_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            ) {
                val state = painter.state

                if (state is AsyncImagePainter.State.Success) {
                    SubcomposeAsyncImageContent()
                } else {
                    Text(
                        text = initials,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ImageBubble(
    entry: NodeEntry,
    userName: String,
    isFromCurrentUser: Boolean,
    profilePicture: String,
    onImageClick: (String) -> Unit
) {
    val imageUrl = entry.value ?: ""
    val date = entry.createdAt

    val timeFormat = stringResource(R.string.chat_timestamp_format)
    val timeFormatter = remember(timeFormat) { SimpleDateFormat(timeFormat, Locale.getDefault()) }
    val timeString = date?.let { timeFormatter.format(it) }
        ?: stringResource(R.string.chat_timestamp_placeholder)

    val bubbleShape = if (isFromCurrentUser) {
        RoundedCornerShape(
            topStart = 16.dp, topEnd = 0.dp, bottomEnd = 16.dp, bottomStart = 16.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        contentAlignment = if (isFromCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.Top) {

            if (!isFromCurrentUser) {
                ProfileAvatar(userName, profilePicture)
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
            ) {
                Text(
                    text = if (isFromCurrentUser) stringResource(R.string.chat_user_me) else userName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 2.dp, start = 4.dp, end = 4.dp)
                )

                val screenWidth = LocalConfiguration.current.screenWidthDp.dp

                Box(
                    modifier = Modifier
                        .widthIn(max = screenWidth * 0.75f)
                        .clip(bubbleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onImageClick(imageUrl) }
                ) {
                    if (imageUrl.isNotBlank()) {
                        SubcomposeAsyncImage(
                            model = imageUrl,
                            contentDescription = stringResource(R.string.chat_image_description),
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.fillMaxWidth(),
                            loading = {
                                ShimmerImageLoader()
                            },
                            error = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.chat_image_load_error),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )

                        if (entry.category != null && entry.category.isNotBlank()) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(bottomEnd = 8.dp),
                                modifier = Modifier.align(Alignment.TopStart)
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Label,
                                    contentDescription = stringResource(R.string.chat_category_label),
                                    modifier = Modifier.size(14.dp),
                                    tint = Color.White
                                )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = entry.category,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Surface(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(topStart = 8.dp),
                            modifier = Modifier.align(Alignment.BottomEnd)
                        ) {
                            Text(
                                text = timeString,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                    } else {
                        Box(
                            modifier = Modifier
                                .width(screenWidth * 0.5f)
                                .height(100.dp)
                                .padding(16.dp), contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.chat_image_load_error),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            if (isFromCurrentUser) {
                Spacer(modifier = Modifier.width(8.dp))
                ProfileAvatar(userName, profilePicture)
            }
        }
    }
}


@Composable
fun ChecklistBubble(
    entry: NodeEntry,
    userName: String,
    isFromCurrentUser: Boolean,
    profilePicture: String,
    onItemCheckedChange: (Int, Boolean) -> Unit
) {
    val title = entry.value ?: ""
    val items = entry.items
    val date = entry.createdAt

    val timeFormat = stringResource(R.string.chat_timestamp_format)
    val timeFormatter = remember(timeFormat) { SimpleDateFormat(timeFormat, Locale.getDefault()) }
    val timeString = date?.let { timeFormatter.format(it) }
        ?: stringResource(R.string.chat_timestamp_placeholder)

    val bubbleColor = if (isFromCurrentUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val bubbleShape = if (isFromCurrentUser) {
        RoundedCornerShape(
            topStart = 16.dp, topEnd = 0.dp, bottomEnd = 16.dp, bottomStart = 16.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        contentAlignment = if (isFromCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.Top) {

            if (!isFromCurrentUser) {
                ProfileAvatar(userName, profilePicture)
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
            ) {
                Text(
                    text = if (isFromCurrentUser) stringResource(R.string.chat_user_me) else userName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 2.dp, start = 4.dp, end = 4.dp)
                )

                val screenWidth = LocalConfiguration.current.screenWidthDp.dp

                Box(
                    modifier = Modifier
                        .widthIn(max = screenWidth * 0.75f)
                        .clip(bubbleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Surface(
                        color = bubbleColor, shape = bubbleShape, shadowElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .widthIn(max = screenWidth * 0.75f)
                        ) {
                            CategoryDisplay(category = entry.category)

                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            items?.forEachIndexed { index, item ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Checkbox(
                                        checked = item.isChecked,
                                        enabled = true,
                                        onCheckedChange = { newCheckedState ->
                                            onItemCheckedChange(index, newCheckedState)
                                        },
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textDecoration = if (item.isChecked) {
                                            TextDecoration.LineThrough
                                        } else {
                                            null
                                        },
                                        color = if (item.isChecked) {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = timeString,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }

            if (isFromCurrentUser) {
                Spacer(modifier = Modifier.width(8.dp))
                ProfileAvatar(userName, profilePicture)
            }
        }
    }
}

@Composable
fun MessageBubble(
    entry: NodeEntry, userName: String, isFromCurrentUser: Boolean, profilePicture: String
) {
    val text = entry.value ?: ""
    val date = entry.createdAt

    val timeFormat = stringResource(R.string.chat_timestamp_format)
    val timeFormatter = remember(timeFormat) { SimpleDateFormat(timeFormat, Locale.getDefault()) }
    val timeString = date?.let { timeFormatter.format(it) }
        ?: stringResource(R.string.chat_timestamp_placeholder)

    val bubbleColor = if (isFromCurrentUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val bubbleShape = if (isFromCurrentUser) {
        RoundedCornerShape(
            topStart = 16.dp, topEnd = 0.dp, bottomEnd = 16.dp, bottomStart = 16.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        contentAlignment = if (isFromCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.Top) {

            if (!isFromCurrentUser) {
                ProfileAvatar(userName, profilePicture)
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                modifier = Modifier.weight(1f, fill = false),
                horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
            ) {
                Text(
                    text = if (isFromCurrentUser) stringResource(R.string.chat_user_me) else userName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 2.dp, start = 4.dp, end = 4.dp)
                )

                val screenWidth = LocalConfiguration.current.screenWidthDp.dp
                Surface(
                    color = bubbleColor, shape = bubbleShape, shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .widthIn(max = screenWidth * 0.75f)
                    ) {
                        CategoryDisplay(category = entry.category)

                        Text(
                            text = text, style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = timeString,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }

            if (isFromCurrentUser) {
                Spacer(modifier = Modifier.width(8.dp))
                ProfileAvatar(userName, profilePicture)
            }
        }
    }
}

@Composable
fun ReminderBubble(
    entry: NodeEntry,
    userName: String,
    isFromCurrentUser: Boolean,
    profilePicture: String,
    onItemStateChange: (index: Int, isChecked: Boolean) -> Unit
) {
    val itemsList = entry.items
    val reminderItem = itemsList?.firstOrNull()
    val title = entry.value ?: stringResource(R.string.chat_reminder_default_title)
    val timestamp = reminderItem?.text?.toLongOrNull() ?: 0L
    val isChecked = reminderItem?.isChecked ?: false
    val date = entry.createdAt

    val invalidTime = stringResource(R.string.chat_reminder_invalid_time)
    val context = LocalContext.current

    val formattedAlarmTime = remember(timestamp, invalidTime, context) {
        if (timestamp == 0L) invalidTime
        else {
            val dateStr = DateFormat.getMediumDateFormat(context).format(Date(timestamp))
            val timeStr = DateFormat.getTimeFormat(context).format(Date(timestamp))
            "$dateStr, $timeStr"
        }
    }

    val timeString = formatTimestamp(date)

    val hasAlarmPassed = remember(timestamp, isChecked) {
        (timestamp != 0L) && (timestamp < System.currentTimeMillis())
    }

    val bubbleColor = if (isFromCurrentUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val bubbleShape = if (isFromCurrentUser) {
        RoundedCornerShape(
            topStart = 16.dp, topEnd = 0.dp, bottomEnd = 16.dp, bottomStart = 16.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        contentAlignment = if (isFromCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.Top) {

            if (!isFromCurrentUser) {
                ProfileAvatar(userName, profilePicture)
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                modifier = Modifier.weight(1f, fill = false),
                horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
            ) {
                Text(
                    text = if (isFromCurrentUser) stringResource(R.string.chat_user_me) else userName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 2.dp, start = 4.dp, end = 4.dp)
                )

                val screenWidth = LocalConfiguration.current.screenWidthDp.dp

                Surface(
                    color = bubbleColor,
                    shape = bubbleShape,
                    shadowElevation = 1.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .widthIn(max = screenWidth * 0.75f)
                    ) {
                        CategoryDisplay(category = entry.category)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium,
                                    textDecoration = if (hasAlarmPassed) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (hasAlarmPassed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Alarm,
                                        contentDescription = stringResource(R.string.chat_reminder_alarm_icon_description),
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = formattedAlarmTime,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (hasAlarmPassed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Switch(
                                checked = isChecked, onCheckedChange = { newState ->
                                    onItemStateChange(0, newState)
                                }, enabled = !hasAlarmPassed
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = timeString,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.align(Alignment.End),
                            color = if (isFromCurrentUser) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (isFromCurrentUser) {
                Spacer(modifier = Modifier.width(8.dp))
                ProfileAvatar(userName, profilePicture)
            }
        }
    }
}

@Composable
fun CategoryDisplay(category: String?) {
    if (!category.isNullOrBlank()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Label,
                contentDescription = stringResource(R.string.chat_category_label),
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = category,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun formatTimestamp(date: java.util.Date?): String {
    if (date == null) return ""
    return try {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(date)
    } catch (e: Exception) {
        Log.e("formatTimestamp", "Error al formatear fecha", e)
        ""
    }
}

internal fun saveBitmapToStorage(context: Context, bitmap: Bitmap): Boolean {
    val displayName = "IMG_${System.currentTimeMillis()}.jpg"
    val mimeType = "image/jpeg"

    val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
        put(MediaStore.Images.Media.MIME_TYPE, mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Download/Nodes")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }

    var outputStream: OutputStream? = null
    var uri: android.net.Uri? = null
    val contentResolver = context.contentResolver

    try {
        uri = contentResolver.insert(collectionUri, contentValues)
        if (uri == null) {
            Log.e("SaveImage", "Error al crear MediaStore entry")
            return false
        }

        outputStream = contentResolver.openOutputStream(uri)
        if (outputStream == null) {
            Log.e("SaveImage", "Error al obtener output stream")
            return false
        }

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            contentResolver.update(uri, contentValues, null, null)
        }

        Log.d("SaveImage", "Imagen guardada con éxito en $uri")
        return true

    } catch (e: Exception) {
        Log.e("SaveImage", "Error al guardar imagen", e)
        uri?.let { contentResolver.delete(it, null, null) }
        return false
    } finally {
        outputStream?.close()
    }
}
