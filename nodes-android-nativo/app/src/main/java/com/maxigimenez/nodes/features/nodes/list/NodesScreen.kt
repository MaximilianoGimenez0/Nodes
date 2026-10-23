package com.maxigimenez.nodes.features.nodes.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.maxigimenez.nodes.core.util.ToastHelper
import com.maxigimenez.nodes.core.ui.components.NodesBackground
import com.maxigimenez.nodes.features.nodes.components.CreateNodeDialog
import com.maxigimenez.nodes.features.nodes.components.EmptyNodesState
import com.maxigimenez.nodes.features.nodes.components.JoinNodeDialog
import com.maxigimenez.nodes.features.nodes.components.NodeList
import com.maxigimenez.nodes.features.nodes.components.NodesFabMenu
import kotlinx.coroutines.launch

enum class DialogType {
    NONE, CREATE_NODE, JOIN_NODE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodesScreen(
    contentController: NavController,
    nodesViewModel: NodesViewModel,
    modifier: Modifier,
    outerPadding: PaddingValues
) {
    val uiState by nodesViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val userInfo by nodesViewModel.userInfo.collectAsStateWithLifecycle()

    LaunchedEffect(userInfo) {
        userInfo?.let { message ->
            ToastHelper.showToast(context, message)
            nodesViewModel.clearUserInfo()
        }
    }

    var isFabMenuExpanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(DialogType.NONE) }
    var nodeName by remember { mutableStateOf("") }
    var nodeCode by remember { mutableStateOf("") }

    val pendingAction by nodesViewModel.pendingAction.collectAsStateWithLifecycle()

    LaunchedEffect(pendingAction) {
        when (pendingAction) {
            NodeAction.CREATE -> {
                showDialog = DialogType.CREATE_NODE
                nodesViewModel.setPendingAction(NodeAction.NONE)
            }
            NodeAction.JOIN -> {
                showDialog = DialogType.JOIN_NODE
                nodesViewModel.setPendingAction(NodeAction.NONE)
            }
            NodeAction.NONE -> {}
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        NodesBackground(speedMultiplier = 0.5f, nodeCount = 20)

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .padding(outerPadding),
            containerColor = Color.Transparent, 
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "NODES",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 4.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                    )
                )
            },
            floatingActionButton = {
                NodesFabMenu(
                    isFabMenuExpanded = isFabMenuExpanded,
                    onFabClicked = { isFabMenuExpanded = !isFabMenuExpanded },
                    onCreateClicked = {
                        isFabMenuExpanded = false
                        showDialog = DialogType.CREATE_NODE
                    },
                    onJoinClicked = {
                        isFabMenuExpanded = false
                        showDialog = DialogType.JOIN_NODE
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.workspaces.isEmpty()) {
                    EmptyNodesState(modifier = Modifier.align(Alignment.Center))
                } else {
                    NodeList(
                        modifier = Modifier.fillMaxSize(),
                        nodes = uiState.workspaces,
                        contentNavController = contentController
                    )
                }
            }
        }
    }

    when (showDialog) {
        DialogType.CREATE_NODE -> {
            CreateNodeDialog(
                nodeName = nodeName,
                onNameChange = { nodeName = it },
                onConfirm = {
                    nodesViewModel.onNewNodeRequested(nodeName)
                    showDialog = DialogType.NONE
                    nodeName = ""
                },
                onDismiss = {
                    showDialog = DialogType.NONE
                    nodeName = ""
                })
        }

        DialogType.JOIN_NODE -> {
            JoinNodeDialog(
                nodeCode = nodeCode,
                onCodeChange = { nodeCode = it.uppercase() },
                onConfirm = {
                    nodesViewModel.joinNode(nodeCode)
                    showDialog = DialogType.NONE
                    nodeCode = ""
                },
                onDismiss = {
                    showDialog = DialogType.NONE
                    nodeCode = ""
                })
        }

        DialogType.NONE -> {}
    }
}
