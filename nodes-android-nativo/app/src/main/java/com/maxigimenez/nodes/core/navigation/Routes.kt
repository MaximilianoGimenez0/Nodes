package com.maxigimenez.nodes.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.maxigimenez.nodes.R

enum class Routes(
    val route: String,
    val icon: ImageVector,
    @StringRes val labelResId: Int,
    @StringRes val contentDescResId: Int
) {
    Home(
        route = "home",
        icon = Icons.Default.Home,
        labelResId = R.string.bottom_nav_home,
        contentDescResId = R.string.bottom_nav_home_desc
    ),
    Nodes(
        route = "workspaces_graph",
        icon = Icons.AutoMirrored.Filled.List,
        labelResId = R.string.bottom_nav_workspaces,
        contentDescResId = R.string.bottom_nav_workspaces_desc
    ),
    Profile(
        route = "settings",
        icon = Icons.Default.Settings,
        labelResId = R.string.bottom_nav_profile,
        contentDescResId = R.string.bottom_nav_profile_desc
    )
}
