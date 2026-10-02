package com.maxigimenez.nodes.features.nodes.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import com.maxigimenez.nodes.core.ui.theme.pressClickEffect
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.maxigimenez.nodes.R

@Composable
private fun FabMenuSubItem(
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    contentDescription: String
) {
    ExtendedFloatingActionButton(
        modifier = Modifier
            .width(160.dp)
            .pressClickEffect(),
        onClick = onClick,
        icon = { Icon(icon, contentDescription, modifier = Modifier.size(24.dp)) },
        text = { Text(label, style = MaterialTheme.typography.labelLarge) },
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    )
}

@Composable
fun NodesFabMenu(
    isFabMenuExpanded: Boolean,
    onFabClicked: () -> Unit,
    onCreateClicked: () -> Unit,
    onJoinClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFabMenuExpanded) 45f else 0f,
        label = "fabRotation"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedVisibility(
            visible = isFabMenuExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FabMenuSubItem(
                    onClick = onJoinClicked,
                    icon = Icons.Default.GroupAdd,
                    label = stringResource(R.string.workspaces_fab_join_text),
                    contentDescription = stringResource(R.string.workspaces_fab_join_description)
                )

                FabMenuSubItem(
                    onClick = onCreateClicked,
                    icon = Icons.Default.AddCircle,
                    label = stringResource(R.string.workspaces_fab_create_text),
                    contentDescription = stringResource(R.string.workspaces_fab_create_description)
                )
            }
        }

        FloatingActionButton(
            onClick = onFabClicked,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .rotate(rotation)
                .pressClickEffect()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.workspaces_fab_menu_description),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun NodeEntriesControls(
    isFabMenuExpanded: Boolean,
    onFabClicked: () -> Unit,
    modifier: Modifier = Modifier,
    onMessage: () -> Unit,
    onCheckList: () -> Unit,
    onImage: () -> Unit,
    onReminder: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFabMenuExpanded) 45f else 0f,
        label = "fabRotation"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        AnimatedVisibility(
            visible = isFabMenuExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FabMenuSubItem(
                    onClick = onMessage,
                    icon = Icons.AutoMirrored.Filled.Message,
                    label = stringResource(R.string.chat_fab_message),
                    contentDescription = stringResource(R.string.chat_fab_message)
                )

                FabMenuSubItem(
                    onClick = onCheckList,
                    icon = Icons.Default.Checklist,
                    label = stringResource(R.string.chat_fab_checklist),
                    contentDescription = stringResource(R.string.chat_fab_checklist)
                )

                FabMenuSubItem(
                    onClick = onImage,
                    icon = Icons.Default.Image,
                    label = stringResource(R.string.chat_fab_image),
                    contentDescription = stringResource(R.string.chat_fab_image)
                )

                FabMenuSubItem(
                    onClick = onReminder,
                    icon = Icons.Default.NotificationsActive,
                    label = stringResource(R.string.chat_fab_reminder),
                    contentDescription = stringResource(R.string.chat_fab_reminder)
                )
            }
        }

        FloatingActionButton(
            onClick = onFabClicked,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .rotate(rotation)
                .pressClickEffect()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.workspaces_fab_menu_description),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
