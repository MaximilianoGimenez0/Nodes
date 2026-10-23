package com.maxigimenez.nodes.features.home

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.maxigimenez.nodes.R
import com.maxigimenez.nodes.app.RequestNotificationPermission
import com.maxigimenez.nodes.features.auth.AuthState
import com.maxigimenez.nodes.features.auth.AuthViewModel
import kotlinx.coroutines.delay
import kotlin.math.sqrt
import kotlin.random.Random
import com.maxigimenez.nodes.core.ui.theme.entranceAnimation
import com.maxigimenez.nodes.core.ui.theme.pressClickEffect
import com.maxigimenez.nodes.core.ui.components.NodesBackground


import com.maxigimenez.nodes.features.nodes.list.NodesViewModel
import com.maxigimenez.nodes.features.nodes.list.NodeAction
import com.maxigimenez.nodes.core.navigation.Routes

@Composable
fun HomePage(
    modifier: Modifier,
    outerPadding: PaddingValues,
    authViewmodel: AuthViewModel,
    nodesViewModel: NodesViewModel,
    navController: NavController,
    contentNavController: NavController
) {
    val authState = authViewmodel.authState.observeAsState()
    val currentUser = authViewmodel.userProfile.collectAsState()
    val context = LocalContext.current
    val appVersion = remember { getAppVersion(context) }

    RequestNotificationPermission()

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.UnAuthenticated -> {
                navController.navigate("login") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
            else -> Unit
        }
    }

    if (authState.value is AuthState.Authenticated) {
        val userName = currentUser.value?.firstName?.split(" ")?.firstOrNull() ?: "Usuario"

        DashboardContent(
            modifier = modifier,
            outerPadding = outerPadding,
            userName = userName,
            appVersion = appVersion,
            nodesViewModel = nodesViewModel,
            navController = navController,
            contentNavController = contentNavController
        )

    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    modifier: Modifier,
    outerPadding: PaddingValues,
    userName: String,
    appVersion: String,
    nodesViewModel: NodesViewModel,
    navController: NavController,
    contentNavController: NavController
) {
    val nodesFeatures = listOf(
        Triple(
            Icons.Filled.RocketLaunch,
            R.string.home_feature_collab_title,
            R.string.home_feature_collab_desc
        ), Triple(
            Icons.Filled.Checklist,
            R.string.home_feature_notes_title,
            R.string.home_feature_notes_desc
        ), Triple(
            Icons.Filled.NotificationsActive,
            R.string.home_feature_reminders_title,
            R.string.home_feature_reminders_desc
        ), Triple(
            Icons.Filled.PhotoCamera,
            R.string.home_feature_multimedia_title,
            R.string.home_feature_multimedia_desc
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        NodesBackground(speedMultiplier = 0.7f, nodeCount = 30)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(outerPadding)
                .entranceAnimation(),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "NODES",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 6.sp,
                    modifier = Modifier.padding(vertical = 16.dp).padding(top = 32.dp)
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = stringResource(R.string.home_welcome_message, userName),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.home_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Filled.Add,
                        title = stringResource(R.string.home_quick_action_create),
                        onClick = { 
                            nodesViewModel.setPendingAction(NodeAction.CREATE)
                            contentNavController.navigate(Routes.Nodes.route) {
                                popUpTo(contentNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    QuickActionCard(
                        icon = Icons.Filled.GroupAdd,
                        title = stringResource(R.string.home_quick_action_join),
                        onClick = { 
                            nodesViewModel.setPendingAction(NodeAction.JOIN)
                            contentNavController.navigate(Routes.Nodes.route) {
                                popUpTo(contentNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    QuickActionCard(
                        icon = Icons.AutoMirrored.Filled.Notes,
                        title = stringResource(R.string.home_quick_action_private),
                        onClick = {
                            contentNavController.navigate(Routes.Nodes.route) {
                                popUpTo(contentNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.home_tips_header),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }

            items(nodesFeatures) { (icon, titleRes, descRes) ->
                FeatureCard(
                    icon = icon,
                    title = stringResource(titleRes),
                    description = stringResource(descRes),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }

            item {
                AppVersionFooter(
                    appVersion = appVersion,
                    modifier = Modifier.padding(top = 40.dp)
                )
            }
        }
    }
}

@Composable
fun QuickActionCard(icon: ImageVector, title: String, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .width(130.dp)
            .height(130.dp)
            .pressClickEffect(),
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
        }
    }
}

@Composable
fun FeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .pressClickEffect(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(Modifier.width(20.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun AppVersionFooter(appVersion: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.home_footer_version, appVersion),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

fun getAppVersion(context: Context): String {
    return context.getString(R.string.default_version, com.maxigimenez.nodes.BuildConfig.VERSION_NAME)
}

private class HomeParticleNode(
    var x: Float,
    var y: Float,
    var dx: Float,
    var dy: Float,
    val radius: Float
)
