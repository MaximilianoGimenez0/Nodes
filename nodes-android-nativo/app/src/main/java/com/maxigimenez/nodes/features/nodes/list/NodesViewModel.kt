package com.maxigimenez.nodes.features.nodes.list

import com.maxigimenez.nodes.core.util.ImageUtils
import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maxigimenez.nodes.R
import com.maxigimenez.nodes.data.model.ChecklistItem
import com.maxigimenez.nodes.data.model.EntryType
import com.maxigimenez.nodes.data.model.UserProfile
import com.maxigimenez.nodes.data.model.Node
import com.maxigimenez.nodes.data.model.NodeEntry
import com.maxigimenez.nodes.data.model.NodesUiState
import com.maxigimenez.nodes.data.repository.NodeRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.TimeoutCancellationException
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.util.UUID

enum class NodeAction {
    NONE, CREATE, JOIN
}

@SuppressLint("StaticFieldLeak")
class NodesViewModel(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val nodeRepository: NodeRepository,
    application: Application,
) : AndroidViewModel(application) {

    private val _pendingAction = MutableStateFlow(NodeAction.NONE)
    val pendingAction: StateFlow<NodeAction> = _pendingAction.asStateFlow()

    fun setPendingAction(action: NodeAction) {
        _pendingAction.value = action
    }

    private val context = application.applicationContext

    private val _nodeMembers = MutableStateFlow<List<UserProfile>>(emptyList())
    val nodeMembers: StateFlow<List<UserProfile>> = _nodeMembers.asStateFlow()

    private val _userInfo = MutableStateFlow<String?>(null)
    val userInfo: StateFlow<String?> = _userInfo.asStateFlow()

    private val _isSendingImage = MutableStateFlow(false)
    val isSendingImage: StateFlow<Boolean> = _isSendingImage.asStateFlow()

    fun clearUserInfo() {
        _userInfo.value = null
    }

    private val _uiState = MutableStateFlow(NodesUiState())
    val uiState: StateFlow<NodesUiState> = _uiState.asStateFlow()

    private val _selectedNode = MutableStateFlow<Node?>(null)
    val selectedNode: StateFlow<Node?> = _selectedNode.asStateFlow()

    private var nodeListener: ListenerRegistration? = null

    fun loadNodeDetails(nodeId: String) {
        nodeListener?.remove()
        _selectedNode.value = null
        _nodeMembers.value = emptyList()
        val docRef = firestore.collection("workspaces").document(nodeId)

        nodeListener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                _selectedNode.value = null
                _nodeMembers.value = emptyList()
                return@addSnapshotListener
            }

            if (snapshot?.exists() == true) {
                val node = snapshot.toObject(Node::class.java)?.copy(id = snapshot.id)
                _selectedNode.value = node
                if (node != null) {
                    loadNodeMembers(node.members)
                } else {
                    _nodeMembers.value = emptyList()
                }
            } else {
                _selectedNode.value = null
                _nodeMembers.value = emptyList()
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        entriesListener?.remove()
        nodeListener?.remove()
    }


    init {
        loadNodes()
    }

    fun onNewNodeRequested(name: String) {
        viewModelScope.launch {
            createNode(name)
        }
    }

    private fun loadNodes() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.update {
                it.copy(
                    error = context.getString(R.string.workspace_unauthenticated_error),
                    isLoading = false
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        firestore.collection("workspaces").whereArrayContains("members", userId)
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    _uiState.update {
                        it.copy(
                            error = context.getString(R.string.workspace_load_data_error),
                            isLoading = false
                        )
                    }
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val nodesList = snapshots.documents.mapNotNull { document ->
                        val node = document.toObject(Node::class.java)
                        node?.copy(id = document.id)
                    }
                    _uiState.update { it.copy(workspaces = nodesList, isLoading = false) }
                }
            }
    }


    fun joinNode(nodeId: String) {
        val userId = auth.currentUser?.uid ?: return
        val nodeRef = firestore.collection("workspaces").document(nodeId)

        nodeRef.update("members", FieldValue.arrayUnion(userId)).addOnSuccessListener {
            _userInfo.value = context.getString(R.string.workspace_join_success)
        }.addOnFailureListener { e ->
            _userInfo.value = context.getString(R.string.workspace_join_error)
            println("Error al intentar unirse al node: $e")
        }
    }


    suspend fun createNode(name: String) {
        val db = firestore
        val userId = auth.currentUser?.uid

        if (userId == null) {
            println("Error: Usuario no autenticado.")
            return
        }

        try {
            val newNodeId = generateUniqueNodeId()

            val newNode = Node(
                name = name,
                ownerId = userId,
                members = listOf(userId),
                admins = listOf(userId),
                inviteCode = newNodeId
            )

            db.collection("workspaces").document(newNodeId).set(newNode).await()

            _userInfo.value =
                context.getString(R.string.workspace_create_success, newNode.name)
            println("Firestore: ¡Node '${newNode.name}' creado con éxito con ID: $newNodeId!")

        } catch (e: Exception) {
            _userInfo.value = context.getString(R.string.workspace_create_error)
            println("Firestore: Error al crear el node: $e")
        }
    }

    private suspend fun generateUniqueNodeId(): String {
        val db = firestore
        var nodeId = generateRandomCode(6)
        var isUnique = false


        while (!isUnique) {
            val docRef = db.collection("workspaces").document(nodeId)
            try {

                val document = docRef.get().await()
                if (!document.exists()) {
                    isUnique = true
                } else {
                    println("Colisión de ID encontrada: $nodeId. Regenerando...")
                    nodeId = generateRandomCode(6)
                }
            } catch (e: Exception) {
                println("Error al verificar la unicidad del ID: $e")
                throw e
            }
        }
        return nodeId
    }

    private fun generateRandomCode(length: Int): String {
        val allowedChars = ('A'..'Z') + ('2'..'9')
        return (1..length).asSequence().map { allowedChars.random() }.joinToString("")
    }


    fun addNodeImageEntry(
        nodeId: String,
        imageUri: Uri? = null,
        imageBitmap: Bitmap? = null,
        name: String,
        profilePicture: String,
    ) {
        viewModelScope.launch {
            _isSendingImage.value = true
            var resultMessage: String? = null
            try {
                withTimeout(30.seconds) {
                    val imageData = if (imageUri != null) {
                        ImageUtils.compressImage(context, imageUri)
                    } else if (imageBitmap != null) {
                        ImageUtils.compressBitmap(imageBitmap)
                    } else null

                    if (imageData == null) {
                        resultMessage = context.getString(R.string.workspace_detail_image_process_error)
                        return@withTimeout
                    }

                    val imageUrl = ImageUtils.uploadImage(imageData, "nodes/$nodeId/entries")
                    if (imageUrl == null) {
                        resultMessage = context.getString(R.string.workspace_detail_image_process_error)
                        return@withTimeout
                    }

                    val currentUserId = auth.currentUser?.uid
                    if (currentUserId == null) {
                        resultMessage = context.getString(R.string.workspace_unauthenticated_error)
                        return@withTimeout
                    }

                    val newEntry = NodeEntry(
                        value = imageUrl,
                        type = EntryType.IMAGE,
                        name = name,
                        profilePicture = profilePicture,
                        items = emptyList(),
                        userId = currentUserId,
                        createdAt = null
                    )

                    firestore.collection("workspaces").document(nodeId).collection("entries")
                        .add(newEntry).await()

                    Log.d(TAG, "Nueva entry de imagen añadida con éxito a $nodeId.")
                    resultMessage = context.getString(R.string.chat_image_send_success)
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Timeout al subir la imagen", e)
                resultMessage = context.getString(R.string.chat_image_upload_timeout)
            } catch (e: Exception) {
                Log.e(TAG, "Error al subir imagen y añadir entry", e)
                resultMessage = context.getString(R.string.workspace_entry_add_error)
            } finally {
                _isSendingImage.value = false
                resultMessage?.let { msg ->
                    _userInfo.value = msg
                }
            }
        }
    }

    fun addNodeEntry(
        nodeId: String,
        value: String? = null,
        type: EntryType,
        name: String,
        profilePicture: String,
        items: List<ChecklistItem>? = emptyList(),
    ) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Log.e(TAG, "No se puede añadir entry, usuario no logueado.")
            return
        }

        val newEntry = NodeEntry(
            value = value,
            type = type,
            name = name,
            profilePicture = profilePicture,
            items = items,
            userId = currentUserId,
            createdAt = null
        )


        viewModelScope.launch {
            try {
                firestore.collection("workspaces").document(nodeId).collection("entries")
                    .add(newEntry).await()

                Log.d(TAG, "Nueva entry añadida con éxito a $nodeId.")

            } catch (e: Exception) {
                Log.e(TAG, "Error al añadir la entry a $nodeId", e)
                _userInfo.value = context.getString(R.string.workspace_entry_add_error)
            }
        }
    }


    fun deleteNodeEntry(nodeId: String, entryId: String) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Log.e(TAG, "No se puede eliminar, usuario no logueado.")
            _userInfo.value = context.getString(R.string.workspace_delete_auth_error)
            return
        }

        viewModelScope.launch {
            try {
                firestore.collection("workspaces").document(nodeId).collection("entries")
                    .document(entryId).delete().await()

                _userInfo.value = context.getString(R.string.workspace_delete_entry_success)
                Log.d(TAG, "Entry $entryId eliminada de $nodeId.")

            } catch (e: Exception) {
                Log.e(TAG, "Error al eliminar la entry $entryId de $nodeId", e)
                _userInfo.value = context.getString(R.string.workspace_delete_entry_error)
            }
        }
    }

    fun updateEntryCategory(nodeId: String, entryId: String, newCategory: String) {
        viewModelScope.launch {
            try {
                firestore.collection("workspaces").document(nodeId).collection("entries")
                    .document(entryId).update("category", newCategory).await()

                _userInfo.value =
                    context.getString(R.string.workspace_category_update_success, newCategory)
                Log.d(TAG, "Categoría actualizada para entry $entryId.")

            } catch (e: Exception) {
                Log.e(TAG, "Error al actualizar la categoría", e)
                _userInfo.value = context.getString(R.string.workspace_category_update_error)
            }
        }
    }

    fun reportEntry(nodeId: String, entry: NodeEntry) {
        val currentUserId = auth.currentUser?.uid ?: return

        val report = hashMapOf(
            "reportedBy" to currentUserId,
            "reportedUser" to entry.userId,
            "nodeId" to nodeId,
            "entryId" to entry.id,
            "content" to entry.value,
            "type" to entry.type.name,
            "timestamp" to FieldValue.serverTimestamp(),
            "status" to "PENDING"
        )

        viewModelScope.launch {
            try {
                firestore.collection("reports").add(report).await()
                _userInfo.value = context.getString(R.string.report_success)
                Log.d(TAG, "Reporte enviado para entry ${entry.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar el reporte", e)
                _userInfo.value = context.getString(R.string.report_error)
            }
        }
    }

    private var entriesListener: ListenerRegistration? = null
    private val _nodeEntries = MutableStateFlow<List<NodeEntry>>(emptyList())
    val nodeEntries: StateFlow<List<NodeEntry>> = _nodeEntries.asStateFlow()

    private val _availableCategories = MutableStateFlow<List<String>>(emptyList())
    val availableCategories: StateFlow<List<String>> = _availableCategories.asStateFlow()


    fun loadNodeEntries(nodeId: String) {

        entriesListener?.remove()

        val entriesRef =
            firestore.collection("workspaces").document(nodeId).collection("entries")
                .orderBy("createdAt")

        entriesListener = entriesRef.addSnapshotListener { snapshots, error ->
            if (error != null) {
                println("Error al cargar entries del node: $error")
                _nodeEntries.value = emptyList()
                return@addSnapshotListener
            }

            if (snapshots != null) {
                val entries = snapshots.documents.mapNotNull { doc ->

                    val entry = doc.toObject(NodeEntry::class.java)

                    entry?.copy(id = doc.id)
                }

                _nodeEntries.value = entries

                _availableCategories.value =
                    entries.mapNotNull { it.category }.filter { it.isNotBlank() }.distinct()
                        .sorted()


            } else {
                _nodeEntries.value = emptyList()
            }
        }
    }


    private fun loadNodeMembers(memberIds: List<String>) {

        if (memberIds.isEmpty()) {
            _nodeMembers.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                val memberProfiles = memberIds.mapNotNull { userId ->
                    try {
                        val document = firestore.collection("users").document(userId).get().await()
                        document.toObject(UserProfile::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al cargar el perfil del miembro $userId", e)
                        null
                    }
                }

                _nodeMembers.value = memberProfiles
                Log.d(TAG, "Se cargaron ${memberProfiles.size} perfiles de miembros.")

            } catch (e: Exception) {
                Log.w(TAG, "Error general al cargar los miembros del node", e)
                _nodeMembers.value = emptyList()
            }
        }
    }

    fun updateChecklistItemState(
        nodeId: String, entryId: String, itemIndex: Int, newCheckedState: Boolean
    ) {
        viewModelScope.launch {
            try {
                val entryRef =
                    firestore.collection("workspaces").document(nodeId).collection("entries")
                        .document(entryId)

                val currentEntry = _nodeEntries.value.firstOrNull { it.id == entryId }
                if (currentEntry == null) {
                    Log.e(TAG, "No se encontró la entry $entryId para actualizar")
                    return@launch
                }

                val currentItemsList = currentEntry.items

                if (currentItemsList == null) {
                    Log.e(
                        TAG,
                        "Error: La entry $entryId no es una checklist o sus items no son una lista."
                    )
                    return@launch
                }

                val newItemsList = currentItemsList.toMutableList()

                if (itemIndex < 0 || itemIndex >= newItemsList.size) {
                    Log.e(TAG, "Error: Índice $itemIndex está fuera de rango.")
                    return@launch
                }

                val itemToUpdate = newItemsList[itemIndex]
                newItemsList[itemIndex] = itemToUpdate.copy(isChecked = newCheckedState)

                entryRef.update("items", newItemsList).await()

                Log.d(TAG, "Checklist item $itemIndex en entry $entryId actualizado.")

            } catch (e: Exception) {
                Log.e(TAG, "Error al actualizar el checklist item", e)
                _userInfo.value = context.getString(R.string.workspace_checklist_update_error)
            }
        }
    }


    fun updateNodeName(id: String, newName: String) {

        viewModelScope.launch {

            try {
                nodeRepository.actualizarNombreEnFirebase(id, newName)

                var nodeActualizado: Node? = null

                _uiState.update { estadoActual ->
                    val updatedNodes = estadoActual.workspaces.map { node ->
                        if (node.id == id) {
                            val copiaActualizada = node.copy(name = newName)
                            nodeActualizado = copiaActualizada
                            copiaActualizada
                        } else {
                            node
                        }
                    }
                    estadoActual.copy(workspaces = updatedNodes)
                }

                nodeActualizado?.let {
                    _selectedNode.value = it
                }

                _userInfo.value = context.getString(R.string.workspace_name_update_success)

            } catch (e: Exception) {
                Log.e("ViewModelError", "Error al actualizar nombre: ${e.message}")

                _userInfo.value = context.getString(
                    R.string.workspace_generic_save_error, e.message ?: "Error desconocido"
                )
            }
        }
    }

    fun updateNodePicture(nodeId: String, newPicture: String) {

        viewModelScope.launch {
            try {
                nodeRepository.updateNodePicture(nodeId, newPicture)

                var nodeActualizado: Node? = null

                _uiState.update { estadoActual ->
                    val updatedNodes = estadoActual.workspaces.map { node ->
                        if (node.id == nodeId) {
                            val copiaActualizada = node.copy(workspacePicture = newPicture)
                            nodeActualizado = copiaActualizada
                            copiaActualizada
                        } else {
                            node
                        }
                    }
                    estadoActual.copy(workspaces = updatedNodes)
                }

                nodeActualizado?.let {
                    _selectedNode.value = it
                }

                _userInfo.value = context.getString(R.string.workspace_picture_update_success)

            } catch (e: Exception) {
                Log.e("ViewModelError", "Error al actualizar la imagen: ${e.message}")

                _userInfo.value = context.getString(
                    R.string.workspace_generic_save_error, e.message ?: "Error desconocido"
                )
            }
        }
    }

    fun toggleAdminRole(nodeId: String, targetUid: String, makeAdmin: Boolean) {
        viewModelScope.launch {
            try {
                val nodeRef = firestore.collection("workspaces").document(nodeId)
                if (makeAdmin) {
                    nodeRef.update("admins", FieldValue.arrayUnion(targetUid)).await()
                } else {
                    nodeRef.update("admins", FieldValue.arrayRemove(targetUid)).await()
                }
                Log.d(TAG, "Rol de admin actualizado para $targetUid en $nodeId")
            } catch (e: Exception) {
                Log.e(TAG, "Error al actualizar rol de admin", e)
                _userInfo.value = context.getString(R.string.error_update_permissions)
            }
        }
    }

    fun leaveNode(node: Node, onSuccess: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val isAdmin = node.admins.contains(userId) || node.ownerId == userId

        viewModelScope.launch {
            try {
                if (isAdmin) {
                    // ADMIN: Borrar todo el nodo y sus entradas
                    val nodeRef = firestore.collection("workspaces").document(node.id)
                    
                    // Borrar entradas (batch)
                    val entriesSnapshot = nodeRef.collection("entries").get().await()
                    val batch = firestore.batch()
                    entriesSnapshot.documents.forEach { batch.delete(it.reference) }
                    batch.commit().await()

                    // Borrar documento principal
                    nodeRef.delete().await()
                    
                    // Nota: Las imágenes en Storage idealmente se borran via Cloud Functions
                    // para evitar loops pesados en el cliente.
                } else {
                    // USER: Simplemente remover de la lista de miembros
                    val nodeRef = firestore.collection("workspaces").document(node.id)
                    nodeRef.update("members", FieldValue.arrayRemove(userId)).await()
                }
                
                _userInfo.value = context.getString(R.string.leave_node_success)
                onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Error al salir del nodo", e)
                _userInfo.value = context.getString(R.string.leave_node_error)
            }
        }
    }

    fun uploadNodePictureFromUri(nodeId: String, uri: Uri, context: Context) {
        viewModelScope.launch {
            _isSendingImage.value = true
            var resultMsg: String? = null
            try {
                withTimeout(30.seconds) {
                    val compressedData = ImageUtils.compressImage(context, uri)

                    if (compressedData == null) {
                        resultMsg = context.getString(R.string.workspace_detail_image_process_error)
                        return@withTimeout
                    }
                    uploadNodeBytesToStorage(nodeId, compressedData)
                    resultMsg = context.getString(R.string.workspace_picture_update_success)
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Timeout al subir imagen del nodo", e)
                resultMsg = context.getString(R.string.chat_image_upload_timeout)
            } catch (e: Exception) {
                Log.e("ViewModelError", "Error subiendo imagen desde Uri: ${e.message}")
                resultMsg = context.getString(R.string.workspace_detail_image_process_error)
            } finally {
                _isSendingImage.value = false
                resultMsg?.let { msg ->
                    _userInfo.value = msg
                }
            }
        }
    }

    fun uploadNodePictureFromBitmap(nodeId: String, bitmap: Bitmap) {
        viewModelScope.launch {
            _isSendingImage.value = true
            var resultMsg: String? = null
            try {
                withTimeout(30.seconds) {
                    val compressedData = ImageUtils.compressBitmap(bitmap)

                    if (compressedData == null) {
                        resultMsg = context.getString(R.string.workspace_detail_image_process_error)
                        return@withTimeout
                    }
                    uploadNodeBytesToStorage(nodeId, compressedData)
                    resultMsg = context.getString(R.string.workspace_picture_update_success)
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Timeout al subir imagen del nodo", e)
                resultMsg = context.getString(R.string.chat_image_upload_timeout)
            } catch (e: Exception) {
                Log.e("ViewModelError", "Error subiendo imagen desde Bitmap: ${e.message}")
                resultMsg = context.getString(R.string.workspace_detail_image_process_error)
            } finally {
                _isSendingImage.value = false
                resultMsg?.let { msg ->
                    _userInfo.value = msg
                }
            }
        }
    }


    private suspend fun uploadNodeBytesToStorage(nodeId: String, data: ByteArray) {
        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("node_pictures/$nodeId/${UUID.randomUUID()}.jpg")

        imageRef.putBytes(data).await()

        val downloadUrl = imageRef.downloadUrl.await().toString()

        updateNodePicture(nodeId = nodeId, newPicture = downloadUrl)
    }
}


class NodesViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(NodesViewModel::class.java)) {

            val firestore: FirebaseFirestore = Firebase.firestore
            val auth: FirebaseAuth = FirebaseAuth.getInstance()

            val repository = NodeRepository(firestore, auth)

            return NodesViewModel(firestore, auth, repository, application) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
