package com.maxigimenez.nodes.features.auth

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import com.maxigimenez.nodes.R
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.maxigimenez.nodes.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.ActionCodeSettings
import com.maxigimenez.nodes.BuildConfig

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    val db = FirebaseFirestore.getInstance()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableLiveData<AuthState>()

    val authState: LiveData<AuthState> = _authState

    private val _userId = MutableLiveData<String?>()
    val userId: LiveData<String?> = _userId

    private val _pendingWorkspaceId = MutableStateFlow<String?>(null)
    val pendingWorkspaceId = _pendingWorkspaceId.asStateFlow()

    fun setPendingWorkspaceId(id: String?) {
        _pendingWorkspaceId.value = id
    }

    fun clearPendingWorkspaceId() {
        _pendingWorkspaceId.value = null
    }


    init {
        checkAuthStatus()

        auth.addAuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            updateUser(currentUser)

            currentUser?.let {
                loadUserProfile(it.uid)
                updateFcmToken()
            }
        }
    }

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    fun loadUserProfile(uid: String) {
        viewModelScope.launch {
            val profile = getUserProfile(uid)
            _userProfile.value = profile
        }
    }

    private fun updateUser(user: FirebaseUser?) {
        if (user != null) {
            _authState.value = AuthState.Authenticated
            _userId.value = user.uid
        } else {
            _authState.value = AuthState.UnAuthenticated
            _userId.value = null
        }
    }

    fun checkAuthStatus() {

        if (auth.currentUser == null) {
            _authState.value = AuthState.UnAuthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error(context.getString(R.string.auth_error_empty_fields))
            return
        }

        _authState.value = AuthState.Loading

        viewModelScope.launch {
            try {

                auth.signInWithEmailAndPassword(email, password).await()

                _authState.value = AuthState.Authenticated

            } catch (exception: Exception) {

                val errorMessage = when (exception) {
                    is FirebaseAuthInvalidUserException -> context.getString(R.string.auth_error_invalid_user)
                    is FirebaseAuthInvalidCredentialsException -> context.getString(R.string.auth_error_invalid_credentials)
                    is FirebaseNetworkException -> context.getString(R.string.auth_error_network)
                    is FirebaseAuthException -> {
                        when (exception.errorCode) {
                            "ERROR_INVALID_EMAIL" -> context.getString(R.string.auth_error_invalid_email)
                            "ERROR_USER_DISABLED" -> context.getString(R.string.auth_error_user_disabled)
                            "ERROR_TOO_MANY_REQUESTS" -> context.getString(R.string.auth_error_too_many_requests)
                            "ERROR_OPERATION_NOT_ALLOWED" -> context.getString(R.string.auth_error_operation_not_allowed)
                            else -> context.getString(R.string.auth_error_unknown)
                        }
                    }
                    else -> context.getString(R.string.auth_error_unknown)
                }

                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }

    fun sendPasswordResetEmail(email: String, onComplete: (Boolean, String?) -> Unit) {
        if (email.isBlank()) {
            onComplete(false, context.getString(R.string.auth_error_empty_fields))
            return
        }

        viewModelScope.launch {
            try {
                // 1. Configuras la URL de destino
                val actionCodeSettings = ActionCodeSettings.newBuilder()
                    .setUrl(BuildConfig.PASSWORD_RESET_URL)
                    .setHandleCodeInApp(false) // false para que abra el navegador web
                    .build()

                // 2. Pasas las configuraciones en la llamada
                auth.sendPasswordResetEmail(email, actionCodeSettings).await()

                onComplete(true, null)
            } catch (exception: Exception) {
                val errorMessage = when (exception) {
                    is FirebaseNetworkException -> context.getString(R.string.auth_error_network)
                    is FirebaseAuthException -> {
                        when (exception.errorCode) {
                            "ERROR_INVALID_EMAIL" -> context.getString(R.string.auth_error_invalid_email)
                            "ERROR_TOO_MANY_REQUESTS" -> context.getString(R.string.auth_error_too_many_requests)
                            else -> context.getString(R.string.auth_error_unknown)
                        }
                    }
                    else -> context.getString(R.string.auth_error_unknown)
                }
                onComplete(false, errorMessage)
            }
        }
    }

    fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        termsAccepted: Boolean = false,
    ) {
        if (!termsAccepted) {
            _authState.value = AuthState.Error(context.getString(R.string.auth_terms_error))
            return
        }

        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error(context.getString(R.string.auth_error_empty_fields))
            return
        }

        _authState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val uuid = authResult.user?.uid

                if (uuid != null) {
                    val userProfile = mapOf(
                        "id" to uuid,
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "email" to email,
                        "profilePicture" to null,
                        "blockedUsers" to emptyList<String>()
                    )

                    db.collection("users").document(uuid).set(userProfile).await()
                    _authState.value = AuthState.Authenticated
                } else {
                    throw Exception("empty uuid")
                }

            } catch (exception: Exception) {
                val errorMessage = when (exception) {
                    is FirebaseAuthUserCollisionException -> context.getString(R.string.auth_error_user_collision)
                    is FirebaseAuthWeakPasswordException -> context.getString(R.string.auth_error_weak_password)
                    is FirebaseNetworkException -> context.getString(R.string.auth_error_network)
                    is FirebaseAuthException -> {
                        when (exception.errorCode) {
                            "ERROR_INVALID_EMAIL" -> context.getString(R.string.auth_error_invalid_email)
                            "ERROR_USER_DISABLED" -> context.getString(R.string.auth_error_user_disabled)
                            "ERROR_TOO_MANY_REQUESTS" -> context.getString(R.string.auth_error_too_many_requests)
                            "ERROR_OPERATION_NOT_ALLOWED" -> context.getString(R.string.auth_error_operation_not_allowed)
                            else -> context.getString(R.string.auth_error_unknown)
                        }
                    }
                    else -> context.getString(R.string.auth_error_unknown)
                }

                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }

    fun signOut() {

        val userId = auth.currentUser?.uid

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful && userId != null) {
                val token = task.result


                db.collection("users").document(userId).collection("tokens").document(token)
                    .delete().addOnCompleteListener {
                        auth.signOut()
                        _authState.value = AuthState.UnAuthenticated
                    }
            } else {
                auth.signOut()
                _authState.value = AuthState.UnAuthenticated
            }
        }
    }


    suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            val snapshot = db.collection("users").document(uid).get().await()

            snapshot.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }


    fun updateFcmToken() {
        val userId = auth.currentUser?.uid ?: return

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Error al obtener el token actual", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("FCM", "Token obtenido en Login: $token")

            val tokenData = hashMapOf(
                "token" to token, "lastUsed" to System.currentTimeMillis()
            )

            db.collection("users").document(userId).collection("tokens").document(token)
                .set(tokenData).addOnSuccessListener {
                    Log.d("FCM", "Token guardado en Login exitosamente.")
                }
        }
    }

    fun resetState() {
        _authState.value = AuthState.UnAuthenticated
    }

    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        _authState.value = AuthState.Loading
        val functions = FirebaseFunctions.getInstance()

        functions.getHttpsCallable("deleteUserAccount")
            .call()
            .addOnSuccessListener {
                signOut()
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e("AuthViewModel", "Error al eliminar cuenta", exception)
                _authState.value = AuthState.Error(exception.message ?: context.getString(R.string.auth_error_unknown))
                onError(exception.message ?: context.getString(R.string.auth_error_unknown))
            }
    }

    fun blockUser(targetUid: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                db.collection("users").document(currentUid)
                    .update("blockedUsers", FieldValue.arrayUnion(targetUid))
                    .await()

                // Actualizar el perfil local
                val currentProfile = _userProfile.value
                if (currentProfile != null) {
                    val updatedBlockedList = currentProfile.blockedUsers.toMutableList().apply {
                        if (!contains(targetUid)) add(targetUid)
                    }
                    _userProfile.value = currentProfile.copy(blockedUsers = updatedBlockedList)
                }

                onSuccess()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error al bloquear usuario", e)
                onError(context.getString(R.string.block_user_error))
            }
        }
    }

    fun unblockUser(targetUid: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                db.collection("users").document(currentUid)
                    .update("blockedUsers", FieldValue.arrayRemove(targetUid))
                    .await()

                // Actualizar el perfil local
                val currentProfile = _userProfile.value
                if (currentProfile != null) {
                    val updatedBlockedList = currentProfile.blockedUsers.toMutableList().apply {
                        remove(targetUid)
                    }
                    _userProfile.value = currentProfile.copy(blockedUsers = updatedBlockedList)
                }

                onSuccess()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error al desbloquear usuario", e)
                onError(context.getString(R.string.unblock_user_error))
            }
        }
    }
}

sealed class AuthState {
    object Authenticated : AuthState()
    object UnAuthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}



class AuthViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
