package com.maxigimenez.nodes.features.auth

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.maxigimenez.nodes.R
import com.maxigimenez.nodes.core.util.ImageUtils
import com.maxigimenez.nodes.data.model.UserProfile
import com.maxigimenez.nodes.data.repository.UserRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds
import java.util.UUID


class UserViewModel(
    private val userRepository: UserRepository,
    application: Application,
) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    private val _userInfo = MutableStateFlow<String?>(null)
    val userInfo: StateFlow<String?> = _userInfo.asStateFlow()

    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage: StateFlow<Boolean> = _isUploadingImage.asStateFlow()

    fun clearUserInfo() {
        _userInfo.value = null
    }

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    fun loadUserProfile(uid: String) {
        viewModelScope.launch {
            val profile = userRepository.getUserProfile(uid)
            _userProfile.value = profile
        }
    }

    fun updateUserInfo(uid: String, firstName: String, lastName: String) {
        viewModelScope.launch {
            try {
                userRepository.updateUserInfo(uid, firstName, lastName)

                _userProfile.update { currentUser ->
                    currentUser?.copy(firstName = firstName, lastName = lastName)
                }

                _userInfo.value = context.getString(R.string.profile_data_update_success)

            } catch (e: Exception) {
                Log.e("ViewModelError", "Error al guardar los datos: ${e.message}")

                _userInfo.value = context.getString(
                    R.string.workspace_generic_save_error, e.message ?: "Error"
                )
            }
        }
    }

    fun uploadProfilePictureFromUri(userId: String, uri: Uri, context: Context) {
        viewModelScope.launch {
            _isUploadingImage.value = true
            var resultMessage: String? = null
            try {
                withTimeout(30.seconds) {
                    val compressedData = ImageUtils.compressImage(context, uri)

                    if (compressedData == null) {
                        resultMessage = context.getString(R.string.workspace_detail_image_process_error)
                        return@withTimeout
                    }
                    uploadBytesToStorage(userId, compressedData)
                    resultMessage = context.getString(R.string.profile_picture_update_success)
                }
            } catch (e: TimeoutCancellationException) {
                Log.e("UserViewModel", "Timeout al subir foto de perfil", e)
                resultMessage = context.getString(R.string.chat_image_upload_timeout)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error al subir foto de perfil", e)
                resultMessage = context.getString(R.string.error_image)
            } finally {
                _isUploadingImage.value = false
                resultMessage?.let { msg ->
                    _userInfo.value = msg
                }
            }
        }
    }

    // 2. Subir imagen desde la Cámara (Usa Bitmap)
    fun uploadProfilePictureFromBitmap(userId: String, bitmap: Bitmap) {
        viewModelScope.launch {
            _isUploadingImage.value = true
            var resultMessage: String? = null
            try {
                withTimeout(30.seconds) {
                    val compressedData = ImageUtils.compressBitmap(bitmap)

                    if (compressedData == null) {
                        resultMessage = context.getString(R.string.workspace_detail_image_process_error)
                        return@withTimeout
                    }
                    uploadBytesToStorage(userId, compressedData)
                    resultMessage = context.getString(R.string.profile_picture_update_success)
                }
            } catch (e: TimeoutCancellationException) {
                Log.e("UserViewModel", "Timeout al subir foto de perfil", e)
                resultMessage = context.getString(R.string.chat_image_upload_timeout)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error al subir foto de perfil", e)
                resultMessage = context.getString(R.string.error_image)
            } finally {
                _isUploadingImage.value = false
                resultMessage?.let { msg ->
                    _userInfo.value = msg
                }
            }
        }
    }

    private suspend fun uploadBytesToStorage(userId: String, data: ByteArray) {
        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("profile_pictures/$userId/${UUID.randomUUID()}.jpg")

        // Subimos el arreglo de bytes comprimido
        imageRef.putBytes(data).await()
        val downloadUrl = imageRef.downloadUrl.await().toString()
        userRepository.updateUserPicture(userId, downloadUrl)
        loadUserProfile(userId)
    }

}

class UserViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    private val repository = UserRepository()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(repository, application) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
