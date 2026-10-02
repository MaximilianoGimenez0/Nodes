package com.maxigimenez.nodes.core.navigation


import android.app.Application
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.maxigimenez.nodes.core.ui.components.MainBottomBar
import com.maxigimenez.nodes.features.home.HomePage
import com.maxigimenez.nodes.features.nodes.list.NodesPage
import com.maxigimenez.nodes.features.settings.SettingsPage
import com.maxigimenez.nodes.features.auth.AuthState
import com.maxigimenez.nodes.features.auth.AuthViewModel
import com.maxigimenez.nodes.features.settings.SettingsViewModel
import com.maxigimenez.nodes.features.auth.UserViewModel
import com.maxigimenez.nodes.features.auth.UserViewModelFactory
import com.maxigimenez.nodes.features.nodes.list.NodesViewModel
import com.maxigimenez.nodes.features.nodes.list.NodesViewModelFactory
import com.maxigimenez.nodes.features.nodes.detail.NodeDetailScreen
import com.maxigimenez.nodes.features.nodes.detail.NodeDetailsContent

import com.maxigimenez.nodes.core.ui.theme.NodesAnimations

@Composable
fun ContentNavigation(
    modifier: Modifier,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    navController: NavController
) {
    val contentNavController = rememberNavController()

    val authState = authViewModel.authState.observeAsState()

    val context = LocalContext.current
    val application = context.applicationContext as Application

    val factory = NodesViewModelFactory(application)
    val nodesViewModel: NodesViewModel = viewModel(factory = factory)

    val userViewmodel: UserViewModel = viewModel(
        factory = UserViewModelFactory(
            application
        )
    )

    val pendingWorkspaceId by authViewModel.pendingWorkspaceId.collectAsState()

    LaunchedEffect(authState.value, pendingWorkspaceId) {
        val state = authState.value
        
        // 1. Manejo de desautenticación
        if (state is AuthState.UnAuthenticated) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
            return@LaunchedEffect
        }

        // 2. Manejo de navegación pendiente (Notificaciones)
        if (state is AuthState.Authenticated && pendingWorkspaceId != null) {
            // Navegamos al chat específico
            contentNavController.navigate("chat/$pendingWorkspaceId") {
                launchSingleTop = true
            }
            // Limpiamos el ID para evitar navegaciones infinitas en recomposiciones
            authViewModel.clearPendingWorkspaceId()
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            MainBottomBar(contentNavController)
        },
        containerColor = Color.Transparent, // Permitimos que el fondo de las pantallas fluya
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        content = { innerPadding ->
            NavHost(
                navController = contentNavController,
                startDestination = Routes.Home.route,
                modifier = Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(innerPadding),
                enterTransition = { NodesAnimations.enterTransition() },
                exitTransition = { NodesAnimations.exitTransition() },
                popEnterTransition = { NodesAnimations.popEnterTransition() },
                popExitTransition = { NodesAnimations.popExitTransition() }
            ) {
            // Grafo de Workspaces: Lista -> Chat -> Info
            navigation(startDestination = "workspaces", route = Routes.Nodes.route) {
                composable("workspaces") {
                    NodesPage(
                        modifier = Modifier.fillMaxSize(),
                        outerPadding = innerPadding,
                        contentController = contentNavController,
                        nodesViewModel = nodesViewModel
                    )
                }
                composable(
                    "chat/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.StringType })
                ) { backStackEntry ->
                    val nodeId = backStackEntry.arguments?.getString("id")
                    if (nodeId != null) {
                        NodeDetailScreen(
                            nodeId = nodeId,
                            nodesViewModel = nodesViewModel,
                            contentNavController = contentNavController,
                            modifier = Modifier.fillMaxSize(),
                            authViewModel = authViewModel,
                            outerPadding = innerPadding
                        )
                    }
                }
                composable(
                    "info/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.StringType })
                ) { backStackEntry ->
                    val nodeId = backStackEntry.arguments?.getString("id")
                    if (nodeId != null) {
                        NodeDetailsContent(
                            nodesViewModel = nodesViewModel,
                            authViewModel = authViewModel,
                            modifier = Modifier.fillMaxSize(),
                            contentNavController = contentNavController,
                            outerPadding = innerPadding
                        )
                    }
                }
            }

            composable("home") {
                HomePage(
                    modifier = Modifier.fillMaxSize(),
                    outerPadding = innerPadding,
                    authViewmodel = authViewModel,
                    nodesViewModel = nodesViewModel,
                    navController = navController,
                    contentNavController = contentNavController
                )
            }

            composable("settings") {
                SettingsPage(
                    modifier = Modifier.fillMaxSize(),
                    outerPadding = innerPadding,
                    authViewmodel = authViewModel,
                    settingsViewModel = settingsViewModel,
                    navController = navController,
                    userViewModel = userViewmodel,
                    contentController = contentNavController
                )
            }


        }
    })

}
