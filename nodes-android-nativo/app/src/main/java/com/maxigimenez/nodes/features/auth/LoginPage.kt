package com.maxigimenez.nodes.features.auth

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.maxigimenez.nodes.core.util.ToastHelper
import com.maxigimenez.nodes.R
import com.maxigimenez.nodes.core.util.AuthValidator
import com.maxigimenez.nodes.core.ui.components.CustomLoginField
import com.maxigimenez.nodes.core.ui.components.NodesBackground

import com.maxigimenez.nodes.core.ui.theme.entranceAnimation

class ParticleNode(
    var x: Float,
    var y: Float,
    var dx: Float,
    var dy: Float,
    val radius: Float
)

@Composable
fun LoginPage(modifier: Modifier = Modifier, authViewmodel: AuthViewModel, navController: NavController) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordHidden by remember { mutableStateOf(true) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetEmailError by remember { mutableStateOf<String?>(null) }
    var isResetLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val authState = authViewmodel.authState.observeAsState()

    val invalidEmailMsg = stringResource(R.string.validation_email_invalid)
    val shortPasswordMsg = stringResource(R.string.validation_password_too_short)
    val resetSuccessMsg = stringResource(R.string.reset_password_success_message)

    LaunchedEffect(authState.value) {
        when (val state = authState.value) {
            is AuthState.Authenticated -> {
                navController.navigate("home")
            }
            is AuthState.Error -> {
                ToastHelper.showToast(context, state.message)
                authViewmodel.resetState()
            }
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            )
    ) {
        NodesBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.entranceAnimation(delay = 100)
            ) {
                Text(
                    text = "NODES",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 8.sp
                )
                Text(
                    text = stringResource(id = R.string.login_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
                    .entranceAnimation(delay = 300),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.login_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    CustomLoginField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                        },
                        placeholder = stringResource(id = R.string.common_email),
                        errorText = emailError,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CustomLoginField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                        },
                        placeholder = stringResource(id = R.string.common_password),
                        isPassword = true,
                        isPasswordHidden = isPasswordHidden,
                        onVisibilityClick = { isPasswordHidden = !isPasswordHidden },
                        errorText = passwordError,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            val isEmailValid = AuthValidator.isValidEmail(email)
                            val isPasswordValid = AuthValidator.isValidPassword(password)

                            if (!isEmailValid) emailError = invalidEmailMsg
                            if (!isPasswordValid) passwordError = shortPasswordMsg

                            if (isEmailValid && isPasswordValid) {
                                authViewmodel.login(email, password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = authState.value != AuthState.Loading
                    ) {
                        if (authState.value == AuthState.Loading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = stringResource(id = R.string.login_button_login),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = {
                            resetEmail = email
                            resetEmailError = null
                            showResetDialog = true
                        }
                    ) {
                        Text(
                            text = stringResource(id = R.string.login_forgot_password),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (showResetDialog) {
                AlertDialog(
                    onDismissRequest = {
                        if (!isResetLoading) {
                            showResetDialog = false
                        }
                    },
                    title = {
                        Text(
                            text = stringResource(id = R.string.reset_password_dialog_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = stringResource(id = R.string.reset_password_dialog_message),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            CustomLoginField(
                                value = resetEmail,
                                onValueChange = {
                                    resetEmail = it
                                    resetEmailError = null
                                },
                                placeholder = stringResource(id = R.string.common_email),
                                errorText = resetEmailError,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val isEmailValid = AuthValidator.isValidEmail(resetEmail)
                                if (!isEmailValid) {
                                    resetEmailError = invalidEmailMsg
                                    return@Button
                                }
                                isResetLoading = true
                                authViewmodel.sendPasswordResetEmail(resetEmail) { success, _ ->
                                    isResetLoading = false
                                    showResetDialog = false
                                    ToastHelper.showToast(context, resetSuccessMsg)
                                }
                            },
                            enabled = !isResetLoading
                        ) {
                            if (isResetLoading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(text = stringResource(id = R.string.reset_password_dialog_send))
                            }
                        }
                    },
                    dismissButton = {
                        if (!isResetLoading) {
                            TextButton(
                                onClick = { showResetDialog = false }
                            ) {
                                Text(text = stringResource(id = R.string.cancel))
                            }
                        }
                    }
                )
            }

            TextButton(
                onClick = { navController.navigate("signup") },
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.login_navigate_to_signup_link),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
