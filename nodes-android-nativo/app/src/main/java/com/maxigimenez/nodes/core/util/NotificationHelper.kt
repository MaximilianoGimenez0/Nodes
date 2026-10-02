package com.maxigimenez.nodes.core.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    const val MESSAGES_CHANNEL_ID = "nodes_messages"
    const val REMINDERS_CHANNEL_ID = "nodes_reminders"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val messagesChannel = NotificationChannel(
                MESSAGES_CHANNEL_ID,
                "Mensajes de Nodos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de nuevos mensajes y actualizaciones en tus Nodos"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }

            val remindersChannel = NotificationChannel(
                REMINDERS_CHANNEL_ID,
                "Recordatorios",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas de recordatorios programados en tus notas"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(messagesChannel)
            notificationManager.createNotificationChannel(remindersChannel)
        }
    }

    /**
     * Verifica si las notificaciones generales del sistema están habilitadas.
     */
    fun isGlobalNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /**
     * Verifica si las notificaciones Push (Canal de Mensajes de Nodos) están activas.
     * Si las notificaciones globales del sistema están apagadas, devuelve false.
     */
    fun isPushNotificationsEnabled(context: Context): Boolean {
        if (!isGlobalNotificationsEnabled(context)) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = notificationManager.getNotificationChannel(MESSAGES_CHANNEL_ID)
            if (channel != null && channel.importance == NotificationManager.IMPORTANCE_NONE) {
                return false
            }
        }
        return true
    }

    /**
     * Verifica si los Recordatorios (Canal de Recordatorios) están activos.
     * Si las notificaciones globales del sistema están apagadas, devuelve false.
     */
    fun isRemindersNotificationsEnabled(context: Context): Boolean {
        if (!isGlobalNotificationsEnabled(context)) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = notificationManager.getNotificationChannel(REMINDERS_CHANNEL_ID)
            if (channel != null && channel.importance == NotificationManager.IMPORTANCE_NONE) {
                return false
            }
        }
        return true
    }

    fun areNotificationsFullyEnabled(context: Context): Boolean {
        return isPushNotificationsEnabled(context) && isRemindersNotificationsEnabled(context)
    }
}
