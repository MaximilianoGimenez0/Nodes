package com.maxigimenez.nodes.app

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maxigimenez.nodes.core.navigation.AppNavigation
import com.maxigimenez.nodes.core.ui.theme.NodesTheme
import com.maxigimenez.nodes.core.util.LocaleHelper
import com.maxigimenez.nodes.features.auth.AuthViewModel
import com.maxigimenez.nodes.features.auth.AuthViewModelFactory
import com.maxigimenez.nodes.features.settings.SettingsViewModel
import com.maxigimenez.nodes.features.settings.SettingsViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val application = context.applicationContext as Application

            authViewModel = viewModel(
                factory = AuthViewModelFactory(application = application),
            )

            // Procesar intent inicial (Cold Start)
            LaunchedEffect(intent) {
                handleIntent(intent)
            }

            val settingsManager = (context.applicationContext as NodesApplication).settingsManager
            val settingsViewModelFactory = remember(settingsManager) {
                SettingsViewModelFactory(settingsManager)
            }
            val settingsViewModel: SettingsViewModel = viewModel(factory = settingsViewModelFactory)
            val settings by settingsViewModel.appSettings.collectAsState()

            LaunchedEffect(settings.language) {
                LocaleHelper.setLocale(settings.language)
            }

            val systemIsDark = isSystemInDarkTheme()
            val darkTheme: Boolean = when (settings.theme) {
                "Claro" -> false
                "Oscuro" -> true
                else -> systemIsDark
            }

            val baseDensity = LocalDensity.current
            val newDensity = remember(settings.fontSize, baseDensity) {
                val fontScale = when (settings.fontSize) {
                    0 -> 0.85f
                    1 -> 1.0f
                    2 -> 1.15f
                    else -> 1.0f
                }
                Density(density = baseDensity.density, fontScale = fontScale)
            }

            NodesTheme(darkTheme = darkTheme, dynamicColor = false) {
                CompositionLocalProvider(LocalDensity provides newDensity) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation(
                            modifier = Modifier.fillMaxSize(),
                            authViewModel = authViewModel,
                            settingsViewModel = settingsViewModel
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val workspaceId = intent?.getStringExtra("EXTRA_WORKSPACE_ID")
        if (!workspaceId.isNullOrBlank()) {
            if (::authViewModel.isInitialized) {
                authViewModel.setPendingWorkspaceId(workspaceId)
            }
        }
    }
}

@Composable
fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val context = LocalContext.current
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    Log.d("Permissions", "Permiso de notificación CONCEDIDO")
                } else {
                    Log.w("Permissions", "Permiso de notificación DENEGADO")
                }
            }

        LaunchedEffect(key1 = Unit) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
