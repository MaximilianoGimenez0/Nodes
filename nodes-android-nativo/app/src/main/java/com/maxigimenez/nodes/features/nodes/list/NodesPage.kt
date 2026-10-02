package com.maxigimenez.nodes.features.nodes.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.maxigimenez.nodes.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodesPage(
    modifier: Modifier = Modifier,
    outerPadding: PaddingValues,
    contentController: NavController,
    nodesViewModel: NodesViewModel
) {
    NodesScreen(
        contentController = contentController,
        nodesViewModel = nodesViewModel,
        modifier = modifier.fillMaxSize(),
        outerPadding = outerPadding
    )
}
