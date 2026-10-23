package com.maxigimenez.nodes.features.nodes.entries

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.maxigimenez.nodes.R
import com.maxigimenez.nodes.data.model.ChecklistItem
import com.maxigimenez.nodes.app.NodesApplication
import com.maxigimenez.nodes.data.model.NodeEntry
import com.maxigimenez.nodes.core.util.NotificationHelper
import com.maxigimenez.nodes.app.MainActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val settingsManager = (context.applicationContext as NodesApplication).settingsManager
        val appSettings = runBlocking { settingsManager.appSettingsFlow.first() }
        
        if (!appSettings.allowNotifications) return

        val title = intent.getStringExtra("REMINDER_TITLE") ?: context.getString(R.string.reminder_default_title)
        val workspaceId = intent.getStringExtra("WORKSPACE_ID")
        val entryId = intent.getStringExtra("ENTRY_ID") ?: ""
        
        val notificationId = if (entryId.isNotBlank()) entryId.hashCode() else System.currentTimeMillis().toInt()

        sendNotification(context, title, workspaceId, notificationId)

        if (workspaceId != null && entryId.isNotBlank()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = Firebase.firestore
                    val entryRef = db.collection("workspaces").document(workspaceId)
                        .collection("entries").document(entryId)
                    val entrySnapshot = entryRef.get().await()
                    val entry = entrySnapshot.toObject(NodeEntry::class.java)
                    if (entry?.items != null) {
                        val newItems = entry.items.toMutableList()
                        if (newItems.isNotEmpty()) {
                            newItems[0] = newItems[0].copy(isChecked = false)
                            entryRef.update("items", newItems).await()
                            Log.d("ReminderReceiver", "Reminder state deactivated in Firestore")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ReminderReceiver", "Error deactivating reminder state", e)
                }
            }
        }
    }

    private fun sendNotification(context: Context, title: String, workspaceId: String?, notificationId: Int) {
        val channelId = NotificationHelper.REMINDERS_CHANNEL_ID
        
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("EXTRA_WORKSPACE_ID", workspaceId)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_nodes)
            .setContentTitle(title)
            .setContentText(context.getString(R.string.reminder_notification_text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setShowWhen(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notify(notificationId, builder.build())
            }
        }
    }
}

object AlarmScheduler {

    /** Verifica la concesión para programar alarmas exactas en Android 12+ (API 31). */
    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    /**
     * Redirige al usuario a los ajustes del sistema para conceder el permiso de alarmas exactas.
     */
    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val intent = Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                val fallbackIntent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
            }
        }
    }

    /** Programa una alarma exacta que se ejecuta incluso en modo Doze. */
    fun schedule(context: Context, entry: NodeEntry, workspaceId: String?): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val entryId = entry.id
        val title = entry.value ?: context.getString(R.string.reminder_default_title)

        val itemsList = entry.items as? List<ChecklistItem>
        if (itemsList.isNullOrEmpty()) return false

        val reminderItem = itemsList.first()
        val timeInMillis = reminderItem.text.toLongOrNull() ?: 0L
        val isChecked = reminderItem.isChecked

        if (!isChecked) {
            cancel(context, entryId)
            return true
        }

        if (timeInMillis == 0L || timeInMillis <= System.currentTimeMillis()) return false

        if (!canScheduleExactAlarms(context)) {
            Log.w("AlarmScheduler", "Permiso de alarma exacta denegado por el sistema.")
            return false
        }

        val pendingIntent = createPendingIntent(
            context, entryId, title, workspaceId, PendingIntent.FLAG_UPDATE_CURRENT
        ) ?: return false

        return try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
            Log.d("AlarmScheduler", "Alarma exacta programada para $timeInMillis ms")
            true
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "SecurityException al programar alarma exacta: ${e.message}")
            false
        }
    }

    fun cancel(context: Context, entryId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = createPendingIntent(
            context, entryId, "", null, PendingIntent.FLAG_NO_CREATE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("AlarmScheduler", "Alarma cancelada para entryId: $entryId")
        }
    }

    private fun createPendingIntent(
        context: Context, entryId: String, title: String, workspaceId: String?, flag: Int
    ): PendingIntent? {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("REMINDER_TITLE", title)
            putExtra("ENTRY_ID", entryId)
            putExtra("WORKSPACE_ID", workspaceId)
            data = Uri.parse("reminder://$entryId")
        }
        val requestCode = entryId.hashCode()
        return PendingIntent.getBroadcast(
            context, requestCode, intent, flag or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
