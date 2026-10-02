package com.maxigimenez.nodes.features.nodes.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.maxigimenez.nodes.R
import com.maxigimenez.nodes.core.ui.components.CharacterLimitedOutlinedTextField
import com.maxigimenez.nodes.data.model.ChecklistItem
import android.text.format.DateFormat
import java.util.Calendar

@Composable
fun CreateNodeDialog(
    nodeName: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_workspace_dialog_title)) },
        text = {
            CharacterLimitedOutlinedTextField(
                value = nodeName,
                onValueChange = onNameChange,
                maxLength = 25,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.create_workspace_dialog_label)) },
                singleLine = true,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = nodeName.isNotBlank()
            ) {
                Text(stringResource(R.string.create_workspace_dialog_confirm_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
fun JoinNodeDialog(
    nodeCode: String,
    onCodeChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.join_workspace_dialog_title)) },
        text = {
            CharacterLimitedOutlinedTextField(
                value = nodeCode,
                onValueChange = onCodeChange,
                maxLength = 6,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.join_workspace_dialog_label)) },
                singleLine = true,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = nodeCode.isNotBlank()
            ) {
                Text(stringResource(R.string.join_workspace_dialog_confirm_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
fun CreateMessageDialog(
    messageValue: String,
    onMessageChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_message_dialog_title)) },
        text = {
            CharacterLimitedOutlinedTextField(
                value = messageValue,
                onValueChange = onMessageChanged,
                maxLength = 250,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.create_message_dialog_label)) },
                minLines = 3,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm, enabled = messageValue.isNotBlank()
            ) {
                Text(stringResource(R.string.common_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReminderDialog(
    onConfirm: (title: String, items: List<ChecklistItem>) -> Unit, onDismiss: () -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    
    val context = LocalContext.current
    val is24HourFormat = DateFormat.is24HourFormat(context)
    val timePickerState = rememberTimePickerState(is24Hour = is24HourFormat)
    var showTimePickerDialog by remember { mutableStateOf(false) }

    val formattedTime = remember(timePickerState.hour, timePickerState.minute, is24HourFormat) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
            set(Calendar.MINUTE, timePickerState.minute)
        }
        DateFormat.getTimeFormat(context).format(calendar.time)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.reminder_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.reminder_dialog_label)) },
                    singleLine = true
                )
                Button(
                    onClick = { showTimePickerDialog = true }, modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.reminder_dialog_select_time_button, formattedTime))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                        if (before(Calendar.getInstance())) {
                            add(Calendar.DATE, 1)
                        }
                    }
                    val reminderTimeInMillis = calendar.timeInMillis

                    val reminderItem = ChecklistItem(
                        text = reminderTimeInMillis.toString(),
                        isChecked = true
                    )

                    onConfirm(title, listOf(reminderItem))
                }, enabled = title.isNotBlank()
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        })

    if (showTimePickerDialog) {
        TimePickerDialog(
            onDismiss = { showTimePickerDialog = false },
            onConfirm = { showTimePickerDialog = false },
            timePickerState = timePickerState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit, onConfirm: () -> Unit, timePickerState: TimePickerState
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.time_picker_dialog_title)) },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
            ) {
                TimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.common_accept))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        })
}
