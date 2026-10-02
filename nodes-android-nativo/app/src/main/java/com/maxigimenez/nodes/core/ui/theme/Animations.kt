package com.maxigimenez.nodes.core.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

object NodesAnimations {
    // Duraciones estandarizadas
    const val ShortDuration = 250
    const val MediumDuration = 400
    const val LongDuration = 600

    // Easings modernos
    val FastOutSlowIn = FastOutSlowInEasing
    val StandardEasing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    val EmphasizedEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    // Transiciones de Navegación Predefinidas
    fun enterTransition(): EnterTransition {
        return fadeIn(animationSpec = tween(MediumDuration, easing = FastOutSlowIn)) +
                slideInHorizontally(
                    initialOffsetX = { 300 },
                    animationSpec = tween(MediumDuration, easing = FastOutSlowIn)
                )
    }

    fun exitTransition(): ExitTransition {
        return fadeOut(animationSpec = tween(MediumDuration, easing = FastOutSlowIn)) +
                slideOutHorizontally(
                    targetOffsetX = { -300 },
                    animationSpec = tween(MediumDuration, easing = FastOutSlowIn)
                )
    }

    fun popEnterTransition(): EnterTransition {
        return fadeIn(animationSpec = tween(MediumDuration, easing = FastOutSlowIn)) +
                slideInHorizontally(
                    initialOffsetX = { -300 },
                    animationSpec = tween(MediumDuration, easing = FastOutSlowIn)
                )
    }

    fun popExitTransition(): ExitTransition {
        return fadeOut(animationSpec = tween(MediumDuration, easing = FastOutSlowIn)) +
                slideOutHorizontally(
                    targetOffsetX = { 300 },
                    animationSpec = tween(MediumDuration, easing = FastOutSlowIn)
                )
    }
}

/**
 * Extension para aplicar una animación de entrada (Fade + Scale + Offset)
 */
@Composable
fun Modifier.entranceAnimation(
    delay: Int = 0,
    duration: Int = NodesAnimations.MediumDuration
): Modifier {
    val animationState = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        animationState.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = duration,
                delayMillis = delay,
                easing = NodesAnimations.EmphasizedEasing
            )
        )
    }

    return this
        .alpha(animationState.value)
        .scale(0.95f + (0.05f * animationState.value))
        .offset(y = (20 * (1f - animationState.value)).dp)
}

/**
 * Para animaciones staggered en listas
 */
@Composable
fun staggeredDelay(index: Int, baseDelay: Int = 50): Int {
    return index * baseDelay
}

/**
 * Efecto de escala al presionar para botones o tarjetas
 */
@Composable
fun Modifier.pressClickEffect(): Modifier {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "pressScale",
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    return this
        .scale(scale)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                }
            )
        }
}
