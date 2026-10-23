package com.maxigimenez.nodes.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.maxigimenez.nodes.features.auth.LoginPage
import com.maxigimenez.nodes.features.auth.SignUpPage
import com.maxigimenez.nodes.features.auth.AuthViewModel
import com.maxigimenez.nodes.features.auth.AuthState
import com.maxigimenez.nodes.features.settings.SettingsViewModel

import com.maxigimenez.nodes.core.ui.theme.NodesAnimations

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.observeAsState()
    val pendingWorkspaceId by authViewModel.pendingWorkspaceId.collectAsState()

    LaunchedEffect(authState, pendingWorkspaceId) {
        if (authState is AuthState.Authenticated && pendingWorkspaceId != null) {
            if (navController.currentBackStackEntry?.destination?.route != "home") {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(
        navController = navController, 
        startDestination = "login", 
        modifier = modifier.fillMaxSize(),
        enterTransition = { NodesAnimations.enterTransition() },
        exitTransition = { NodesAnimations.exitTransition() },
        popEnterTransition = { NodesAnimations.popEnterTransition() },
        popExitTransition = { NodesAnimations.popExitTransition() }
    ) {
        composable("login") {
            LoginPage(Modifier.fillMaxSize(), authViewmodel = authViewModel, navController = navController)
        }

        composable("signup") {
            SignUpPage(Modifier.fillMaxSize(), authViewmodel = authViewModel, navController = navController)
        }

        composable("home") {
            ContentNavigation(Modifier.fillMaxSize(), authViewModel, settingsViewModel, navController)
        }
    }
}
