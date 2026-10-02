package com.maxigimenez.nodes.features.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.maxigimenez.nodes.core.ui.components.CustomLoginField
import com.maxigimenez.nodes.core.util.AuthValidator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import com.maxigimenez.nodes.core.ui.components.NodesBackground
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import android.content.Intent
import android.net.Uri
import com.maxigimenez.nodes.BuildConfig
import com.maxigimenez.nodes.core.ui.theme.entranceAnimation

@Composable
fun SignUpPage(modifier: Modifier, authViewmodel: AuthViewModel, navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordHidden by remember { mutableStateOf(true) }
    var firstname by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }
    var termsAccepted by rememberSaveable { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var firstnameError by remember { mutableStateOf<String?>(null) }
    var lastnameError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val authState = authViewmodel.authState.observeAsState()

    val invalidEmailMsg = stringResource(R.string.validation_email_invalid)
    val shortPasswordMsg = stringResource(R.string.validation_password_too_short)
    val fieldRequiredMsg = stringResource(R.string.validation_field_required)

    LaunchedEffect(authState.value) {
        when (val state = authState.value) {
            is AuthState.Authenticated -> navController.navigate("home")
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Branding Section
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
                    text = stringResource(id = R.string.home_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Form Section
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
                    .entranceAnimation(delay = 300),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.signup_title),
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

                    Spacer(modifier = Modifier.height(16.dp))

                    CustomLoginField(
                        value = firstname,
                        onValueChange = {
                            firstname = it
                            firstnameError = null
                        },
                        placeholder = stringResource(id = R.string.common_firstname),
                        errorText = firstnameError,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CustomLoginField(
                        value = lastname,
                        onValueChange = {
                            lastname = it
                            lastnameError = null
                        },
                        placeholder = stringResource(id = R.string.common_lastname),
                        errorText = lastnameError,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = termsAccepted,
                            onCheckedChange = { termsAccepted = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                uncheckedColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        
                        val part1 = stringResource(R.string.signup_terms_accept_part1)
                        val link = stringResource(R.string.signup_terms_accept_link)
                        val part2 = stringResource(R.string.signup_terms_accept_part2)

                        val annotatedString = buildAnnotatedString {
                            append(part1)
                            append(" ")
                            pushStringAnnotation(tag = "terms", annotation = BuildConfig.TERMS_URL)
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append(link)
                            }
                            pop()
                            append(" ")
                            append(part2)
                        }   

                        ClickableText(
                            text = annotatedString,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            onClick = { offset ->
                                annotatedString.getStringAnnotations(tag = "terms", start = offset, end = offset)
                                    .firstOrNull()?.let { annotation ->
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                                        context.startActivity(intent)
                                    }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            if (!termsAccepted) return@Button

                            val isEmailValid = AuthValidator.isValidEmail(email)
                            val isPasswordValid = AuthValidator.isValidPassword(password)
                            val isFirstnameValid = AuthValidator.isNotBlank(firstname)
                            val isLastnameValid = AuthValidator.isNotBlank(lastname)

                            if (!isEmailValid) emailError = invalidEmailMsg
                            if (!isPasswordValid) passwordError = shortPasswordMsg
                            if (!isFirstnameValid) firstnameError = fieldRequiredMsg
                            if (!isLastnameValid) lastnameError = fieldRequiredMsg

                            if (isEmailValid && isPasswordValid && isFirstnameValid && isLastnameValid) {
                                authViewmodel.signUp(email, password, firstname, lastname, termsAccepted)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = authState.value != AuthState.Loading && termsAccepted
                    ) {
                        if (authState.value == AuthState.Loading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = stringResource(id = R.string.signup_button_signup),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Footer Section
            TextButton(
                onClick = { navController.navigate("login") },
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.signup_navigate_to_login_link),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
