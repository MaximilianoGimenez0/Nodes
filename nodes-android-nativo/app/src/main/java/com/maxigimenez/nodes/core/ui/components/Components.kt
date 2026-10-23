package com.maxigimenez.nodes.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.unit.sp
import com.maxigimenez.nodes.R
import com.maxigimenez.nodes.data.model.ChecklistItem
import com.maxigimenez.nodes.core.navigation.Routes
import com.maxigimenez.nodes.features.auth.ParticleNode
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import kotlin.math.sqrt
import kotlin.random.Random


@Composable
fun ShimmerImageLoader(modifier: Modifier = Modifier) {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(brush)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChecklistDialog(
    onConfirm: (title: String, items: List<ChecklistItem>) -> Unit, onDismiss: () -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var items by remember { mutableStateOf(listOf("")) }
    var currentItemText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.checklist_dialog_title)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CharacterLimitedOutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    maxLength = 20,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(id = R.string.checklist_dialog_list_title_label)) },
                    singleLine = true,
                )

                HorizontalDivider()

                items.forEachIndexed { index, item ->
                    if (item.isNotBlank()) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .padding(end = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(item)
                            }

                            IconButton(
                                onClick = {
                                    items = items.filterIndexed { i, _ -> i != index }
                                }, modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = stringResource(id = R.string.checklist_dialog_delete_item_desc),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                CharacterLimitedOutlinedTextField(
                    value = currentItemText,
                    onValueChange = { currentItemText = it },
                    maxLength = 20,
                    label = { Text(stringResource(id = R.string.checklist_dialog_new_item_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (currentItemText.isNotBlank()) {
                                    items = items + currentItemText
                                    currentItemText = ""
                                }
                            }, enabled = currentItemText.isNotBlank()
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = stringResource(id = R.string.checklist_dialog_add_item_desc)
                            )
                        }
                    })
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                val finalItems = (items + currentItemText).filter { it.isNotBlank() }
                onConfirm(
                    title, finalItems.map { ChecklistItem(text = it, isChecked = false) })
            },
                enabled = title.isNotBlank() && (items.any { it.isNotBlank() } || currentItemText.isNotBlank())) {
                Text(stringResource(id = R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(id = R.string.cancel)) }
        })
}


@Composable
fun MainBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        windowInsets = NavigationBarDefaults.windowInsets,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Routes.entries.forEach { destination ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
            
            val label = stringResource(destination.labelResId)
            val contentDesc = stringResource(destination.contentDescResId)

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    } else {
                        // Volver a la raíz del grafo si se pulsa la pestaña actualmente activa
                        if (destination == Routes.Nodes && currentDestination.route != "workspaces") {
                            val popped = navController.popBackStack("workspaces", inclusive = false)
                            if (!popped) {
                                navController.navigate("workspaces") {
                                    popUpTo(Routes.Nodes.route) {
                                        inclusive = true
                                    }
                                }
                            }
                        }
                    }
                },
                icon = {
                    Icon(
                        destination.icon,
                        contentDescription = contentDesc
                    )
                },
                label = {
                    Text(label, style = MaterialTheme.typography.labelSmall)
                }
            )
        }
    }
}


@Composable
fun CharacterLimitedOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    maxLength: Int,
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.length <= maxLength) {
                onValueChange(newValue)
            }
        },
        label = label,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        modifier = modifier,
        trailingIcon = trailingIcon,
        supportingText = {
            Text(
                text = "${value.length} / $maxLength",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        },
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomLoginField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    isPasswordHidden: Boolean = false,
    onVisibilityClick: () -> Unit = {},
    errorText: String? = null,
    maxLength: Int = Int.MAX_VALUE,
    singleLine: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = value,
            onValueChange = { newValue ->
                if (newValue.length <= maxLength) {
                    onValueChange(newValue)
                }
            },
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(16.dp),
            isError = errorText != null,
            leadingIcon = leadingIcon,
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            visualTransformation = if (isPassword && isPasswordHidden) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
                if (isPassword) {
                    IconButton(onClick = onVisibilityClick) {
                        Icon(
                            imageVector = if (isPasswordHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = if (errorText != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            singleLine = singleLine,
            keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default,
            textStyle = MaterialTheme.typography.bodyLarge
        )

        if (errorText != null) {
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterRow(
    categories: List<String>, selectedCategory: String?, onCategorySelected: (String) -> Unit
) {
    if (categories.isNotEmpty()) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = (selectedCategory == category),
                    onClick = { onCategorySelected(category) },
                    label = { Text(category) })
            }
        }
    }
}

@Composable
fun NodesBackground(
    modifier: Modifier = Modifier,
    nodeCount: Int = 50,
    minRadius: Float = 4f,
    maxRadius: Float = 12f,
    speedMultiplier: Float = 1.0f,
    maxDistance: Float = 300f,
    color: Color = MaterialTheme.colorScheme.primary
) {
    var currentTime by remember { mutableStateOf(0L) }
    val nodes = remember(nodeCount, minRadius, maxRadius) { mutableStateListOf<ParticleNode>() }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { time ->
                currentTime = time
            }
        }
    }

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val time = currentTime // Forzar la invalidación del dibujo en cada frame mediante lectura de estado

        if (nodes.isEmpty() && size.width > 0) {
            for (i in 0 until nodeCount) {
                val radiusSpan = (maxRadius - minRadius).coerceAtLeast(0f)
                nodes.add(
                    ParticleNode(
                        x = Random.nextFloat() * size.width,
                        y = Random.nextFloat() * size.height,
                        dx = (Random.nextFloat() - 0.5f) * 1.5f,
                        dy = (Random.nextFloat() - 0.5f) * 1.5f,
                        radius = Random.nextFloat() * radiusSpan + minRadius
                    )
                )
            }
        }

        nodes.forEach { node ->
            node.x += node.dx * speedMultiplier
            node.y += node.dy * speedMultiplier

            if (node.x <= 0f || node.x >= size.width) node.dx *= -1
            if (node.y <= 0f || node.y >= size.height) node.dy *= -1
        }

        for (i in 0 until nodes.size) {
            for (j in i + 1 until nodes.size) {
                val n1 = nodes[i]
                val n2 = nodes[j]

                val dx = n1.x - n2.x
                val dy = n1.y - n2.y
                val distance = sqrt(dx * dx + dy * dy)

                if (distance < maxDistance) {
                    val rawOpacity = 1f - (distance / maxDistance)
                    val lineOpacity = rawOpacity * 0.4f

                    drawLine(
                        color = color.copy(alpha = lineOpacity),
                        start = Offset(n1.x, n1.y),
                        end = Offset(n2.x, n2.y),
                        strokeWidth = 2.5f
                    )
                }
            }
        }

        nodes.forEach { node ->
            drawCircle(
                color = color.copy(alpha = 0.6f),
                radius = node.radius,
                center = Offset(node.x, node.y)
            )
        }
    }
}
