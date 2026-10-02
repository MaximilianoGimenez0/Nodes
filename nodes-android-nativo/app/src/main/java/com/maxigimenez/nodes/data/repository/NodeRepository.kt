package com.maxigimenez.nodes.data.repository

import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NodeRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {

    private val db = Firebase.firestore
    private val nodesCollection = db.collection("workspaces")

    suspend fun actualizarNombreEnFirebase(id: String, nuevoNombre: String) {
        nodesCollection.document(id).update("name", nuevoNombre).await()
    }

    suspend fun updateNodePicture(id: String, profilePicture: String) {
        nodesCollection.document(id).update("workspacePicture", profilePicture).await()
    }
}
