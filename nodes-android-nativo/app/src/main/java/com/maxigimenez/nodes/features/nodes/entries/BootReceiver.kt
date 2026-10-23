package com.maxigimenez.nodes.features.nodes.entries

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.maxigimenez.nodes.data.model.EntryType
import com.maxigimenez.nodes.data.model.NodeEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Compatibilidad con reinicio rápido de fabricantes OEM (ej. HTC, MIUI)
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            val userId = Firebase.auth.currentUser?.uid ?: return
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = Firebase.firestore
                    val workspaces = db.collection("workspaces")
                        .whereArrayContains("members", userId)
                        .get().await()

                    for (doc in workspaces.documents) {
                        val workspaceId = doc.id
                        val reminders = db.collection("workspaces").document(workspaceId)
                            .collection("entries")
                            .whereEqualTo("type", EntryType.REMINDER.name)
                            .get().await()

                        for (entryDoc in reminders.documents) {
                            val entry = entryDoc.toObject(NodeEntry::class.java)?.copy(id = entryDoc.id)
                            if (entry != null) {
                                AlarmScheduler.schedule(context, entry, workspaceId)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
