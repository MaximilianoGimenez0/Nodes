package com.maxigimenez.nodes.data.repository

import com.maxigimenez.nodes.data.model.UserProfile
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = Firebase.firestore
    private val usersCollection = db.collection("users")

    suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            val snapshot = usersCollection.document(uid).get().await()
            snapshot.toObject<UserProfile>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUserInfo(id: String, newName: String, newLastName: String) {
        usersCollection.document(id).update("firstName", newName).await()
        usersCollection.document(id).update("lastName", newLastName).await()
    }

    suspend fun updateUserPicture(id: String, profilePicture: String) {
        usersCollection.document(id).update("profilePicture", profilePicture).await()
    }
}
