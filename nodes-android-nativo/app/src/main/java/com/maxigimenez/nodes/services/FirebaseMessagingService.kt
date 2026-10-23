package com.maxigimenez.nodes.services

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.maxigimenez.nodes.app.MainActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.maxigimenez.nodes.R
import com.maxigimenez.nodes.core.util.NotificationHelper
import com.maxigimenez.nodes.app.NodesApplication
import com.maxigimenez.nodes.data.local.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class FirebaseMessagingService : FirebaseMessagingService() {

    private val settingsManager: SettingsManager by lazy {
        (application as NodesApplication).settingsManager
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.data.let { data ->
            val title = data["title"]
            val body = data["body"]
            val workspaceId = data["workspaceId"]

            if (title != null && body != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    val currentSettings = settingsManager.appSettingsFlow.first()

                    if (!NodesApplication.isAppInForeground && currentSettings.allowNotifications) {
                        sendNotification(title, body, workspaceId)
                    }
                }
            }
        }
    }

    override fun onNewToken(token: String) {
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            sendTokenToFirestore(token, currentUser.uid)
        }
    }

    private fun sendNotification(title: String, messageBody: String, workspaceId: String?) {
        val channelId = NotificationHelper.MESSAGES_CHANNEL_ID

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("EXTRA_WORKSPACE_ID", workspaceId)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        // Evitar sobreescritura de PendingIntents entre distintos nodos mediante requestCode único
        val requestCode = workspaceId?.hashCode() ?: 0

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 
            requestCode,
            intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification_nodes)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setShowWhen(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@FirebaseMessagingService, android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Reemplazar notificaciones previas del mismo nodo utilizando su identificador hash
                val notificationId = if (!workspaceId.isNullOrBlank()) workspaceId.hashCode() else System.currentTimeMillis().toInt()
                notify(notificationId, builder.build())
            }
        }
    }

    private fun sendTokenToFirestore(token: String, userId: String) {
        if (token.isBlank() || userId.isBlank()) return

        val db = Firebase.firestore

        val tokenData = hashMapOf(
            "token" to token, "lastUsed" to System.currentTimeMillis()
        )

        db.collection("users").document(userId).collection("tokens").document(token).set(tokenData)
            .addOnSuccessListener {
                Log.d("FCM", "Token guardado en Firestore exitosamente.")
            }.addOnFailureListener { e ->
                Log.e("FCM", "Error al guardar token", e)
            }
    }
}
