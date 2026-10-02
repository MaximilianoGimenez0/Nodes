package com.maxigimenez.nodes.data.model

import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

@Keep
data class Node(
    val id: String = "",
    val name: String = "",
    val inviteCode: String = "",
    @ServerTimestamp val createdAt: Date? = null,
    val members: List<String> = emptyList(),
    val admins: List<String> = emptyList(),
    val ownerId: String = "",
    val workspacePicture: String? = null
)

@Keep
data class NodesUiState(
    val workspaces: List<Node> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@Keep
enum class EntryType {
    MESSAGE, CHECK_LIST, IMAGE, REMINDER, NONE
}

@Keep
data class ChecklistItem(
    val text: String = "",
    @get:PropertyName("isChecked") @set:PropertyName("isChecked") var isChecked: Boolean = false
)

@Keep
data class NodeEntry(
    val id: String = "",
    @ServerTimestamp val createdAt: Date? = null,
    val value: String? = "",
    val userId: String = "",
    val name: String = "",
    val profilePicture: String = "",
    val items: List<ChecklistItem>? = emptyList(),
    val type: EntryType = EntryType.NONE,
    val category: String? = null,
)

@Keep
data class UserProfile(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val profilePicture: String? = null,
    val blockedUsers: List<String> = emptyList()
)

@Keep
data class AppSettings(
    val theme: String = "Sistema",
    val language: String = "Español",
    val allowNotifications: Boolean = true,
    val fontSize: Int = 1
)
